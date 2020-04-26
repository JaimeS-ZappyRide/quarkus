package io.quarkus.amazon.sns.deployment;

import io.quarkus.builder.item.MultiBuildItem;

import java.util.Objects;

public final class AmazonSnsFeatureBuildItem extends MultiBuildItem {

    public static final String SNS = "amazon-simple-notification-service";
    private final String info;

    public AmazonSnsFeatureBuildItem(String info) {
        this.info = (String) Objects.requireNonNull(info);
    }

    public String getInfo() {
        return this.info;
    }
}
