package io.quarkiverse.loggingui.quarkus.logging.ui;

import static io.quarkus.runtime.annotations.ConfigPhase.RUN_TIME;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = RUN_TIME)
public class LoggingUiConfig {

    /**
     * The base path, defaults to /loggers
     */
    @ConfigItem(defaultValue = "/loggers")
    String basePath;
}
