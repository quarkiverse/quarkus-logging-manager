package io.quarkiverse.loggingmanager;

import java.util.Objects;

import javax.json.bind.annotation.JsonbTransient;

/**
 * Some information of a logger
 */
public class LoggerInfo implements Comparable<LoggerInfo> {
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LoggerInfo other = (LoggerInfo) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(LoggerInfo o) {
        return this.getName().compareTo(o.getName());
    }

}
