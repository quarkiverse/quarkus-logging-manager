package io.quarkiverse.loggingmanager.stream;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

/**
 * Log handler for Logger Manager
 */
public class HistoryHandler extends ExtHandler {

    private final int size;
    private final List<ExtLogRecord> history = new LinkedList<>();

    public HistoryHandler(int size) {
        this.size = size;
        super.setLevel(Level.INFO);
        setFormatter(new JsonFormatter());
    }

    @Override
    public final void doPublish(final ExtLogRecord record) {
        // Don't log empty messages
        if (record.getMessage() == null || record.getMessage().isEmpty()) {
            return;
        }

        if (isLoggable(record)) {
            history.add(record);

            while (history.size() > size) {
                history.remove(0);
            }
        }

    }

    public List<ExtLogRecord> getHistory() {
        return history;
    }

    public void clearHistory() {
        this.history.clear();
    }
}
