package io.quarkiverse.loggingui.quarkus.logging.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static java.util.logging.Level.ALL;
import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

public class LoggerHandler implements Handler<RoutingContext> {

    public static final String LOGGER_NAME_PATH_PARAM = "loggerName";
    private ObjectMapper objectMapper;

    private static final String[] standardLevels = {
            OFF.getName(), SEVERE.getName(), WARNING.getName(), INFO.getName(), CONFIG.getName(), FINE.getName(),
            FINER.getName(), FINEST.getName(), ALL.getName()
    };

    public LoggerHandler() {
        this.objectMapper =ObjectMapperProducer.get();
    }

    @Override
    public void handle(RoutingContext routingContext) {

        HttpServerRequest request = routingContext.request();
        HttpMethod method = request.method();

        HttpServerResponse response = routingContext.response();
        response.headers().add("Content-Type", "application/json");
        String loggerName = request.getParam(LOGGER_NAME_PATH_PARAM);

        if (GET == method) {
            String result = null;
            try {
                result = objectMapper.writeValueAsString(buildLoggerGetResponse(loggerName));
            } catch (JsonProcessingException e) {
                response.setStatusCode(503).end(result);
            }
            response.end(result);
        }

        if (POST == method) {
            routingContext.request().bodyHandler(buff -> {
                if (buff.length() > 0) {
                    ByteBufInputStream in = new ByteBufInputStream(buff.getByteBuf());
                    try {
                        LogLevelDescription newLogLevel = objectMapper.readValue((InputStream) in, LogLevelDescription.class);
                        setLoggerLevel(loggerName, newLogLevel);
                        response.setStatusCode(204).end();
                    } catch (IllegalArgumentException e) {
                        response.setStatusCode(400).setStatusMessage("Invalid Log level").end();
                    } catch (IOException e) {
                        response.setStatusCode(500).end();
                    }
                }
            });

        }

    }

    private void setLoggerLevel(String loggerName, LogLevelDescription newLogLevel) {
        if (newLogLevel.getConfiguredLevel() == null) {
            throw new IllegalArgumentException("Log leve must not be null");
        }
        Logger logger = Logger.getLogger(loggerName);
        String upperCaseConfiguredLevel = newLogLevel.getConfiguredLevel().toUpperCase(Locale.ROOT);
        logger.setLevel(Level.parse(upperCaseConfiguredLevel));
    }

    public Map<String, Object> buildLoggerGetResponse(String loggerName) {
        Map<String, LogLevelDescription> loggers = isNullOrEmpty(loggerName) ? getAllLoggersInformation()
                : getLoggerInformation(loggerName);

        Map<String, Object> response = new HashMap<>();
        response.put("levels", standardLevels);
        response.put("loggers", loggers);
        return response;
    }

    private Map<String, LogLevelDescription> getLoggerInformation(String loggerName) {
        Map<String, LogLevelDescription> levelByLogger = new HashMap<>();
        Logger logger = Logger.getLogger(loggerName);
        LogLevelDescription logLevelDescription = getLoggerByName(logger);

        levelByLogger.put(logger.getName(), logLevelDescription);
        return levelByLogger;
    }

    private Map<String, LogLevelDescription> getAllLoggersInformation() {
        Map<String, LogLevelDescription> levelByLogger = new HashMap<>();
        LogManager manager = LogManager.getLogManager();
        Enumeration<String> loggerNames = manager.getLoggerNames();
        while (loggerNames.hasMoreElements()) {
            String loggerName = loggerNames.nextElement();

            if (isNullOrEmpty(loggerName)) {
                continue;
            }
            Logger logger = Logger.getLogger(loggerName);
            LogLevelDescription logLevelDescription = getLoggerByName(logger);

            levelByLogger.put(logger.getName(), logLevelDescription);
        }
        return levelByLogger;
    }

    private LogLevelDescription getLoggerByName(Logger logger) {
        String effectiveLogLevel = getEffectiveLogLevel(logger);
        String configuredLogLevel = getConfiguredLogLevel(logger.getLevel());

        return new LogLevelDescription(configuredLogLevel, effectiveLogLevel);

    }

    private String getConfiguredLogLevel(Level level) {
        return level != null ? level.getName() : null;
    }

    private String getEffectiveLogLevel(Logger logger) {
        String effectiveLogLevel = null;
        Logger currentLogger = logger;
        while (effectiveLogLevel == null) {
            if (currentLogger.getLevel() != null) {
                effectiveLogLevel = currentLogger.getLevel().getName();
            } else {
                currentLogger = currentLogger.getParent();
            }
        }
        return effectiveLogLevel;
    }
}
