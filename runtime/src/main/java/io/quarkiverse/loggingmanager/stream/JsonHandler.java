package io.quarkiverse.loggingmanager.stream;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

import io.vertx.core.http.ServerWebSocket;

/**
 * Log handler for Logger Manager
 */
public class JsonHandler extends ExtHandler {

    private final ServerWebSocket session;

    public JsonHandler(ServerWebSocket session) {
        this.session = session;
        setFormatter(new JsonFormatter());
    }

    @Override
    public final void doPublish(final ExtLogRecord record) {
        // Don't log empty messages
        if (record.getMessage() == null || record.getMessage().isEmpty()) {
            return;
        }

        if (session != null) {
            String message = getFormatter().format(record);
            try {
                session.writeTextMessage(message);
            } catch (Throwable ex) {
                session.close();
            }
        }

    }
}
