package io.quarkiverse.loggingmanager.deployment;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;

import io.quarkus.vertx.http.runtime.logstream.LogController;
import io.smallrye.openapi.api.models.media.SchemaImpl;

/**
 * Create OpenAPI entries (if configured)
 */
public class LoggingManagerOpenAPIFilter implements OASFilter {
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final String REF_LOGGER_NAME = "#/components/schemas/LoggerName";
    private static final String REF_LOGGER_LEVEL = "#/components/schemas/LoggerLevel";

    private static final String REF_LIST_LOGGER_INFO = "#/components/schemas/ListLoggerInfo";
    private static final String REF_LIST_STRING = "#/components/schemas/ListString";

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

        openAPI.getComponents().addSchema("LoggerName", createLoggerName());
        openAPI.getComponents().addSchema("LoggerLevel", createLoggerLevel());

        openAPI.getComponents().addSchema("LoggerInfo", createLoggerInfo());
        openAPI.getComponents().addSchema("ListString", createListString());

        if (openAPI.getPaths() == null) {
            openAPI.setPaths(OASFactory.createPaths());
        }
        Paths paths = openAPI.getPaths();

        // Loggers
        paths.addPathItem(basePath, createLoggersPathItem());

        // Levels
        paths.addPathItem(basePath + "/levels", createLevelsPathItem());
    }

    private PathItem createLevelsPathItem() {
        PathItem pathItem = OASFactory.createPathItem();
        pathItem.setDescription("All available levels ");
        pathItem.setSummary(
                "Return all levels that is available");
        pathItem.setGET(createLevelsOperation());
        return pathItem;
    }

    private Operation createLevelsOperation() {
        Operation operation = OASFactory.createOperation();
        operation.setDescription("This returns all possible log levels");
        operation.setOperationId("logging_manager_levels");
        operation.setTags(Collections.singletonList(tag));
        operation.setSummary("Get all available levels");
        operation.setResponses(createLevelsAPIResponses());
        return operation;
    }

    private APIResponses createLevelsAPIResponses() {
        APIResponses responses = OASFactory.createAPIResponses();
        responses.addAPIResponse("200", createLevelsAPIResponse());
        return responses;
    }

    private APIResponse createLevelsAPIResponse() {
        APIResponse response = OASFactory.createAPIResponse();
        response.setContent(createLevelsContent());
        return response;
    }

    private Content createLevelsContent() {
        Content content = OASFactory.createContent();
        content.addMediaType(JSON_CONTENT_TYPE, createLevelsMediaType());
        return content;
    }

    private MediaType createLevelsMediaType() {
        MediaType mediaType = OASFactory.createMediaType();
        mediaType.setSchema(OASFactory.createSchema().ref(REF_LIST_STRING));
        return mediaType;
    }

    private PathItem createLoggersPathItem() {
        PathItem pathItem = OASFactory.createPathItem();
        pathItem.setDescription("Logging Manager Loggers");
        pathItem.setSummary(
                "Return info on all loggers, or a specific logger");
        pathItem.setGET(createLoggersOperation());
        pathItem.setPOST(createLoggerPostOperation());
        return pathItem;
    }

    private Operation createLoggerPostOperation() {
        Operation operation = OASFactory.createOperation();
        operation.setDescription("Update a log level for a certain logger");
        operation.setOperationId("logging_manager_update");
        operation.setTags(Collections.singletonList(tag));
        operation.setSummary("Update log level");
        operation.setResponses(createLoggerPostAPIResponses());
        operation.setRequestBody(createLoggersPostRequestBody());
        return operation;
    }

    private RequestBody createLoggersPostRequestBody() {
        RequestBody requestBody = OASFactory.createRequestBody();
        requestBody.setContent(createRequestBodyContent());
        return requestBody;
    }

    private Content createRequestBodyContent() {
        Content content = OASFactory.createContent();
        content.addMediaType(FORM_CONTENT_TYPE, createRequestBodyMediaType());

        return content;
    }

    private MediaType createRequestBodyMediaType() {
        MediaType mediaType = OASFactory.createMediaType();

        Schema schema = OASFactory.createSchema();
        schema.setType(Schema.SchemaType.OBJECT);

        Map<String, Schema> properties = new HashMap<>();
        properties.put("loggerName", OASFactory.createSchema().ref(REF_LOGGER_NAME));
        properties.put("loggerLevel", OASFactory.createSchema().ref(REF_LOGGER_LEVEL));
        schema.setProperties(properties);

        mediaType.setSchema(schema);

        return mediaType;
    }

    private APIResponses createLoggerPostAPIResponses() {
        APIResponses responses = OASFactory.createAPIResponses();
        APIResponse apiResponse = OASFactory.createAPIResponse();
        apiResponse.setDescription("Created");
        responses.addAPIResponse("201", apiResponse);
        return responses;
    }

    private Operation createLoggersOperation() {
        Operation operation = OASFactory.createOperation();
        operation.setDescription("Get information on all loggers or a specific logger.");
        operation.setOperationId("logging_manager_get_all");
        operation.setTags(Collections.singletonList(tag));
        operation.setSummary("Information on Logger(s)");
        operation.setResponses(createLoggersAPIResponses());
        operation.addParameter(createLoggersParameter());
        return operation;
    }

    private APIResponses createLoggersAPIResponses() {
        APIResponses responses = OASFactory.createAPIResponses();
        responses.addAPIResponse("200", createLoggersAPIResponse());
        APIResponse notFound = OASFactory.createAPIResponse();
        notFound.setDescription("Not Found");
        responses.addAPIResponse("404", notFound);
        return responses;
    }

    private APIResponse createLoggersAPIResponse() {
        APIResponse response = OASFactory.createAPIResponse();
        response.setContent(createLoggersContent());
        return response;
    }

    private Content createLoggersContent() {
        Content content = OASFactory.createContent();
        content.addMediaType(JSON_CONTENT_TYPE, createLoggersMediaType());
        return content;
    }

    private MediaType createLoggersMediaType() {
        MediaType mediaType = OASFactory.createMediaType();
        mediaType.setSchema(OASFactory.createSchema().ref(REF_LIST_LOGGER_INFO));
        return mediaType;
    }

    private Parameter createLoggersParameter() {
        Parameter p = OASFactory.createParameter();
        p.setName("loggerName");
        p.setIn(Parameter.In.QUERY);
        p.setSchema(OASFactory.createSchema().type(Schema.SchemaType.STRING));
        return p;
    }

    private Schema createLoggerName() {
        Schema schema = new SchemaImpl("LoggerName");
        schema.setType(Schema.SchemaType.STRING);
        schema.setDescription("The logger name");

        return schema;
    }

    private Schema createLoggerLevel() {
        Schema schema = new SchemaImpl("LoggerLevel");
        schema.setType(Schema.SchemaType.STRING);

        List<String> loggerLevels = LogController.LEVELS;
        for (String l : loggerLevels) {
            schema.addEnumeration(l);
        }
        return schema;
    }

    private Schema createLoggerInfo() {
        Schema schema = new SchemaImpl("LoggerInfo");
        schema.setType(Schema.SchemaType.OBJECT);
        schema.setProperties(createLoggerInfoProperties());
        return schema;
    }

    private Map<String, Schema> createLoggerInfoProperties() {
        Map<String, Schema> map = new HashMap<>();
        map.put("configuredLevel", createStringSchema("configuredLevel"));
        map.put("effectiveLevel", createStringSchema("effectiveLevel"));
        map.put("name", createStringSchema("name"));

        return map;
    }

    private Schema createStringSchema(String name) {
        Schema schema = new SchemaImpl(name);
        schema.setType(Schema.SchemaType.STRING);
        return schema;
    }

    private Schema createListString() {
        Schema schema = new SchemaImpl("ListString");
        schema.setType(Schema.SchemaType.ARRAY);
        schema.setItems(new SchemaImpl().type(Schema.SchemaType.STRING));
        return schema;
    }

}
