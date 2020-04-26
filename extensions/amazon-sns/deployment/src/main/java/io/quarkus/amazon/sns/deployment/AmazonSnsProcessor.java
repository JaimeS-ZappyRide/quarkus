package io.quarkus.amazon.sns.deployment;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.DeploymentException;

import io.quarkus.builder.item.MultiBuildItem;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.BeanRegistrationPhaseBuildItem;
import io.quarkus.arc.processor.BuildExtension;
import io.quarkus.arc.processor.InjectionPointInfo;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.configuration.ConfigurationError;
import io.quarkus.deployment.builditem.AdditionalApplicationArchiveMarkerBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageProxyDefinitionBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;

import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.SdkHttpService;
import software.amazon.awssdk.http.async.SdkAsyncHttpService;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsClient;

import io.quarkus.amazon.common.base.SyncHttpClientBuildTimeConfig;

public class AmazonSnsProcessor {

    private static final String FEATURE = "amazon-sns";

    public static final String AWS_SDK_APPLICATION_ARCHIVE_MARKERS = "software/amazon/awssdk";
    
    private static final String APACHE_HTTP_SERVICE = "software.amazon.awssdk.http.apache.ApacheSdkHttpService";
    private static final String NETTY_HTTP_SERVICE = "software.amazon.awssdk.http.nio.netty.NettySdkAsyncHttpService";
    private static final String URL_HTTP_SERVICE = "software.amazon.awssdk.http.urlconnection.UrlConnectionSdkHttpService";

    private static final List<String> INTERCEPTOR_PATHS = Arrays.asList(
            "software/amazon/awssdk/global/handlers/execution.interceptors",
            "software/amazon/awssdk/services/Sns/execution.interceptors");

    private static final DotName EXECUTION_INTERCEPTOR_NAME = DotName.createSimple(ExecutionInterceptor.class.getName());
    private static final DotName SYNC_CLIENT_NAME = DotName.createSimple(SnsClient.class.getName());
    private static final DotName ASYNC_CLIENT_NAME = DotName.createSimple(SnsAsyncClient.class.getName());

    SnsBuildTimeConfig buildTimeConfig;

    @BuildStep
    AdditionalApplicationArchiveMarkerBuildItem marker() {
        return new AdditionalApplicationArchiveMarkerBuildItem(AWS_SDK_APPLICATION_ARCHIVE_MARKERS);
    }

    @BuildStep
    void setup(CombinedIndexBuildItem combinedIndexBuildItem,
               BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport,
               BuildProducer<ReflectiveClassBuildItem> reflectiveClasses,
               BuildProducer<FeatureBuildItem> feature,
               BuildProducer<AdditionalBeanBuildItem> additionalBeans,
               BuildProducer<NativeImageResourceBuildItem> resource) {

        feature.produce(new FeatureBuildItem(SnsFeatureBuildItem.SNS));

        // Indicates that this extension would like the SSL support to be enabled
        extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem(SnsFeatureBuildItem.SNS));

        INTERCEPTOR_PATHS.forEach(path -> resource.produce(new NativeImageResourceBuildItem(path)));

        List<String> knownInterceptorImpls = combinedIndexBuildItem.getIndex()
                .getAllKnownImplementors(EXECUTION_INTERCEPTOR_NAME)
                .stream()
                .map(c -> c.name().toString()).collect(Collectors.toList());

        buildTimeConfig.sdk.interceptors.orElse(Collections.emptyList()).forEach(interceptorClass -> {
            if (!knownInterceptorImpls.contains(interceptorClass.getName())) {
                throw new ConfigurationError(
                        String.format(
                                "quarkus.Sns.sdk.interceptors (%s) - must list only existing implementations of software.amazon.awssdk.core.interceptor.ExecutionInterceptor",
                                buildTimeConfig.sdk.interceptors.toString()));
            }
        });

        reflectiveClasses.produce(new ReflectiveClassBuildItem(false, false,
                knownInterceptorImpls.toArray(new String[knownInterceptorImpls.size()])));

        additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(SnsClientProducer.class));
    }

    @BuildStep
    SnsClientBuildItem analyzeSnsClientInjectionPoints(BeanRegistrationPhaseBuildItem beanRegistrationPhase,
                                                       BuildProducer<ServiceProviderBuildItem> serviceProvider,
                                                       BuildProducer<NativeImageProxyDefinitionBuildItem> proxyDefinition) {

        boolean createSyncClient = false;
        boolean createAsyncClient = false;

        for (InjectionPointInfo injectionPoint : beanRegistrationPhase.getContext()
                .get(BuildExtension.Key.INJECTION_POINTS)) {
            Type requiredType = injectionPoint.getRequiredType();

            if (SYNC_CLIENT_NAME.equals(requiredType.name())) {
                createSyncClient = true;
            }

            if (ASYNC_CLIENT_NAME.equals(requiredType.name())) {
                createAsyncClient = true;
            }
        }

        if (createSyncClient) {
            if (buildTimeConfig.syncClient.type == SyncHttpClientBuildTimeConfig.SyncClientType.APACHE) {
                checkClasspath(APACHE_HTTP_SERVICE, "apache-client");

                //Register Apache client as sync client
                proxyDefinition.produce(
                        new NativeImageProxyDefinitionBuildItem("org.apache.http.conn.HttpClientConnectionManager",
                                "org.apache.http.pool.ConnPoolControl",
                                "software.amazon.awssdk.http.apache.internal.conn.Wrapped"));

                serviceProvider.produce(new ServiceProviderBuildItem(SdkHttpService.class.getName(), APACHE_HTTP_SERVICE));
            } else {
                checkClasspath(URL_HTTP_SERVICE, "url-connection-client");
                serviceProvider.produce(new ServiceProviderBuildItem(SdkHttpService.class.getName(), URL_HTTP_SERVICE));
            }
        }

        if (createAsyncClient) {
            checkClasspath(NETTY_HTTP_SERVICE, "netty-nio-client");
            //Register netty as async client
            serviceProvider.produce(new ServiceProviderBuildItem(SdkAsyncHttpService.class.getName(), NETTY_HTTP_SERVICE));
        }

        return new SnsClientBuildItem(createSyncClient, createAsyncClient);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void buildClients(SnsClientBuildItem clientBuildItem, SnsServiceConfig runtimeConfig, SnsRecorder recorder,
                      BeanContainerBuildItem beanContainer, ShutdownContextBuildItem shutdown) {

        if (clientBuildItem.isCreateSyncClient() || clientBuildItem.isCreateAsyncClient()) {

            if (clientBuildItem.isCreateSyncClient()) {
                recorder.createClient(beanContainer.getValue(), shutdown);
            }

            if (clientBuildItem.isCreateAsyncClient()) {
                recorder.createAsyncClient(beanContainer.getValue(), shutdown);
            }
        }
    }

    private void checkClasspath(String className, String dependencyName) {
        try {
            Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(
                    "Missing 'software.amazon.awssdk:" + dependencyName + "' dependency on the classpath");
        }
    }
}
