package io.quarkiverse.loggingmanager;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

import java.util.Objects;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Handler for logger-related HTTP endpoints.
 * Supports GET requests to retrieve logger information and POST requests to update logger levels.
 */
public class LoggerGetHandler implements Handler<RoutingContext> {
    private static final String LOGGER_NAME_PARAM = "loggerName";

    /**
     * Handles incoming HTTP requests by delegating to appropriate method handlers.
     *
     * @param routingContext The Vert.x routing context
     */
    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        HttpMethod method = request.method();

        if (GET == method) {
            handleGet(request, response);
        }
    }

    /**
     * Handles GET requests to retrieve logger information.
     * Returns all loggers if no logger name is specified, or details for a specific logger.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     */
    private void handleGet(HttpServerRequest request, HttpServerResponse response) {
        response.headers().add("Content-Type", "application/json");

        String loggerName = request.getParam(LOGGER_NAME_PARAM);
        if (loggerName == null || loggerName.isEmpty()) {
            response.end(LogController.getLoggers().build());
        } else {
            response.end(Objects.requireNonNull(LogController.getLogger(loggerName)).build());
        }
    }
}