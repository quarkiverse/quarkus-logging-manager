package io.quarkiverse.loggingmanager.deployment;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.tags.Tag;

import io.quarkiverse.loggingmanager.LogController;

/**
 * Create OpenAPI entries (if configured)
 */
public class LoggingManagerOpenAPIFilter implements OASFilter {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final String REF_LOGGER_LEVEL = "#/components/schemas/LoggerLevel";
    private static final String REF_LOGGER_INFO = "#/components/schemas/LoggerInfo";

    private final String basePath;
    private final String tag;

    public LoggingManagerOpenAPIFilter(String basePath, String tag) {
        this.basePath = basePath;
        this.tag = tag;
    }

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        if (openAPI.getComponents() == null) {
            openAPI.setComponents(OASFactory.createComponents());
        }
        openAPI.getComponents()
                .addSchema("LoggerLevel", createLoggerLevel())
                .addSchema("LoggerInfo", createLoggerInfo());
        if (openAPI.getPaths() == null) {
            openAPI.setPaths(OASFactory.createPaths());
        }
        Tag tag = OASFactory.createTag();
        tag.setName(this.tag);
        tag.setDescription("Visualize and manage the log level of your loggers.");
        openAPI.addTag(tag);
        openAPI.getPaths()
                .addPathItem(basePath, createLoggersPathItem())
                .addPathItem(basePath + "/levels", createLevelsPathItem())
                .addPathItem(basePath + "/duration", createDurationPathItem());
    }



    private Schema createLoggerLevel() {
        Schema schema = OASFactory.createSchema()
                .title("LoggerLevel")
                .type(List.of(Schema.SchemaType.STRING));
        LogController.LEVELS.forEach(schema::addEnumeration);
        return schema;
    }

    private Schema createLoggerInfo() {
        Map<String, Schema> properties = new LinkedHashMap<>();
        properties.put("configuredLevel", OASFactory.createSchema().ref(REF_LOGGER_LEVEL));
        properties.put("effectiveLevel", OASFactory.createSchema().ref(REF_LOGGER_LEVEL));
        properties.put("name", OASFactory.createSchema().type(List.of(Schema.SchemaType.STRING)));
        return OASFactory.createSchema()
                .title("LoggerInfo")
                .type(List.of(Schema.SchemaType.OBJECT))
                .properties(properties);
    }

    private PathItem createLoggersPathItem() {
        return OASFactory.createPathItem()
                .summary("Return info on all loggers, or a specific logger")
                .description("Logging Manager Loggers")
                .GET(createLoggersGetOperation())
                .POST(createLoggerPostOperation());
    }

    private Operation createLoggersGetOperation() {
        return OASFactory.createOperation()
                .operationId("logging_manager_get_all")
                .summary("Information on Logger(s)")
                .description("Get information on all loggers or a specific logger.")
                .tags(Collections.singletonList(tag))
                .addParameter(OASFactory.createParameter()
                        .name("loggerName").in(Parameter.In.QUERY)
                        .schema(OASFactory.createSchema().type(List.of(Schema.SchemaType.STRING))))
                .responses(OASFactory.createAPIResponses()
                        .addAPIResponse("200", OASFactory.createAPIResponse()
                                .description("Ok")
                                .content(OASFactory.createContent().addMediaType(
                                        JSON_CONTENT_TYPE,
                                        OASFactory.createMediaType().schema(OASFactory.createSchema()
                                                .type(List.of(Schema.SchemaType.ARRAY))
                                                .items(OASFactory.createSchema().ref(REF_LOGGER_INFO))))))
                        .addAPIResponse("404", OASFactory.createAPIResponse().description("Not Found")));
    }

    private Operation createLoggerPostOperation() {
        Map<String, Schema> properties = new LinkedHashMap<>();
        properties.put("loggerName", OASFactory.createSchema().type(List.of(Schema.SchemaType.STRING)));
        properties.put("loggerLevel", OASFactory.createSchema().ref(REF_LOGGER_LEVEL));
        return OASFactory.createOperation()
                .operationId("logging_manager_update")
                .summary("Update log level")
                .description("Update a log level for a certain logger")
                .tags(Collections.singletonList(tag))
                .requestBody(OASFactory.createRequestBody()
                        .content(OASFactory.createContent().addMediaType(
                                FORM_CONTENT_TYPE,
                                OASFactory.createMediaType().schema(OASFactory.createSchema()
                                        .type(List.of(Schema.SchemaType.OBJECT))
                                        .properties(properties)))))
                .responses(OASFactory.createAPIResponses()
                        .addAPIResponse("201", OASFactory.createAPIResponse().description("Created")));
    }

    private PathItem createLevelsPathItem() {
        return OASFactory.createPathItem()
                .description("All available levels")
                .summary("Return all levels that is available")
                .GET(OASFactory.createOperation()
                        .description("This returns all possible log levels")
                        .operationId("logging_manager_levels")
                        .tags(Collections.singletonList(tag))
                        .summary("Get all available levels")
                        .responses(OASFactory.createAPIResponses()
                                .addAPIResponse("200", OASFactory.createAPIResponse()
                                        .description("Ok")
                                        .content(OASFactory.createContent().addMediaType(
                                                JSON_CONTENT_TYPE,
                                                OASFactory.createMediaType().schema(OASFactory.createSchema()
                                                        .type(List.of(Schema.SchemaType.ARRAY))
                                                        .items(OASFactory.createSchema().ref(REF_LOGGER_LEVEL))))))));
    }

    private PathItem createDurationPathItem() {
        Map<String, Schema> properties = new LinkedHashMap<>();
        properties.put("loggerName", OASFactory.createSchema().type(List.of(Schema.SchemaType.STRING)));
        properties.put("loggerLevel", OASFactory.createSchema().ref(REF_LOGGER_LEVEL));
        properties.put("duration", OASFactory.createSchema()
                                               .type(List.of(Schema.SchemaType.INTEGER))
                                               .description("Duration in seconds before reverting to the previous level"));

        return OASFactory.createPathItem()
                 .summary("Set temporary log level duration")
                 .description("Update a logger's level for a specified duration before reverting")
                 .POST(OASFactory.createOperation()
                          .operationId("logging_manager_set_duration")
                          .summary("Set a temporary log level")
                          .description("Set a log level for a specific logger that will automatically revert after a given duration")
                          .tags(Collections.singletonList(tag))
                          .requestBody(OASFactory.createRequestBody()
                                  .content(OASFactory.createContent().addMediaType(
                                            FORM_CONTENT_TYPE,
                                            OASFactory.createMediaType().schema(OASFactory.createSchema()
                                                    .type(List.of(Schema.SchemaType.OBJECT))
                                                    .properties(properties)))))
                          .responses(OASFactory.createAPIResponses()
                                  .addAPIResponse("200", OASFactory.createAPIResponse()
                                            .description("Log level already set to the requested value"))
                                  .addAPIResponse("201", OASFactory.createAPIResponse()
                                            .description("Temporary log level set successfully"))
                                  .addAPIResponse("400", OASFactory.createAPIResponse()
                                            .description("Invalid request: invalid level or duration"))
                                  .addAPIResponse("404", OASFactory.createAPIResponse()
                                            .description("Logger not found"))));
    }

}
