package io.quarkus.amazon.common.base;

import io.quarkus.runtime.annotations.ConfigItem;
/**
 * Sub classes must implement 
 * 
 */

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public abstract class AwsServiceConfig {
        /**
         * Enable DynamoDB service endpoint discovery. //
         */
        // @ConfigItem
        // public boolean enableEndpointDiscovery;

        /**
         * SDK client configurations
         */
        @ConfigItem(name = ConfigItem.PARENT)
        public SdkConfig sdk;

        /**
         * AWS service configurations
         */
        @ConfigItem
        public AwsConfig aws;

        /**
         * Sync client transport configuration
         */
        @ConfigItem
        public SyncHttpClientConfig syncClient;

        /**
         * Netty HTTP client transport configuration
         */
        @ConfigItem
        public NettyHttpClientConfig asyncClient;
}
