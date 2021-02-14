package io.quarkiverse.loggingmanager;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

import io.quarkus.vertx.http.runtime.logstream.LogController;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class LoggerHandler implements Handler<RoutingContext> {
    private static final String LOGGER_NAME_PARAM = "loggerName";
    private static final String LOGGER_LEVEL_PARAM = "loggerLevel";

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();

        HttpMethod method = request.method();

        if (GET == method) {
            handleGet(request, response);
        } else if (POST == method) {
            handlePost(request, response);
        }
    }

    private void handleGet(HttpServerRequest request, HttpServerResponse response) {
        response.headers().add("Content-Type", "application/json");

        String loggerName = request.getParam(LOGGER_NAME_PARAM);
        if (loggerName == null || loggerName.isEmpty()) {
            response.end(LogController.getLoggers().build());
        } else {
            response.end(LogController.getLogger(loggerName).build());
        }
    }

    private void handlePost(HttpServerRequest request, HttpServerResponse response) {

        String loggerName = request.getFormAttribute(LOGGER_NAME_PARAM);
        String loggerLevel = request.getFormAttribute(LOGGER_LEVEL_PARAM);
        if (loggerLevel == null || loggerLevel.isEmpty()) {
            LogController.updateLogLevel(loggerName, null);
        } else {
            LogController.updateLogLevel(loggerName, loggerLevel);
        }

        response.setStatusCode(201).end();

    }
}
