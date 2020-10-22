package io.quarkiverse.loggingui.quarkus.logging.ui.deployment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

@SuppressWarnings("unchecked")
class QuarkusLoggingUiTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest();

    @Test
    public void testChangeState1() {
        String loggerName = "io.quarkus.loggers";
        String newLogLevel = "trace";
        Map<String, Object> response = performGetRequest(loggerName);
        checkGetResponse(response, loggerName);
        performPostRequest(loggerName, newLogLevel, 204);
    }

    @Test
    public void testChangeState2() {
        String loggerName = "io.quarkus.loggers";
        String newLogLevel = "trace";
        Map<String, Object> response = performGetRequest(loggerName);
        checkGetResponse(response, loggerName);
        performPostRequest(loggerName, newLogLevel, 204);
    }

    private Map<String, Object> performGetRequest(String loggerName) {
        return RestAssured.when().get("/loggers/" + loggerName).then().statusCode(200)
                .body("loggers", notNullValue())
                .body("levels", notNullValue())
                .extract()
                .as(Map.class);
    }

    private void checkGetResponse(Map<String, Object> response, String loggerName) {
        checkGetResponse(response, loggerName, null, "INFO");
    }

    private void checkGetResponse(Map<String, Object> response, String loggerName, String configuredLogLevel,
            String effectiveLogLevel) {
        ArrayList<String> levels = (ArrayList) response.get("levels");
        assertThat(levels.size(), equalTo(9));

        Map<String, Map<String, String>> descriptionByLoggerName = (Map<String, Map<String, String>>) response.get(
                "loggers");
        assertThat(descriptionByLoggerName.size(), equalTo(1));
        Map<String, String> logLevelDescription = descriptionByLoggerName.get(loggerName);
        assertThat(logLevelDescription.get("configuredLevel"),
                configuredLogLevel == null ? nullValue() : equalTo(configuredLogLevel));
        assertThat(logLevelDescription.get("effectiveLevel"), equalTo(effectiveLogLevel));
    }

    private void performPostRequest(String loggerName, String newLogLevel, int expectedStatusCode) {
        given().contentType("application/json")
                .body(String.format("{\"configuredLevel\": \"%s\"}", newLogLevel))
                .when()
                .post("/loggers/{loggerName}", loggerName)
                .then().statusCode(expectedStatusCode);
    }
}
