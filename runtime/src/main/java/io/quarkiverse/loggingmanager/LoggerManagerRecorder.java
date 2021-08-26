package io.quarkiverse.loggingmanager;

import java.util.Optional;
import java.util.function.Consumer;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.vertx.http.runtime.logstream.LogStreamWebSocket;
import io.quarkus.vertx.http.runtime.logstream.WebSocketLogHandler;
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

    public Handler<RoutingContext> uiHandler(String loggingManagerFinalDestination, String loggingManagerPath,
            LoggingManagerRuntimeConfig runtimeConfig) {

        if (runtimeConfig.enableUi) {
            return new LoggingManagerStaticHandler(loggingManagerFinalDestination, loggingManagerPath);
        } else {
            return new LoggingManagerNotFoundHandler();
        }
    }

    public Handler<RoutingContext> logStreamWebSocketHandler(LoggingManagerRuntimeConfig runtimeConfig,
            RuntimeValue<Optional<WebSocketLogHandler>> historyHandler) {
        if (runtimeConfig.enableUi) {
            return new LogStreamWebSocket(historyHandler.getValue().get());
        } else {
            return new LoggingManagerNotFoundHandler();
        }
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
