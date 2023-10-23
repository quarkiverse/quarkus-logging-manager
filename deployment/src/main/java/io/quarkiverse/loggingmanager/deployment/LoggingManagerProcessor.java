package io.quarkiverse.loggingmanager.deployment;

import java.util.function.BooleanSupplier;

import io.quarkiverse.loggingmanager.LoggerManagerRecorder;
import io.quarkiverse.loggingmanager.LoggingManagerRuntimeConfig;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.smallrye.openapi.deployment.spi.AddToOpenAPIDefinitionBuildItem;
import io.quarkus.vertx.http.deployment.BodyHandlerBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem.Builder;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.runtime.management.ManagementInterfaceBuildTimeConfig;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class LoggingManagerProcessor {
    private static final String FEATURE = "logging-manager";

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

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void includeRestEndpoints(BuildProducer<RouteBuildItem> routeProducer,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            LoggingManagerConfig loggingManagerConfig,
            BodyHandlerBuildItem bodyHandlerBuildItem,
            LoggerManagerRecorder recorder,
            LaunchModeBuildItem launchMode,
            LoggingManagerRuntimeConfig runtimeConfig,
            ManagementInterfaceBuildTimeConfig managementConfig) {

        if ("/".equals(loggingManagerConfig.basePath)) {
            throw new ConfigurationException(
                    "quarkus.logging-manager.base-path was set to \"/\", this is not allowed as it blocks the application from serving anything else.");
        }

        if (shouldInclude(launchMode, loggingManagerConfig)) {
            Handler<RoutingContext> loggerHandler = recorder.loggerHandler();
            Handler<RoutingContext> levelHandler = recorder.levelHandler();

            Builder loggerBuilder = nonApplicationRootPathBuildItem.routeBuilder();
            Builder levelBuilder = nonApplicationRootPathBuildItem.routeBuilder();

            if (managementConfig.enabled) {
                loggerBuilder.management();
                levelBuilder.management();
            }

            loggerBuilder = loggerBuilder
                    .routeFunction(loggingManagerConfig.basePath,
                                   recorder.routeConsumer(bodyHandlerBuildItem.getHandler(), runtimeConfig))
                    .displayOnNotFoundPage("All available loggers")
                    .handler(loggerHandler);

            routeProducer.produce(loggerBuilder.build());

            levelBuilder = levelBuilder
                    .nestedRoute(loggingManagerConfig.basePath, "levels")
                    .displayOnNotFoundPage("All available log levels")
                    .handler(levelHandler);

            routeProducer.produce(levelBuilder.build());
        }
    }

    @BuildStep(onlyIf = OpenAPIIncluded.class)
    public void includeInOpenAPIEndpoint(BuildProducer<AddToOpenAPIDefinitionBuildItem> openAPIProducer,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            Capabilities capabilities,
            LaunchModeBuildItem launchMode,
            LoggingManagerConfig loggingManagerConfig,
            ManagementInterfaceBuildTimeConfig managementConfig) {

        // Add to OpenAPI if OpenAPI is available
        if (capabilities.isPresent(Capability.SMALLRYE_OPENAPI) && shouldInclude(launchMode, loggingManagerConfig, managementConfig)) {
        if (capabilities.isPresent(Capability.SMALLRYE_OPENAPI) && shouldInclude(launchMode, loggingManagerConfig, managementConfig)) {
            LoggingManagerOpenAPIFilter filter = new LoggingManagerOpenAPIFilter(
                    nonApplicationRootPathBuildItem.resolvePath(loggingManagerConfig.basePath),
                    loggingManagerConfig.openapiTag);
            openAPIProducer.produce(new AddToOpenAPIDefinitionBuildItem(filter));
        }
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, LoggingManagerConfig loggingManagerConfig) {
        return launchMode.getLaunchMode().isDevOrTest() || loggingManagerConfig.alwaysInclude;
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, LoggingManagerConfig loggingManagerConfig, ManagementInterfaceBuildTimeConfig managementConfig) {
        return !managementConfig.enabled && shouldInclude(launchMode, loggingManagerConfig);
    }

}
