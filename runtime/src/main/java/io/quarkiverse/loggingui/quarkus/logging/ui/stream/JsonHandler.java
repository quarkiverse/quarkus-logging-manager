package io.quarkiverse.loggingui.quarkus.logging.ui.stream;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import io.vertx.core.http.ServerWebSocket;

/**
 * Log handler for Logger UI
 */
public class JsonHandler extends Handler {

    private final ServerWebSocket session;

    public JsonHandler(ServerWebSocket session) {
        this.session = session;
        setFormatter(new JsonFormatter());
    }

    @Override
    public void publish(LogRecord logRecord) {
        if (session != null) {
            String message = getFormatter().format(logRecord);
            try {
                session.writeTextMessage(message);
            } catch (Throwable ex) {
                session.close();
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

}
