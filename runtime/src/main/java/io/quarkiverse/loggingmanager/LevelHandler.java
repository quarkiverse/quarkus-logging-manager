package io.quarkiverse.loggingmanager;

import static io.vertx.core.http.HttpMethod.GET;

import io.quarkus.vertx.http.runtime.logstream.LogController;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class LevelHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {

        HttpServerRequest request = routingContext.request();
        HttpMethod method = request.method();

        HttpServerResponse response = routingContext.response();
        response.headers().add("Content-Type", "application/json");

        if (GET == method) {
            response.end(LogController.getLevels().build());
        } else {
            response.end();
        }
    }

}
