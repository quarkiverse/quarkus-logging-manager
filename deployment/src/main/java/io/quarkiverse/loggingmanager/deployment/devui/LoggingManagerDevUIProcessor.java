package io.quarkiverse.loggingmanager.deployment.devui;

import java.util.Map;
import java.util.stream.Collectors;

import io.quarkiverse.loggingmanager.LogController;
import io.quarkiverse.loggingmanager.devui.LoggingManagerJsonRpcService;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

public class LoggingManagerDevUIProcessor {
    private static final String LEVEL = "level";

    @BuildStep(onlyIf = IsDevelopment.class)
    public void pages(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer) {
        CardPageBuildItem card = new CardPageBuildItem();

        card.addBuildTimeData(LEVEL, LogController.LEVELS
                .stream().map((level) -> Map.of(LEVEL, level))
                .collect(Collectors.toList()));

        card.addPage(Page.tableDataPageBuilder("Level")
                .showColumn(LEVEL)
                .buildTimeDataKey(LEVEL)
                .icon("font-awesome-solid:layer-group"));

        card.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:play")
                .componentLink("qwc-logging-manager-loggers.js"));

        card.setCustomCard("qwc-logging-manager-card.js");
        cardPageBuildItemBuildProducer.produce(card);
    }

    @BuildStep
    JsonRPCProvidersBuildItem createJsonRPCService() {
        return new JsonRPCProvidersBuildItem(LoggingManagerJsonRpcService.class);
    }
}