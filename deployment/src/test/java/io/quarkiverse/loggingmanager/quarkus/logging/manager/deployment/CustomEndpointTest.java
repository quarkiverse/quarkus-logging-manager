package io.quarkiverse.loggingmanager.quarkus.logging.manager.deployment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

@SuppressWarnings("unchecked")
class CustomEndpointTest extends AbstractConfigurationTest {
    private static final String PATH = "/myownendpoint"; //Custom

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.logging-manager.base-path", PATH);

    @Test
    public void getEndpointLoggers() {
        super.getEndpointLoggers(PATH);
    }

    @Test
    public void getEndpointLevels() {
        super.getEndpointLevels(PATH);
    }

    @Test
    public void getEndpointListSpecificLogger() {
        super.getEndpointListSpecificLogger(PATH);
    }

    @Test
    public void getEndpointListROOTLogger() {
        super.getEndpointListROOTLogger(PATH);
    }

    @Test
    public void postEndpointSetsLogLevel() {
        super.postEndpointSetsLogLevel(PATH);
    }

    @Test
    public void postEndpointSetsLogLevelAlsoInLowerCase() {
        super.postEndpointSetsLogLevelAlsoInLowerCase(PATH);
    }

    @Test
    public void postEndpointFailsOnUnknownLogLevel() {
        super.postEndpointFailsOnUnknownLogLevel(PATH);
    }
}
