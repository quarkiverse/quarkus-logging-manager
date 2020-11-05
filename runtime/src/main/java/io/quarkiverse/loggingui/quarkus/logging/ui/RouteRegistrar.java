package io.quarkiverse.loggingui.quarkus.logging.ui;

import static io.quarkiverse.loggingui.quarkus.logging.ui.LoggerHandler.LOGGER_NAME_PATH_PARAM;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.vertx.ext.web.Router;

@ApplicationScoped
public class RouteRegistrar {

    @Inject
    private LoggingUiConfig config;
    @Inject
    private LoggerHandler loggerHandler;

    public void init(@Observes Router router) {
        String effectivePath = generateEffectivePath();

        router.get(effectivePath).blockingHandler(loggerHandler);
        router.get(String.format("%s/:%s", effectivePath, LOGGER_NAME_PATH_PARAM)).blockingHandler(loggerHandler);
        router.post(String.format("%s/:%s", effectivePath, LOGGER_NAME_PATH_PARAM)).blockingHandler(loggerHandler);
    }

    private String generateEffectivePath() {
        String effectivePath = "";

        if (!config.basePath.startsWith("/")) {
            effectivePath += "/";
        }
        effectivePath += config.basePath;

        if (effectivePath.endsWith("/")) {
            effectivePath = effectivePath.substring(0, effectivePath.lastIndexOf("/"));
        }
        return effectivePath;
    }
}
