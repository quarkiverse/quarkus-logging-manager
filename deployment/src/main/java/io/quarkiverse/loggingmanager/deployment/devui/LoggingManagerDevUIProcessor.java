package io.quarkiverse.loggingmanager.deployment.devui;

import java.util.Map;
import java.util.stream.Collectors;

import io.quarkiverse.loggingmanager.LogController;
import io.quarkiverse.loggingmanager.devui.LoggingManagerJsonRpcService;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

public class LoggingManagerDevUIProcessor {
    private static final String LEVEL = "level";

    @BuildStep(onlyIf = IsDevelopment.class)
    public CardPageBuildItem pages() {
        CardPageBuildItem pageBuildItem = new CardPageBuildItem();

        pageBuildItem.addBuildTimeData(LEVEL, LogController.LEVELS
                .stream().map((level) -> Map.of(LEVEL, level))
                .collect(Collectors.toList()));

        pageBuildItem.addPage(Page.tableDataPageBuilder(LEVEL)
                .showColumn(LEVEL)
                .buildTimeDataKey(LEVEL)
                .icon("font-awesome-solid:layer-group"));

        pageBuildItem.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:youtube")
                .componentLink("qwc-logging-manager-loggers.js"));

        return pageBuildItem;
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    JsonRPCProvidersBuildItem createJsonRPCService() {
        return new JsonRPCProvidersBuildItem(LoggingManagerJsonRpcService.class);
    }
}
