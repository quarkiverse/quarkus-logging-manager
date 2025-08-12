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

    private final ConcurrentHashMap<String, Long> activeTimers = new ConcurrentHashMap<>();


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

    /**
     * Handles POST requests to update logger levels.
     * Updates the specified logger with the provided level or removes the level if none specified.
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
        String loggerDuration = request.getFormAttribute(LOGGER_DURATION);

        if (loggerDuration != null && !loggerDuration.isEmpty()) {
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

    private void handleTemporaryPost(
            String loggerName,
            String loggerLevel,
            String loggerDuration,
            RoutingContext routingContext,
            HttpServerResponse response) {

        if (loggerLevel == null || !LogController.LEVELS.contains(loggerLevel.toUpperCase())) {
            response.setStatusCode(400).end("Invalid Logger level");
            return;
        }

        if (!LogController.doesLoggerExist(loggerName)) {
            response.setStatusCode(404).end("Logger '" + loggerName + "' not found.");
            return;
        }

        int durationSeconds;

        try {
            durationSeconds = Integer.parseInt(loggerDuration);
        } catch (NumberFormatException e) {
            response.setStatusCode(400).end("Invalid duration");
            return;
        }

        if (durationSeconds <= 0) {
            response.setStatusCode(400).end("Duration must be positive");
            return;
        }

        String oldLevel = LogController.getConfiguredLogLevel(
                LogContext.getLogContext().getLogger(loggerName));

        if (oldLevel != null && oldLevel.equalsIgnoreCase(loggerLevel)) {
            response.setStatusCode(200).end("Log level already set to " + loggerLevel);
            return;
        }

        LogController.updateLogLevel(loggerName, loggerLevel);

        Long previousTimerId = activeTimers.put(loggerName, null);
        if (previousTimerId != null) {
            routingContext.vertx().cancelTimer(previousTimerId);
        }

        long timerId = routingContext.vertx().setTimer(durationSeconds * 1000L, id -> {
            LogController.updateLogLevel(loggerName, oldLevel);
            activeTimers.remove(loggerName, id);
        });

        activeTimers.put(loggerName, timerId);
        response.setStatusCode(201).end("Temporary log level set for " + durationSeconds + " seconds");
    }
}
