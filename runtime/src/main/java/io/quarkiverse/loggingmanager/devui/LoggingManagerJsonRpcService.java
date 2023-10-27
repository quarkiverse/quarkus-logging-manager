package io.quarkiverse.loggingmanager.devui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.Logger;

import io.quarkiverse.loggingmanager.LogController;
import io.smallrye.common.annotation.NonBlocking;

public class LoggingManagerJsonRpcService {
    @NonBlocking
    public List<LoggerInfo> getLoggers() {
        List<LoggerInfo> infoList = new ArrayList<>();
        LogContext logContext = LogContext.getLogContext();
        Enumeration<String> loggerNames = logContext.getLoggerNames();
        while (loggerNames.hasMoreElements()) {
            infoList.add(getLoggerInfo(logContext, loggerNames.nextElement()));
        }
        return infoList;
    }

    private static LoggerInfo getLoggerInfo(LogContext logContext, String loggerName) {
        Logger logger = logContext.getLogger(loggerName);
        String effectiveLevel = LogController.getEffectiveLogLevel(logger);
        String configuredLevel = LogController.getConfiguredLogLevel(logger);
        return new LoggerInfo(loggerName, effectiveLevel, configuredLevel);
    }

    @NonBlocking
    public void updateLogger(String loggerName, String logLevel) {
        LogController.updateLogLevel(loggerName, logLevel);
    }

    public static class LoggerInfo {
        private final String name;
        private final String effectiveLevel;
        private final String configuredLevel;

        LoggerInfo(String name, String effectiveLevel, String configuredLevel) {
            this.name = name;
            this.effectiveLevel = effectiveLevel;
            this.configuredLevel = configuredLevel;
        }

        public String getName() {
            return this.name;
        }

        public String getEffectiveLevel() {
            return this.effectiveLevel;
        }

        public String getConfiguredLevel() {
            return configuredLevel;
        }
    }
}
