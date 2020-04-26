package io.quarkus.amazon.sns;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsClient;

@Recorder
public class AmazonSnsRecorder {

    public RuntimeValue<SnsClient> createClient(BeanContainer beanContainer, ShutdownContext shutdown) {

        AmazonSnsClientProducer producer = beanContainer.instance(AmazonSnsClientProducer.class);
        shutdown.addShutdownTask(producer::destroy);
        return new RuntimeValue<>(producer.client());
    }

    public RuntimeValue<SnsAsyncClient> createAsyncClient(BeanContainer beanContainer, ShutdownContext shutdown) {

        AmazonSnsClientProducer producer = beanContainer.instance(AmazonSnsClientProducer.class);
        shutdown.addShutdownTask(producer::destroy);
        return new RuntimeValue<>(producer.asyncClient());
    }
}