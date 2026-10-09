package io.quarkiverse.loggingmanager.deployment;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.test.QuarkusExtensionTest;

public class ErroneousConfigTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setExpectedException(ConfigurationException.class)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset("quarkus.logging-manager.base-path=/\n"), "application.properties"));

    @Test
    public void shouldNotStartApplicationIfPathIsASlash() {
        Assertions.fail();
    }
}