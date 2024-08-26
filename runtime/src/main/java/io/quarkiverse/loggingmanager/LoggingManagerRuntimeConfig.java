package io.quarkiverse.loggingmanager;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.logging-manager")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface LoggingManagerRuntimeConfig {

    /**
     * If Logging Manager should be enabled. By default, Logging Manager is enabled if it is included (see
     * {@code always-include}).
     */
    @WithDefault("true")
    boolean enable();
}
