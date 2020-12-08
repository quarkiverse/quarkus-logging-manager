package io.quarkiverse.loggingui.quarkus.logging.ui.deployment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.restassured.RestAssured;

class AbstractConfigurationTest {

    void getEndpointLoggers(String path) {
        // Without trailing /
        List<Map<String, String>> response = RestAssured.when().get(path).then().statusCode(200)
                .body(notNullValue())
                .extract()
                .as(List.class);

        assertThat(response.size(), greaterThan(1));

        // With trailing /
        response = RestAssured.when().get(path).then().statusCode(200)
                .body(notNullValue())
                .extract()
                .as(List.class);

        assertThat(response.size(), greaterThan(1));
    }

    void getEndpointLevels(String path) {
        // Without trailing /
        List<String> levels = RestAssured.when().get(path + "/levels").then().statusCode(200)
                .body(notNullValue())
                .extract()
                .as(List.class);

        assertThat(levels.size(), equalTo(9));

        // With trailing /
        levels = RestAssured.when().get(path + "/levels/").then().statusCode(200)
                .body(notNullValue())
                .extract()
                .as(List.class);

        assertThat(levels.size(), equalTo(9));
    }

    void getEndpointListSpecificLogger(String path) {
        String loggerName = "io.quarkus";
        Map<String, Object> response = performGetRequest(path, loggerName);
        checkGetResponse(response, loggerName);
    }

    void getEndpointListROOTLogger(String path) {
        String loggerName = "ROOT";
        Map<String, Object> response = performGetRequest(path, loggerName);
        checkGetResponse(response, loggerName);
    }

    void postEndpointSetsLogLevel(String path) {
        String loggerName = "ROOT";
        String newLogLevel = "TRACE";
        Map<String, Object> response = performGetRequest(path, loggerName);
        checkGetResponse(response, loggerName);
        performPostRequest(path, loggerName, newLogLevel, 201);
        Map<String, Object> secondCallResponse = performGetRequest(path, loggerName);
        checkGetResponse(secondCallResponse, loggerName, newLogLevel, newLogLevel);
        // Put it back to where is was
        performPostRequest(path, loggerName, "INFO", 201);

    }

    void postEndpointSetsLogLevelAlsoInLowerCase(String path) {
        String loggerName = "io.quarkus.loggers";
        String newLogLevel = "trace";
        Map<String, Object> response = performGetRequest(path, loggerName);
        checkGetResponse(response, loggerName);
        performPostRequest(path, loggerName, newLogLevel, 201);
        Map<String, Object> secondCallResponse = performGetRequest(path, loggerName);
        checkGetResponse(secondCallResponse, loggerName, newLogLevel.toUpperCase(Locale.ROOT),
                newLogLevel.toUpperCase(Locale.ROOT));
        // Put it back to where is was
        performPostRequest(path, loggerName, "INFO", 201);
    }

    void postEndpointFailsOnUnknownLogLevel(String path) {
        String loggerName = "ROOT";
        String newLogLevel = "non-existing";
        performPostRequest(path, loggerName, newLogLevel, 400);
    }

    private Map<String, Object> performGetRequest(String path, String loggerName) {

        return RestAssured.when().get(path + "?loggerName=" + loggerName).then().statusCode(200)
                .body(notNullValue())
                .extract()
                .as(Map.class);
    }

    private void checkGetResponse(Map<String, Object> response, String loggerName) {
        checkGetResponse(response, loggerName, null, "INFO");
    }

    private void checkGetResponse(Map<String, Object> response, String loggerName, String configuredLogLevel,
            String effectiveLogLevel) {

        assertThat(response.get("name"), equalTo(loggerName));
        assertThat(response.get("effectiveLevel"), equalTo(effectiveLogLevel));
    }

    private void performPostRequest(String path, String loggerName, String newLogLevel, int expectedStatusCode) {
        given().contentType("application/json")
                .body(String.format("{\n" +
                        "	\"name\": \"%s\",\n" +
                        "	\"configuredLevel\": \"%s\"\n" +
                        "}", loggerName, newLogLevel))
                .when()
                .post(path)
                .then().statusCode(expectedStatusCode);
    }

    // TODO:
    // Other technologies (slf4j?) compatible?

}
