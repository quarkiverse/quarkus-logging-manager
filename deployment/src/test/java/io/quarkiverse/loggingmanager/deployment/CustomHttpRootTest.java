package io.quarkiverse.loggingmanager.deployment;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;
import io.restassured.RestAssured;

public class CustomHttpRootTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset("quarkus.http.root-path=/foo"), "application.properties"));

    @Test
    public void shouldUseCustomConfig() {
        RestAssured.when().get("/q/logging-manager").then().statusCode(200);
    }
}
