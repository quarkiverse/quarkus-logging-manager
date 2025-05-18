package io.quarkiverse.loggingmanager;

import java.util.function.Consumer;

import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

@Recorder
public class LoggerManagerRecorder {

    public Handler<RoutingContext> loggerGetHandler() {
        return new LoggerGetHandler();
    }

    public Handler<RoutingContext> loggerPostHandler() {
        return new LoggerPostHandler();
    }

    public Handler<RoutingContext> levelHandler() {
        return new LevelHandler();
    }

    public Consumer<Route> routeGetConsumer(Handler<RoutingContext> bodyHandler, LoggingManagerRuntimeConfig runtimeConfig) {
        if (runtimeConfig.enable()) {
            return new GetRouterConsumer();
        } else {
            return route -> route.handler(new LoggingManagerNotFoundHandler());
        }
    }

    public Consumer<Route> routePostConsumer(Handler<RoutingContext> bodyHandler, LoggingManagerRuntimeConfig runtimeConfig) {
        if (runtimeConfig.enable()) {
            return new PostRouterConsumer();
        } else {
            return route -> route.handler(new LoggingManagerNotFoundHandler());
        }
    }

    public static class GetRouterConsumer implements Consumer<Route> {

        @Override
        public void accept(Route route) {
            route.method(HttpMethod.GET);
        }
    }

    public static class PostRouterConsumer implements Consumer<Route> {

        @Override
        public void accept(Route route) {
            route.method(HttpMethod.POST).handler(BodyHandler.create());
        }
    }
}