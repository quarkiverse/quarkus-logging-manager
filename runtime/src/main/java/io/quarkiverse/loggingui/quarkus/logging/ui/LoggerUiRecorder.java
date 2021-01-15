package io.quarkiverse.loggingui.quarkus.logging.ui;

import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class LoggerUiRecorder {

    public Handler<RoutingContext> loggerHandler() {
        return new LoggerHandler();
    }

    public Handler<RoutingContext> levelHandler() {
        return new LevelHandler();
    }

    public Handler<RoutingContext> uiHandler(String loggingUiFinalDestination, String loggingUiPath,
            LoggingUiRuntimeConfig runtimeConfig) {

        if (runtimeConfig.enable) {
            return new LoggingUiStaticHandler(loggingUiFinalDestination, loggingUiPath);
        } else {
            return new LoggingUiNotFoundHandler();
        }
    }

}
