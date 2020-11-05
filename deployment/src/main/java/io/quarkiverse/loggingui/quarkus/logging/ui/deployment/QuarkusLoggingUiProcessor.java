package io.quarkiverse.loggingui.quarkus.logging.ui.deployment;

import java.util.Arrays;
import java.util.List;

import io.quarkiverse.loggingui.quarkus.logging.ui.LoggerHandler;
import io.quarkiverse.loggingui.quarkus.logging.ui.RouteRegistrar;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class QuarkusLoggingUiProcessor {
    private static final String FEATURE = "quarkus-logging-ui";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    List<AdditionalBeanBuildItem> additionalBeans() {
        return Arrays.asList(
                new AdditionalBeanBuildItem(RouteRegistrar.class),
                new AdditionalBeanBuildItem(LoggerHandler.class));
    }
}
