package io.quarkiverse.loggingmanager;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logmanager.LogContext;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Handler for logger-related HTTP endpoints.
 * Supports GET requests to retrieve logger information and POST requests to update logger levels.
 */
public class LoggerHandler implements Handler<RoutingContext> {
    private static final String LOGGER_NAME_PARAM = "loggerName";
    private static final String LOGGER_LEVEL_PARAM = "loggerLevel";
    private static final String LOGGER_DURATION = "duration";
    private static final String TEMPORARY_ENABLED = "temporary";

    private final ConcurrentHashMap<String, TemporaryLogState> temporaryStates = new ConcurrentHashMap<>();

    private static class TemporaryLogState {
        final String originalLevel;
        long timerId;

        TemporaryLogState(String originalLevel, long timerId) {
            this.originalLevel = originalLevel;
            this.timerId = timerId;
        }
    }

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
        } else if (POST == method) {
            handlePost(request, response, routingContext);
        } else {
            response.setStatusCode(405).end(); // Method not allowed
        }
    }

    /**
     * Handles GET requests to retrieve logger information. Returns all loggers if no logger name is specified, or
     * details for a specific logger.
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

    /**
     * Handles POST requests to update logger levels. Updates the specified logger with the provided level or removes
     * the level if none specified.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     */
    private void handlePost(HttpServerRequest request, HttpServerResponse response, RoutingContext routingContext) {
        String contentType = request.getHeader("Content-Type");
        if (!"application/x-www-form-urlencoded".equals(contentType)) {
            response.setStatusCode(415).end();
            return;
        }

        String loggerName = request.getFormAttribute(LOGGER_NAME_PARAM);
        String loggerLevel = request.getFormAttribute(LOGGER_LEVEL_PARAM);

        String temporaryEnabled = request.getParam(TEMPORARY_ENABLED);

        if ("true".equalsIgnoreCase(temporaryEnabled)) {
            String loggerDuration = request.getFormAttribute(LOGGER_DURATION);
            handleTemporaryPost(loggerName, loggerLevel, loggerDuration, routingContext, response);
            return;
        }

        if (loggerLevel == null || loggerLevel.isEmpty()) {
            LogController.updateLogLevel(loggerName, null);
        } else {
            LogController.updateLogLevel(loggerName, loggerLevel);
        }

        response.setStatusCode(201).end();
    }

    /**
     * Handles POST requests to update logger levels temporarily. Updates the specified logger to the provided level
     * for a given duration, then restores the original level automatically.
     *
     * @param loggerName The name of the logger to update
     * @param loggerLevel The new temporary log level
     * @param loggerDuration Duration in seconds for which the temporary level should apply
     * @param routingContext The Vert.x routing context
     * @param response The HTTP response
     */
    private void handleTemporaryPost(
            String loggerName,
            String loggerLevel,
            String loggerDuration,
            RoutingContext routingContext,
            HttpServerResponse response) {

        if (loggerLevel == null || !LogController.LEVELS.contains(loggerLevel.toUpperCase())) {
            response.setStatusCode(400).end("{\"error\":\"Invalid logger level\"}");
            return;
        }

        if (!LogController.doesLoggerExist(loggerName)) {
            response.setStatusCode(404).end("{\"error\":\"Logger '" + loggerName + "' not found\"}");
            return;
        }

        int durationSeconds;
        try {
            durationSeconds = Integer.parseInt(loggerDuration);
            if (durationSeconds <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            response.setStatusCode(400).end("{\"error\":\"Duration must be a positive integer\"}");
            return;
        }

        String currentLevel = LogController.getConfiguredLogLevel(LogContext.getLogContext().getLogger(loggerName));
        TemporaryLogState previousState = temporaryStates.get(loggerName);

        if (loggerLevel.equalsIgnoreCase(currentLevel) && previousState == null) {
            response.setStatusCode(200).end("{\"message\":\"Log level already set to " + loggerLevel + "\"}");
            return;
        }

        if (previousState != null) {
            routingContext.vertx().cancelTimer(previousState.timerId);
        }

        String originalLevel = previousState != null ? previousState.originalLevel : currentLevel;

        LogController.updateLogLevel(loggerName, loggerLevel);

        long timerId = routingContext.vertx().setTimer(durationSeconds * 1000L, id -> {
            LogController.updateLogLevel(loggerName, originalLevel);
            temporaryStates.remove(loggerName);
        });

        temporaryStates.put(loggerName, new TemporaryLogState(originalLevel, timerId));

        response.setStatusCode(201)
                .end("{\"message\":\"Temporary log level '" + loggerLevel + "' set for " + durationSeconds + " seconds\"}");
    }
}
