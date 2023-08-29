package io.quarkiverse.loggingmanager;

import java.util.function.Consumer;

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

    public Consumer<Route> routeConsumer(Handler<RoutingContext> bodyHandler, LoggingManagerRuntimeConfig runtimeConfig) {
        if (runtimeConfig.enable) {
            return new Consumer<Route>() {
                @Override
                public void accept(Route route) {
                    route.handler(bodyHandler);
                }
            };
        } else {
            return new Consumer<Route>() {
                @Override
                public void accept(Route route) {
                    route.handler(new LoggingManagerNotFoundHandler());
                }
            };
        }

    }
}
