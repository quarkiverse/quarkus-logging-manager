package io.quarkiverse.loggingmanager.quarkus.logging.manager.deployment;

import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot
public class LoggingUiConfig {

    /**
     * The base path, defaults to /loggers
     */
    @ConfigItem(defaultValue = "/loggers")
    String basePath;

    /**
     * Whether or not to include the Logger UI endpoints in the generated OpenAPI document
     */
    @ConfigItem(name = "openapi.included", defaultValue = "false")
    boolean openapiIncluded;

    /**
     * The tag to use if OpenAPI is included
     */
    @ConfigItem(defaultValue = "Loggers")
    String openapiTag;

    /**
     * UI configuration
     */
    @ConfigItem
    @ConfigDocSection
    LoggingUiUIConfig ui;
}
