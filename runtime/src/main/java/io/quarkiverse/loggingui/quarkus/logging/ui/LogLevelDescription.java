package io.quarkiverse.loggingui.quarkus.logging.ui;

public class LogLevelDescription {
    private String effectiveLevel;
    private String configuredLevel;

    LogLevelDescription() {
        // Needed for deserialization
    }

    public LogLevelDescription(String configuredLevel, String effectiveLevel) {
        this.effectiveLevel = effectiveLevel;
        this.configuredLevel = configuredLevel;
    }

    public String getEffectiveLevel() {
        return effectiveLevel;
    }

    public String getConfiguredLevel() {
        return configuredLevel;
    }
}
