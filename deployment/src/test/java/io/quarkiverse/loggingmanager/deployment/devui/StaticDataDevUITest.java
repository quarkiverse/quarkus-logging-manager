package io.quarkiverse.loggingmanager.deployment.devui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.quarkiverse.loggingmanager.LogController;
import io.quarkus.devui.tests.DevUIBuildTimeDataTest;
import io.quarkus.test.QuarkusDevModeTest;

public class StaticDataDevUITest extends DevUIBuildTimeDataTest {

    @RegisterExtension
    static final QuarkusDevModeTest config = new QuarkusDevModeTest().withEmptyApplication();

    public StaticDataDevUITest() {
        super("io.quarkiverse.loggingmanager.quarkus-logging-manager");
    }

    @Test
    public void levelDataAvailable() throws Exception {
        JsonNode levelResponse = super.getBuildTimeData("level");
        Assertions.assertAll(
                () -> Assertions.assertInstanceOf(ArrayNode.class, levelResponse),
                () -> Assertions.assertEquals(LogController.LEVELS, levelResponse.findValuesAsText("level")));
    }

}
