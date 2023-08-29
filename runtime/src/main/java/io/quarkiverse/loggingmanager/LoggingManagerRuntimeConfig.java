package io.quarkiverse.loggingmanager;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "logging-manager", phase = ConfigPhase.RUN_TIME)
public class LoggingManagerRuntimeConfig {

    /**
     * If Logging Manager should be enabled. By default, Logging Manager is enabled if it is included (see
     * {@code always-include}).
     */
    @ConfigItem(name = "enable", defaultValue = "true")
    boolean enable;
}
