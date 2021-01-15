package io.quarkiverse.loggingui.quarkus.logging.ui;

import static io.vertx.core.http.HttpMethod.GET;
import static java.util.logging.Level.ALL;
import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.util.Arrays;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class LevelHandler implements Handler<RoutingContext> {
    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    private final String levelsJson;

    public LevelHandler() {
        this.levelsJson = JSONB.toJson(Arrays.asList(new String[] {
                OFF.getName(),
                SEVERE.getName(),
                WARNING.getName(),
                INFO.getName(),
                CONFIG.getName(),
                FINE.getName(),
                FINER.getName(),
                FINEST.getName(),
                ALL.getName() }));
    }

    @Override
    public void handle(RoutingContext routingContext) {

        HttpServerRequest request = routingContext.request();
        HttpMethod method = request.method();

        HttpServerResponse response = routingContext.response();
        response.headers().add("Content-Type", "application/json");

        if (GET == method) {
            response.end(levelsJson);
        } else {
            response.end();
        }
    }

}
