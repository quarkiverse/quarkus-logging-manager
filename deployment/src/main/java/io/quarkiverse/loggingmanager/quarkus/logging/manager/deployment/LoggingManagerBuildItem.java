package io.quarkiverse.loggingmanager.quarkus.logging.manager.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

final class LoggingUiBuildItem extends SimpleBuildItem {

    private final String loggingUiFinalDestination;
    private final String loggingUiPath;

    public LoggingUiBuildItem(String loggingUiFinalDestination, String loggingUiPath) {
        this.loggingUiFinalDestination = loggingUiFinalDestination;
        this.loggingUiPath = loggingUiPath;
    }

    public String getLoggingUiFinalDestination() {
        return loggingUiFinalDestination;
    }

    public String getLoggingUiPath() {
        return loggingUiPath;
    }
}