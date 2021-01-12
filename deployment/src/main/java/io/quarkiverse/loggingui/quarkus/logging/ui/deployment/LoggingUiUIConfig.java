package io.quarkiverse.loggingui.quarkus.logging.ui.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class LoggingUiUIConfig {

    /**
     * The path where Logging UI is available.
     * The value `/` is not allowed as it blocks the application from serving anything else.
     */
    @ConfigItem(defaultValue = "/logging-ui")
    String rootPath;

    /**
     * Always include the UI. By default this will only be included in dev and test.
     * Setting this to true will also include the UI in Prod
     */
    @ConfigItem(defaultValue = "false")
    boolean alwaysInclude;

    public String getRootPath() {
        return rootPath;
    }
}
