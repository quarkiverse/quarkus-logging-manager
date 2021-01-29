package io.quarkiverse.loggingmanager.deployment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BooleanSupplier;

import io.quarkiverse.loggingmanager.LoggerManagerRecorder;
import io.quarkiverse.loggingmanager.LoggingManagerRuntimeConfig;
import io.quarkiverse.loggingmanager.stream.LogstreamSocket;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
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
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.deployment.devmode.NotFoundPageDisplayableEndpointBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class LoggingManagerProcessor {
    private static final String FEATURE = "logging-manager";

    // For the UI
    private static final String UI_WEBJAR_GROUP_ID = "io.quarkiverse.loggingmanager";
    private static final String UI_WEBJAR_ARTIFACT_ID = "quarkus-logging-manager-ui";
    private static final String UI_WEBJAR_PREFIX = "META-INF/resources/logging-manager/";
    private static final String UI_FINAL_DESTINATION = "META-INF/logging-manager-files";
    private static final String FILE_TO_UPDATE = "loggingmanager.js";

    static class OpenAPIIncluded implements BooleanSupplier {
        LoggingManagerConfig config;

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
    void includeRestEndpoints(BuildProducer<RouteBuildItem> routeProducer,
            BuildProducer<NotFoundPageDisplayableEndpointBuildItem> displayableEndpoints,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            LoggingManagerConfig loggingManagerConfig,
            LoggerManagerRecorder recorder) {

        Handler<RoutingContext> loggerHandler = recorder.loggerHandler();
        Handler<RoutingContext> levelHandler = recorder.levelHandler();

        String basePath = nonApplicationRootPathBuildItem.adjustPath(loggingManagerConfig.basePath);

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
            LoggingManagerConfig loggingManagerConfig) {

        // Add to OpenAPI if OpenAPI is available
        if (capabilities.isPresent(Capability.SMALLRYE_OPENAPI)) {
            LoggingManagerOpenAPIFilter filter = new LoggingManagerOpenAPIFilter(
                    nonApplicationRootPathBuildItem.adjustPath(loggingManagerConfig.basePath), loggingManagerConfig.openapiTag);
            openAPIProducer.produce(new AddToOpenAPIDefinitionBuildItem(filter));
        }
    }

    @BuildStep
    void registerManagerExtension(
            BuildProducer<AdditionalBeanBuildItem> annotatedProducer,
            BuildProducer<NotFoundPageDisplayableEndpointBuildItem> notFoundPageDisplayableEndpointProducer,
            BuildProducer<GeneratedResourceBuildItem> generatedResourceProducer,
            BuildProducer<NativeImageResourceBuildItem> nativeImageResourceProducer,
            BuildProducer<LoggingManagerBuildItem> loggingManagerBuildProducer,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            CurateOutcomeBuildItem curateOutcomeBuildItem,
            LaunchModeBuildItem launchMode,
            LoggingManagerConfig loggingManagerConfig) throws Exception {

        if (shouldInclude(launchMode, loggingManagerConfig)) {
            // Make sure the WebSocket gets included.
            annotatedProducer.produce(AdditionalBeanBuildItem.unremovableOf(LogstreamSocket.class));

            // Add the UI
            if ("/".equals(loggingManagerConfig.ui.rootPath)) {
                throw new ConfigurationError(
                        "quarkus.logging-manager.ui.root-path was set to \"/\", this is not allowed as it blocks the application from serving anything else.");
            }

            String loggersPath = nonApplicationRootPathBuildItem.adjustPath(loggingManagerConfig.basePath);
            String logStreamPath = nonApplicationRootPathBuildItem.adjustPath(loggingManagerConfig.ui.streamPath);

            AppArtifact artifact = WebJarUtil.getAppArtifact(curateOutcomeBuildItem, UI_WEBJAR_GROUP_ID,
                    UI_WEBJAR_ARTIFACT_ID);

            if (launchMode.getLaunchMode().isDevOrTest()) {
                Path tempPath = WebJarUtil.copyResourcesForDevOrTest(curateOutcomeBuildItem, launchMode, artifact,
                        UI_WEBJAR_PREFIX, false);
                updateApiUrl(tempPath.resolve(FILE_TO_UPDATE), loggersPath, logStreamPath);

                loggingManagerBuildProducer
                        .produce(new LoggingManagerBuildItem(tempPath.toAbsolutePath().toString(),
                                loggingManagerConfig.ui.rootPath));

                notFoundPageDisplayableEndpointProducer
                        .produce(new NotFoundPageDisplayableEndpointBuildItem(
                                nonApplicationRootPathBuildItem
                                        .adjustPath(loggingManagerConfig.ui.rootPath + "/"),
                                "Quarkus Log viewer"));
            } else {
                Map<String, byte[]> files = WebJarUtil.copyResourcesForProduction(curateOutcomeBuildItem, artifact,
                        UI_WEBJAR_PREFIX, false);

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

                loggingManagerBuildProducer
                        .produce(new LoggingManagerBuildItem(UI_FINAL_DESTINATION, loggingManagerConfig.ui.rootPath));
            }
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerLoggingManagerHandler(
            BuildProducer<RouteBuildItem> routeProducer,
            LoggerManagerRecorder recorder,
            LoggingManagerRuntimeConfig runtimeConfig,
            LoggingManagerBuildItem loggingManagerBuildItem,
            LaunchModeBuildItem launchMode,
            LoggingManagerConfig loggingConfig) throws Exception {

        if (shouldInclude(launchMode, loggingConfig)) {
            Handler<RoutingContext> handler = recorder.uiHandler(loggingManagerBuildItem.getLoggingManagerFinalDestination(),
                    loggingManagerBuildItem.getLoggingManagerPath(), runtimeConfig);
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

    private void updateApiUrl(Path loggingManagerJs, String loggingPath, String logStreamPath) throws IOException {
        String content = new String(Files.readAllBytes(loggingManagerJs), StandardCharsets.UTF_8);
        String result = updateApiUrl(content, loggingPath);
        result = updateStreamUrl(result, logStreamPath);
        if (result != null) {
            Files.write(loggingManagerJs, result.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String updateApiUrl(String original, String loggingPath) {
        return original.replace("loggersUrl = \"hereTheApiUrl\";", "loggersUrl = \"" + loggingPath + "\";");
    }

    public String updateStreamUrl(String original, String logstreamUrl) {
        return original.replace("logstreamUrl = \"hereTheStreamUrl\";", "logstreamUrl = \"" + logstreamUrl + "\";");
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, LoggingManagerConfig loggingManagerConfig) {
        return launchMode.getLaunchMode().isDevOrTest() || loggingManagerConfig.ui.alwaysInclude;
    }
}
