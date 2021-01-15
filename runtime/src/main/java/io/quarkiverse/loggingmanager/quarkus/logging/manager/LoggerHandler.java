package io.quarkiverse.loggingui.quarkus.logging.ui;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class LoggerHandler implements Handler<RoutingContext> {
    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig().withFormatting(true));
    private static final String LOGGER_NAME_PATH_PARAM = "loggerName";

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
        String loggerName = request.getParam(LOGGER_NAME_PATH_PARAM);
        if (loggerName == null || loggerName.isEmpty()) {
            List<LoggerInfo> loggerInfos = getAllLoggerInfos();
            response.end(JSONB.toJson(loggerInfos));
        } else {
            LoggerInfo loggerInfo = getLoggerInfo(loggerName);
            if (loggerInfo != null) {
                response.end(JSONB.toJson(loggerInfo));
            } else {
                response.setStatusCode(404).end();
            }
        }
    }

    private void handlePost(HttpServerRequest request, HttpServerResponse response) {
        request.bodyHandler(buff -> {
            if (buff.length() > 0) {
                String body = new String(buff.getBytes());
                LoggerInfo loggerInfo = JSONB.fromJson(body, LoggerInfo.class);

                Logger logger = Logger.getLogger(loggerInfo.getName());
                if (logger == null) {
                    response.setStatusCode(404).end();
                } else if (loggerInfo.getConfiguredLevel() == null || loggerInfo.getConfiguredLevel().isEmpty()) {
                    logger.setLevel(null);
                    response.setStatusCode(201).end();
                } else {
                    String newLevel = loggerInfo.getConfiguredLevel().toUpperCase(Locale.ROOT);
                    try {
                        Level level = Level.parse(newLevel);
                        logger.setLevel(level);
                        response.setStatusCode(201).end();
                    } catch (IllegalArgumentException iae) {
                        response.setStatusCode(400).end();
                    }
                }
            }
        });

    }

    private List<LoggerInfo> getAllLoggerInfos() {
        List<LoggerInfo> loggerInfos = new ArrayList<>();
        LogManager manager = LogManager.getLogManager();
        Enumeration<String> loggerNames = manager.getLoggerNames();
        while (loggerNames.hasMoreElements()) {
            String loggerName = loggerNames.nextElement();
            LoggerInfo loggerInfo = getLoggerInfo(loggerName);
            if (loggerInfo != null) {
                loggerInfos.add(loggerInfo);
            }
        }

        Collections.sort(loggerInfos);
        return loggerInfos;
    }

    private LoggerInfo getLoggerInfo(String loggerName) {
        if (loggerName != null && !loggerName.isEmpty()) {
            Logger logger = Logger.getLogger(loggerName);
            String configuredLevel = getConfiguredLogLevel(logger);
            String effectiveLevel = getEffectiveLogLevel(logger);
            return new LoggerInfo(loggerName, effectiveLevel, configuredLevel);
        }
        return null;
    }

    private String getConfiguredLogLevel(Logger logger) {
        Level level = logger.getLevel();
        return level != null ? level.getName() : null;
    }

    private String getEffectiveLogLevel(Logger logger) {
        if (logger == null) {
            return null;
        }
        if (logger.getLevel() != null) {
            return logger.getLevel().getName();
        }
        return getEffectiveLogLevel(logger.getParent());
    }
}
