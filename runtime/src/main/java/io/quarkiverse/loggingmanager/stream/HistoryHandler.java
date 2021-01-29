package io.quarkiverse.loggingmanager.stream;

import java.util.LinkedList;
import java.util.List;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

/**
 * Log handler for Logger Manager
 */
public class HistoryHandler extends ExtHandler {

    private int size = 0;
    private final List<ExtLogRecord> history = new LinkedList<>();

    public void setSize(int size) {
        this.size = size;
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

    public boolean hasHistory() {
        return !history.isEmpty();
    }

    public List<ExtLogRecord> getHistory() {
        return history;
    }

    public void clearHistory() {
        this.history.clear();
    }
}
