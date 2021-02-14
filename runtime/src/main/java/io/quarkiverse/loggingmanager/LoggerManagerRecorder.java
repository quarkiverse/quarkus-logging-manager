package io.quarkiverse.loggingmanager;

import java.util.function.Function;

import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.vertx.http.runtime.logstream.LogStreamWebSocket;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
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

        if (runtimeConfig.enable) {
            return new LoggingManagerStaticHandler(loggingManagerFinalDestination, loggingManagerPath);
        } else {
            return new LoggingManagerNotFoundHandler();
        }
    }

    public Handler<RoutingContext> logStreamWebSocketHandler(LoggingManagerRuntimeConfig runtimeConfig) {
        if (runtimeConfig.enable) {
            return new LogStreamWebSocket();
        } else {
            return new LoggingManagerNotFoundHandler();
        }
    }

    public Function<Router, Route> routeFunction(String rootPath, Handler<RoutingContext> bodyHandler) {
        return new Function<Router, Route>() {
            @Override
            public Route apply(Router router) {
                return router.route(rootPath).handler(bodyHandler);
            }
        };
    }
}
