package io.quarkiverse.loggingui.quarkus.logging.ui.stream;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import io.quarkus.arc.Unremovable;

/**
 * Websocket server that can distribute log messages.
 * If there is no subscribers, the logging fall back to normal file.
 */
@Unremovable
@ServerEndpoint("/logstream")
@ApplicationScoped
public class LogstreamSocket {

    @OnOpen
    public void onOpen(Session session) {

    }

    @OnClose
    public void onClose(Session session) {
        stop(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (message != null && !message.isEmpty()) {
            if (message.equalsIgnoreCase(START)) {
                start(session);
            } else if (message.equalsIgnoreCase(STOP)) {
                stop(session);
            }
        }
    }

    private void start(Session session) {
        String uuid = getUuid(session);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            registerHandler(session, uuid);
            SESSIONS.put(session.getId(), session);
        }
    }

    private void stop(Session session) {
        String name = getUuid(session);
        if (name != null) {
            unregisterHandler(session);
            SESSIONS.remove(session.getId());
        }
    }

    private void registerHandler(Session session, String uuid) {

        Handler handler = new MemoryHandler(new JsonHandler(session), 1000, Level.FINEST);

        Logger logger = Logger.getLogger("");
        if (logger != null) {
            logger.addHandler(handler);

            session.getUserProperties().put(HANDLER, handler);
            session.getUserProperties().put(ID, uuid);
        }
    }

    private void unregisterHandler(Session session) {
        Handler handler = getHandler(session);
        if (handler != null) {
            Logger logger = Logger.getLogger("");
            if (logger != null)
                logger.removeHandler(handler);
        }

        session.getUserProperties().remove(ID);
        session.getUserProperties().remove(HANDLER);
    }

    private Handler getHandler(Session session) {
        Object o = session.getUserProperties().get(HANDLER);
        if (o != null) {
            return (Handler) o;
        }
        return null;
    }

    private String getUuid(Session session) {
        Object o = session.getUserProperties().get(ID);
        if (o == null)
            return null;
        return (String) o;
    }

    private static final String ID = "uuid";
    private static final String HANDLER = "handler";

    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String LOGGER_NAME = "loggerName";

    private static final String DOT = ".";
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();
}
