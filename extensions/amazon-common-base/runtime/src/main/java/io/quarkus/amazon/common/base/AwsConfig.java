package io.quarkus.amazon.common.base;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import software.amazon.awssdk.regions.Region;

import java.util.Optional;

@ConfigGroup
public class AwsConfig {
        // @formatter:off
        /**
         * An Amazon Web Services region that hosts DynamoDB.
         *
         * It overrides region provider chain with static value of
         * region with which the DynamoDB client should communicate.
         *
         * If not set, region is retrieved via the default providers chain in the following order:
         *
         * * `aws.region` system property
         * * `region` property from the profile file
         * * Instance profile file
         *
         * See `software.amazon.awssdk.regions.Region` for available regions.
         *
         * @asciidoclet
         */
        // @formatter:on
        @ConfigItem
        public Optional<Region> region = Optional.ofNullable(Region.US_EAST_1);

        /**
         * Defines credentials provider used
         */
        @ConfigItem
        public AwsCredentialsProviderConfig credentials;
}
