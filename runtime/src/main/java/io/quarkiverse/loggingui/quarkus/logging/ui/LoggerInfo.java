package io.quarkiverse.loggingui.quarkus.logging.ui;

import javax.json.bind.annotation.JsonbTransient;

/**
 * Some information of a logger
 */
public class LoggerInfo {
    private String name;
    private String effectiveLevel;
    private String configuredLevel;

    public LoggerInfo() {
    }

    public LoggerInfo(String name, String effectiveLevel, String configuredLevel) {
        this.name = name;
        this.effectiveLevel = effectiveLevel;
        this.configuredLevel = configuredLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEffectiveLevel() {
        return effectiveLevel;
    }

    @JsonbTransient
    public void setEffectiveLevel(String effectiveLevel) {
        this.effectiveLevel = effectiveLevel;
    }

    public String getConfiguredLevel() {
        return configuredLevel;
    }

    public void setConfiguredLevel(String configuredLevel) {
        this.configuredLevel = configuredLevel;
    }

    @Override
    public String toString() {
        return "LoggerInfo{" + "name=" + name + ", effectiveLevel=" + effectiveLevel + ", configuredLevel=" + configuredLevel
                + '}';
    }
}
