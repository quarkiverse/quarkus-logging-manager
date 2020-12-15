package io.quarkiverse.loggingui.quarkus.logging.ui.stream;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.websocket.Session;

/**
 * Log handler for Logger UI
 */
public class JsonHandler extends Handler {

    private final Session session;

    public JsonHandler(Session session) {
        this.session = session;
        setFormatter(new JsonFormatter());
    }

    @Override
    public void publish(LogRecord logRecord) {
        if (session != null) {
            String message = getFormatter().format(logRecord);
            try {
                session.getBasicRemote().sendText(message);
            } catch (Throwable ex) {
                try {
                    session.close();
                } catch (IOException ex1) {
                }
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
