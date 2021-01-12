package io.quarkiverse.loggingui.quarkus.logging.ui.deployment.devconsole;

import java.util.Optional;

import io.quarkiverse.loggingui.quarkus.logging.ui.deployment.LoggingUiConfig;
import io.quarkiverse.loggingui.quarkus.logging.ui.deployment.LoggingUiOpenAPIFilter;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devconsole.spi.DevConsoleTemplateInfoBuildItem;
import io.quarkus.swaggerui.spi.SwaggerUiBuildItem;

public class DevConsoleProcessor {
    @BuildStep(onlyIf = IsDevelopment.class)
    public DevConsoleTemplateInfoBuildItem loggersPath(LoggingUiConfig loggingUiConfig) {
        return new DevConsoleTemplateInfoBuildItem("loggersPath", loggingUiConfig.getUi().getRootPath() + "/index.html");
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    public DevConsoleTemplateInfoBuildItem swaggerUiPath(Optional<SwaggerUiBuildItem> swaggerUiBuildItemOptional) {
        return swaggerUiBuildItemOptional
                .map(SwaggerUiBuildItem::getSwaggerUiPath)
                .map(swaggerUiDestination -> swaggerUiDestination + "/#/" + LoggingUiOpenAPIFilter.LOGGING_UI_TAG.get(0))
                .map(path -> new DevConsoleTemplateInfoBuildItem("swaggerUiPath", path))
                .orElse(new DevConsoleTemplateInfoBuildItem("swaggerUiPath", null));
    }
}
