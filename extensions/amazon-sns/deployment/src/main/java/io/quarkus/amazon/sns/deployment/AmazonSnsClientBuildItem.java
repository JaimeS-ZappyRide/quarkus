package io.quarkus.amazon.sns.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

public class AmazonSnsClientBuildItem extends SimpleBuildItem {

    private final boolean createSyncClient;
    private final boolean createAsyncClient;

    public AmazonSnsClientBuildItem(boolean createSyncClient, boolean createAsyncClient) {
        this.createSyncClient = createSyncClient;
        this.createAsyncClient = createAsyncClient;
    }

    public AmazonSnsClientBuildItem() {
        createSyncClient = false;
        createAsyncClient = true;
    }

    public boolean isCreateSyncClient() {
        return createSyncClient;
    }

    public boolean isCreateAsyncClient() {
        return createAsyncClient;
    }
}
