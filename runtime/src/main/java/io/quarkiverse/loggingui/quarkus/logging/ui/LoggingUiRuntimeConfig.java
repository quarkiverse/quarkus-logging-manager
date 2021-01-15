package io.quarkiverse.loggingui.quarkus.logging.ui;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "logging-ui", phase = ConfigPhase.RUN_TIME)
public class LoggingUiRuntimeConfig {

    /**
     * If Logging UI should be enabled. By default, Logging UI is enabled if it is included (see {@code always-include}).
     */
    @ConfigItem(name = "ui.enable", defaultValue = "true")
    boolean enable;
}
