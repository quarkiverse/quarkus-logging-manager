package io.quarkiverse.loggingmanager.deployment;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "quarkus.logging-manager")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface LoggingManagerConfig {

    /**
     * The base path
     */
    @WithDefault("logging-manager")
    String basePath();

    /**
     * Whether to include the Logger Manager endpoints in the generated OpenAPI document
     */
    @WithName("openapi.included")
    @WithDefault("false")
    boolean openapiIncluded();

    /**
     * The tag to use if OpenAPI is included
     */
    @WithDefault("logging-manager")
    String openapiTag();

    /**
     * Always include this. By default, this will always be included.
     * Setting this to false will also exclude this in Prod
     */
    @WithDefault("true")
    boolean alwaysInclude();
}
