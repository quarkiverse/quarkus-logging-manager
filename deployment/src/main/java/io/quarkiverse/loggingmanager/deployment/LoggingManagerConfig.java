package io.quarkiverse.loggingmanager.deployment;

import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot
public class LoggingManagerConfig {

    /**
     * The base path
     */
    @ConfigItem(defaultValue = "logging-manager")
    String basePath;

    /**
     * Whether or not to include the Logger Manager endpoints in the generated OpenAPI document
     */
    @ConfigItem(name = "openapi.included", defaultValue = "false")
    boolean openapiIncluded;

    /**
     * The tag to use if OpenAPI is included
     */
    @ConfigItem(defaultValue = "Logging-manager")
    String openapiTag;

    /**
     * Always include this. By default this will always be included.
     * Setting this to false will also exclude this in Prod
     */
    @ConfigItem(defaultValue = "true")
    boolean alwaysInclude;

    /**
     * UI configuration
     */
    @ConfigItem
    @ConfigDocSection
    LoggingManagerUIConfig ui;
}
