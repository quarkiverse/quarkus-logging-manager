package io.quarkiverse.loggingui.quarkus.logging.ui.deployment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BooleanSupplier;

import io.quarkiverse.loggingui.quarkus.logging.ui.LoggerUiRecorder;
import io.quarkiverse.loggingui.quarkus.logging.ui.LoggingUiRuntimeConfig;
import io.quarkiverse.loggingui.quarkus.logging.ui.stream.LogstreamSocket;
import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.configuration.ConfigurationError;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.deployment.util.WebJarUtil;
import io.quarkus.smallrye.openapi.deployment.spi.AddToOpenAPIDefinitionBuildItem;
import io.quarkus.undertow.websockets.deployment.AnnotatedWebsocketEndpointBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.deployment.devmode.NotFoundPageDisplayableEndpointBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class LoggingUiProcessor {
    private static final String FEATURE = "quarkus-logging-ui";

    // For the UI
    private static final String UI_WEBJAR_GROUP_ID = "io.quarkiverse.loggingui";
    private static final String UI_WEBJAR_ARTIFACT_ID = "quarkus-logging-ui-ui";
    private static final String UI_WEBJAR_PREFIX = "META-INF/resources/logging-ui/";
    private static final String UI_FINAL_DESTINATION = "META-INF/logging-ui-files";
    private static final String FILE_TO_UPDATE = "loggingui.js";

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
    void incluceRestEndpoints(BuildProducer<RouteBuildItem> routeProducer,
            BuildProducer<NotFoundPageDisplayableEndpointBuildItem> displayableEndpoints,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            LoggingUiConfig loggingUiConfig,
            LoggerUiRecorder recorder) {

        Handler<RoutingContext> loggerHandler = recorder.loggerHandler();
        Handler<RoutingContext> levelHandler = recorder.levelHandler();

        String basePath = nonApplicationRootPathBuildItem.adjustPath(loggingUiConfig.basePath);

        routeProducer.produce(new RouteBuildItem.Builder().route(basePath)
                .handler(loggerHandler).build());
        routeProducer.produce(new RouteBuildItem.Builder().route(basePath + "/levels")
                .handler(levelHandler).build());

        displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(basePath + "/", "All available loggers"));
        displayableEndpoints
                .produce(new NotFoundPageDisplayableEndpointBuildItem(basePath + "/levels/", "All available log levels"));
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
    void registerUiExtension(
            BuildProducer<AnnotatedWebsocketEndpointBuildItem> annotatedProducer,
            BuildProducer<NotFoundPageDisplayableEndpointBuildItem> notFoundPageDisplayableEndpointProducer,
            BuildProducer<GeneratedResourceBuildItem> generatedResourceProducer,
            BuildProducer<NativeImageResourceBuildItem> nativeImageResourceProducer,
            BuildProducer<LoggingUiBuildItem> loggingUiBuildProducer,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            CurateOutcomeBuildItem curateOutcomeBuildItem,
            LaunchModeBuildItem launchMode,
            LoggingUiConfig loggingUiConfig) throws Exception {

        if (shouldInclude(launchMode, loggingUiConfig)) {
            // Make sure the WebSocket gets included.
            annotatedProducer.produce(new AnnotatedWebsocketEndpointBuildItem(LogstreamSocket.class.getName(), false));

            // Add the UI
            if ("/".equals(loggingUiConfig.ui.rootPath)) {
                throw new ConfigurationError(
                        "quarkus.logging-ui.ui.root-path was set to \"/\", this is not allowed as it blocks the application from serving anything else.");
            }

            String loggersPath = nonApplicationRootPathBuildItem.adjustPath(loggingUiConfig.basePath);

            AppArtifact artifact = WebJarUtil.getAppArtifact(curateOutcomeBuildItem, UI_WEBJAR_GROUP_ID,
                    UI_WEBJAR_ARTIFACT_ID);

            if (launchMode.getLaunchMode().isDevOrTest()) {
                Path tempPath = WebJarUtil.copyResourcesForDevOrTest(curateOutcomeBuildItem, launchMode, artifact,
                        UI_WEBJAR_PREFIX);
                updateApiUrl(tempPath.resolve(FILE_TO_UPDATE), loggersPath);

                loggingUiBuildProducer
                        .produce(new LoggingUiBuildItem(tempPath.toAbsolutePath().toString(), loggingUiConfig.ui.rootPath));

                notFoundPageDisplayableEndpointProducer
                        .produce(new NotFoundPageDisplayableEndpointBuildItem(
                                nonApplicationRootPathBuildItem
                                        .adjustPath(loggingUiConfig.ui.rootPath + "/"),
                                "Quarkus Log viewer"));
            } else {
                Map<String, byte[]> files = WebJarUtil.copyResourcesForProduction(curateOutcomeBuildItem, artifact,
                        UI_WEBJAR_PREFIX);

                for (Map.Entry<String, byte[]> file : files.entrySet()) {

                    String fileName = file.getKey();
                    byte[] content = file.getValue();
                    if (fileName.endsWith(FILE_TO_UPDATE)) {
                        content = updateApiUrl(new String(content, StandardCharsets.UTF_8), loggersPath)
                                .getBytes(StandardCharsets.UTF_8);
                    }
                    fileName = UI_FINAL_DESTINATION + "/" + fileName;

                    generatedResourceProducer.produce(new GeneratedResourceBuildItem(fileName, content));
                    nativeImageResourceProducer.produce(new NativeImageResourceBuildItem(fileName));
                }

                loggingUiBuildProducer.produce(new LoggingUiBuildItem(UI_FINAL_DESTINATION, loggingUiConfig.ui.rootPath));
            }
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerLoggingUiHandler(
            BuildProducer<RouteBuildItem> routeProducer,
            LoggerUiRecorder recorder,
            LoggingUiRuntimeConfig runtimeConfig,
            LoggingUiBuildItem loggingUiBuildItem,
            LaunchModeBuildItem launchMode,
            LoggingUiConfig loggingConfig) throws Exception {

        if (shouldInclude(launchMode, loggingConfig)) {
            Handler<RoutingContext> handler = recorder.uiHandler(loggingUiBuildItem.getLoggingUiFinalDestination(),
                    loggingUiBuildItem.getLoggingUiPath(), runtimeConfig);
            routeProducer.produce(new RouteBuildItem.Builder()
                    .route(loggingConfig.ui.rootPath)
                    .handler(handler)
                    .nonApplicationRoute()
                    .build());
            routeProducer.produce(new RouteBuildItem.Builder()
                    .route(loggingConfig.ui.rootPath + "/*")
                    .handler(handler)
                    .nonApplicationRoute()
                    .build());
        }
    }

    private void updateApiUrl(Path loggingUiJs, String loggingPath) throws IOException {
        String content = new String(Files.readAllBytes(loggingUiJs), StandardCharsets.UTF_8);
        String result = updateApiUrl(content, loggingPath);
        if (result != null) {
            Files.write(loggingUiJs, result.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String updateApiUrl(String original, String healthPath) {
        return original.replace("loggersUrl = \"/loggers\";", "loggersUrl = \"" + healthPath + "\";");
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, LoggingUiConfig loggingUiConfig) {
        return launchMode.getLaunchMode().isDevOrTest() || loggingUiConfig.ui.alwaysInclude;
    }
}
