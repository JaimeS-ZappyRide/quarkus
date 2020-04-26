package io.quarkus.amazon.common.base.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class AmazonCommonBaseProcessor {

    private static final String FEATURE = "amazon-common-base";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

}
