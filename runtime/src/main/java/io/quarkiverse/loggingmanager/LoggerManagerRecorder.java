package io.quarkiverse.loggingmanager;

import java.util.function.Consumer;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class LoggerManagerRecorder {

    public Handler<RoutingContext> loggerHandler() {
        return new LoggerHandler();
    }

    public Handler<RoutingContext> levelHandler() {
        return new LevelHandler();
    }

    public Consumer<Route> routeConsumer(Handler<RoutingContext> bodyHandler,
            RuntimeValue<LoggingManagerRuntimeConfig> runtimeConfig) {
        if (runtimeConfig.getValue().enable()) {
            return route -> route.handler(bodyHandler);
        } else {
            return route -> route.handler(new LoggingManagerNotFoundHandler());
        }

    }
}