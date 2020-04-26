package io.quarkus.amazon.sns;

import com.zappyride.quarkus.aws.config.SdkBuildTimeConfig;
import com.zappyride.quarkus.aws.config.SyncHttpClientBuildTimeConfig;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "AmazonSns", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class AmazonSnsBuildTimeConfig {

    /**
     * SDK client configurations
     */
    @ConfigItem(name = ConfigItem.PARENT)
    public SdkBuildTimeConfig sdk;

    /**
     * Sync client transport configuration
     */
    @ConfigItem(name = "sync-client")
    public SyncHttpClientBuildTimeConfig syncClient;
}
