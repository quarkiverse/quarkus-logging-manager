package io.quarkiverse.loggingui.quarkus.logging.ui.deployment;

import java.util.function.BooleanSupplier;

import io.quarkiverse.loggingui.quarkus.logging.ui.LoggerUiRecorder;
import io.quarkiverse.loggingui.quarkus.logging.ui.stream.LogstreamSocket;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.smallrye.openapi.deployment.spi.AddToOpenAPIDefinitionBuildItem;
import io.quarkus.undertow.websockets.deployment.AnnotatedWebsocketEndpointBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class LoggingUiProcessor {
    private static final String FEATURE = "quarkus-logging-ui";

    static class OpenAPIIncluded implements BooleanSupplier {
        LoggingUiConfig config;

        public boolean getAsBoolean() {
            return config.openapiIncluded;
        }
    }

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    void buildExecutionService(BuildProducer<RouteBuildItem> routeProducer,
            LoggingUiConfig loggingUiConfig,
            LoggerUiRecorder recorder) {

        Handler<RoutingContext> loggerHandler = recorder.loggerHandler();
        Handler<RoutingContext> levelHandler = recorder.levelHandler();

        routeProducer.produce(new RouteBuildItem.Builder().nonApplicationRoute().route(loggingUiConfig.basePath)
                .handler(loggerHandler).build());
        routeProducer.produce(new RouteBuildItem.Builder().nonApplicationRoute().route(loggingUiConfig.basePath + "/levels")
                .handler(levelHandler).build());
    }

    @BuildStep(onlyIf = OpenAPIIncluded.class)
    public void includeInOpenAPIEndpoint(BuildProducer<AddToOpenAPIDefinitionBuildItem> openAPIProducer,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            Capabilities capabilities,
            LoggingUiConfig loggingUiConfig) {

        // Add to OpenAPI if OpenAPI is available
        if (capabilities.isPresent(Capability.SMALLRYE_OPENAPI)) {
            LoggingUiOpenAPIFilter filter = new LoggingUiOpenAPIFilter(
                    nonApplicationRootPathBuildItem.adjustPath(loggingUiConfig.basePath));
            openAPIProducer.produce(new AddToOpenAPIDefinitionBuildItem(filter));
        }
    }

    @BuildStep
    void registerUiHandler(
            BuildProducer<AnnotatedWebsocketEndpointBuildItem> annotatedProducer,
            LaunchModeBuildItem launchMode,
            LoggingUiConfig loggingUiConfig) throws Exception {

        if (shouldInclude(launchMode, loggingUiConfig)) {
            annotatedProducer.produce(new AnnotatedWebsocketEndpointBuildItem(LogstreamSocket.class.getName(), false));
        }
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, LoggingUiConfig loggingUiConfig) {
        return launchMode.getLaunchMode().isDevOrTest() || loggingUiConfig.ui.alwaysInclude;
    }
}
