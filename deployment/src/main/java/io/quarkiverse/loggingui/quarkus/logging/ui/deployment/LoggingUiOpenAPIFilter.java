package io.quarkiverse.loggingui.quarkus.logging.ui.deployment;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import io.smallrye.openapi.api.models.ComponentsImpl;
import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.api.models.PathsImpl;
import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.api.models.parameters.RequestBodyImpl;
import io.smallrye.openapi.api.models.responses.APIResponseImpl;
import io.smallrye.openapi.api.models.responses.APIResponsesImpl;

/**
 * Create OpenAPI entries (if configured)
 */
public class LoggingUiOpenAPIFilter implements OASFilter {
    public static final List<String> LOGGING_UI_TAG = Collections.singletonList("Logging UI");
    private static final String CONTENT_TYPE = "application/json";
    private static final String REF_LOGGER_INFO = "#/components/schemas/LoggerInfo";
    private static final String REF_LIST_LOGGER_INFO = "#/components/schemas/ListLoggerInfo";
    private static final String REF_LIST_STRING = "#/components/schemas/ListString";

    private final String basePath;

    public LoggingUiOpenAPIFilter(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        if (openAPI.getComponents() == null) {
            openAPI.setComponents(new ComponentsImpl());
        }
        openAPI.getComponents().addSchema("LoggerInfo", createLoggerInfo());
        openAPI.getComponents().addSchema("ListLoggerInfo", createListLoggerInfo());
        openAPI.getComponents().addSchema("ListString", createListString());

        if (openAPI.getPaths() == null) {
            openAPI.setPaths(new PathsImpl());
        }
        Paths paths = openAPI.getPaths();

        // Loggers
        paths.addPathItem(basePath, createLoggersPathItem());

        // Levels
        paths.addPathItem(basePath + "/levels", createLevelsPathItem());
    }

    private PathItem createLevelsPathItem() {
        PathItem pathItem = new PathItemImpl();
        pathItem.setDescription("All available levels ");
        pathItem.setSummary(
                "Return all levels that is available");
        pathItem.setGET(createLevelsOperation());
        return pathItem;
    }

    private Operation createLevelsOperation() {
        Operation operation = new OperationImpl();
        operation.setDescription("This returns all possible log levels");
        operation.setOperationId("loggerui_base_levels");
        operation.setTags(LOGGING_UI_TAG);
        operation.setSummary("Get all available levels");
        operation.setResponses(createLevelsAPIResponses());
        return operation;
    }

    private APIResponses createLevelsAPIResponses() {
        APIResponses responses = new APIResponsesImpl();
        responses.addAPIResponse("200", createLevelsAPIResponse());
        return responses;
    }

    private APIResponse createLevelsAPIResponse() {
        APIResponse response = new APIResponseImpl();
        response.setContent(createLevelsContent());
        return response;
    }

    private Content createLevelsContent() {
        Content content = new ContentImpl();
        content.addMediaType(CONTENT_TYPE, createLevelsMediaType());
        return content;
    }

    private MediaType createLevelsMediaType() {
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setSchema(new SchemaImpl().ref(REF_LIST_STRING));
        return mediaType;
    }

    private PathItem createLoggersPathItem() {
        PathItem pathItem = new PathItemImpl();
        pathItem.setDescription("Logging UI Loggers");
        pathItem.setSummary(
                "Return info on all loggers, or a specific logger");
        pathItem.setGET(createLoggersOperation());
        pathItem.setPOST(createLoggerPostOperation());
        return pathItem;
    }

    private Operation createLoggerPostOperation() {
        Operation operation = new OperationImpl();
        operation.setDescription("Update a log level for a certain logger");
        operation.setOperationId("loggerui_update");
        operation.setTags(LOGGING_UI_TAG);
        operation.setSummary("Update log level");
        operation.setResponses(createLoggerPostAPIResponses());
        operation.setRequestBody(createLoggersPostRequestBody());
        return operation;
    }

    private RequestBody createLoggersPostRequestBody() {
        RequestBody requestBody = new RequestBodyImpl();
        requestBody.setContent(createRequestBodyContent());
        return requestBody;
    }

    private Content createRequestBodyContent() {
        Content content = new ContentImpl();
        content.addMediaType(CONTENT_TYPE, createRequestBodyMediaType());
        return content;
    }

    private MediaType createRequestBodyMediaType() {
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setSchema(new SchemaImpl().ref(REF_LOGGER_INFO));
        mediaType.setExample("{\n" +
                "\t\"name\": \"com.myapp.somenamespace.MyClass\",\n" +
                "\t\"configuredLevel\": \"ERROR\"\n" +
                "}");
        return mediaType;
    }

    private APIResponses createLoggerPostAPIResponses() {
        APIResponses responses = new APIResponsesImpl();
        APIResponseImpl apiResponse = new APIResponseImpl();
        apiResponse.setDescription("Created");
        responses.addAPIResponse("201", apiResponse);
        return responses;
    }

    private Operation createLoggersOperation() {
        Operation operation = new OperationImpl();
        operation.setDescription("Get information on all loggers or a specific logger.");
        operation.setOperationId("loggerui_base");
        operation.setTags(LOGGING_UI_TAG);
        operation.setSummary("Information on Logger(s)");
        operation.setResponses(createLoggersAPIResponses());
        operation.addParameter(createLoggersParameter());
        return operation;
    }

    private APIResponses createLoggersAPIResponses() {
        APIResponses responses = new APIResponsesImpl();
        responses.addAPIResponse("200", createLoggersAPIResponse());
        APIResponseImpl notFound = new APIResponseImpl();
        notFound.setDescription("Not Found");
        responses.addAPIResponse("404", notFound);
        return responses;
    }

    private APIResponse createLoggersAPIResponse() {
        APIResponse response = new APIResponseImpl();
        response.setContent(createLoggersContent());
        return response;
    }

    private Content createLoggersContent() {
        Content content = new ContentImpl();
        content.addMediaType(CONTENT_TYPE, createLoggersMediaType());
        return content;
    }

    private MediaType createLoggersMediaType() {
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setSchema(new SchemaImpl().ref(REF_LIST_LOGGER_INFO));
        return mediaType;
    }

    private Parameter createLoggersParameter() {
        Parameter p = new ParameterImpl();
        p.setName("loggerName");
        p.setIn(Parameter.In.QUERY);
        p.setSchema(new SchemaImpl().type(Schema.SchemaType.STRING));
        return p;
    }

    private Schema createLoggerInfo() {
        Schema schema = new SchemaImpl("LoggerInfo");
        schema.setType(Schema.SchemaType.OBJECT);
        schema.setProperties(createProperties());
        return schema;
    }

    private Map<String, Schema> createProperties() {
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

    private Schema createListLoggerInfo() {
        Schema schema = new SchemaImpl("ListLoggerInfo");
        schema.setType(Schema.SchemaType.ARRAY);
        schema.setItems(new SchemaImpl().ref(REF_LIST_LOGGER_INFO));
        return schema;
    }

    private Schema createListString() {
        Schema schema = new SchemaImpl("ListString");
        schema.setType(Schema.SchemaType.ARRAY);
        schema.setItems(new SchemaImpl().type(Schema.SchemaType.STRING));
        return schema;
    }

}
