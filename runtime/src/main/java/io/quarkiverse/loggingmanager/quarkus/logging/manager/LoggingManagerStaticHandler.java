package io.quarkiverse.loggingmanager.quarkus.logging.manager;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Handling static Logging UI content
 */
public class LoggingUiStaticHandler implements Handler<RoutingContext> {

    private String loggingUiFinalDestination;
    private String loggingUiPath;

    public LoggingUiStaticHandler() {
    }

    public LoggingUiStaticHandler(String loggingUiFinalDestination, String loggingUiPath) {
        this.loggingUiFinalDestination = loggingUiFinalDestination;
        this.loggingUiPath = loggingUiPath;
    }

    public String getLoggingUiFinalDestination() {
        return loggingUiFinalDestination;
    }

    public void setLoggingUiFinalDestination(String loggingUiFinalDestination) {
        this.loggingUiFinalDestination = loggingUiFinalDestination;
    }

    public String getLoggingUiPath() {
        return loggingUiPath;
    }

    public void setLoggingUiPath(String loggingUiPath) {
        this.loggingUiPath = loggingUiPath;
    }

    @Override
    public void handle(RoutingContext event) {
        StaticHandler staticHandler = StaticHandler.create().setAllowRootFileSystemAccess(true)
                .setWebRoot(loggingUiFinalDestination)
                .setDefaultContentEncoding("UTF-8");

        if (event.normalisedPath().length() == loggingUiPath.length()) {

            event.response().setStatusCode(302);
            event.response().headers().set(HttpHeaders.LOCATION, loggingUiPath + "/");
            event.response().end();
            return;
        } else if (event.normalisedPath().length() == loggingUiPath.length() + 1) {
            event.reroute(loggingUiPath + "/index.html");
            return;
        }

        staticHandler.handle(event);
    }

}
