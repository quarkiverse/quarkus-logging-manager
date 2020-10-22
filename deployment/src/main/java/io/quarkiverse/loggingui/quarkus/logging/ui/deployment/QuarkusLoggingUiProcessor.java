package io.quarkiverse.loggingui.quarkus.logging.ui.deployment;

import static io.quarkiverse.loggingui.quarkus.logging.ui.LoggerHandler.LOGGER_NAME_PATH_PARAM;

import io.quarkiverse.loggingui.quarkus.logging.ui.LoggerHandler;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.runtime.HandlerType;

class QuarkusLoggingUiProcessor {
    private static final String FEATURE = "quarkus-logging-ui";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void defineRoute(BuildProducer<RouteBuildItem> routes) {
        routes.produce(new RouteBuildItem("/loggers", new LoggerHandler(), HandlerType.BLOCKING));
        routes.produce(new RouteBuildItem("/loggers/:" + LOGGER_NAME_PATH_PARAM, new LoggerHandler(), HandlerType.BLOCKING));
    }
}
