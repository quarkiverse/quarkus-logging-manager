package io.quarkiverse.loggingmanager.stream;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import org.jboss.logmanager.ExtFormatter;
import org.jboss.logmanager.ExtLogRecord;

/**
 * Formatting log records into a json format
 */
public class JsonFormatter extends ExtFormatter {

    @Override
    public String format(final ExtLogRecord logRecord) {
        try (StringWriter stringWriter = new StringWriter();
                JsonWriter jsonWriter = Json.createWriter(stringWriter)) {

            jsonWriter.writeObject(toJsonObject(logRecord));
            return stringWriter.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private JsonObject toJsonObject(ExtLogRecord logRecord) {
        String formattedMessage = formatMessage(logRecord);
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (logRecord.getLoggerName() != null) {
            builder.add(LOGGER_NAME, logRecord.getLoggerName());
        }
        if (logRecord.getLevel() != null) {
            builder.add(LEVEL, logRecord.getLevel().getName());
        }
        if (formattedMessage != null) {
            builder.add(FORMATTED_MESSAGE, formattedMessage);
        }
        if (logRecord.getMessage() != null) {
            builder.add(MESSAGE, logRecord.getMessage());
        }
        if (logRecord.getSourceClassName() != null) {
            String justClassName = getJustClassName(logRecord.getSourceClassName());
            builder.add(SOURCE_CLASS_NAME_FULL_SHORT, getShortFullClassName(logRecord.getSourceClassName(), justClassName));
            builder.add(SOURCE_CLASS_NAME_FULL, logRecord.getSourceClassName());
            builder.add(SOURCE_CLASS_NAME, justClassName);
        }
        if (logRecord.getSourceMethodName() != null) {
            builder.add(SOURCE_METHOD_NAME, logRecord.getSourceMethodName());
        }
        if (logRecord.getThrown() != null) {
            builder.add(STACKTRACE, getStacktraces(logRecord.getThrown()));
        }
        builder.add(THREAD_ID, logRecord.getThreadID());
        builder.add(THREAD_NAME, Thread.currentThread().getName());
        builder.add(TIMESTAMP, logRecord.getMillis());
        builder.add(SEQUENCE_NUMBER, logRecord.getSequenceNumber());

        return builder.build();
    }

    private JsonArray getStacktraces(Throwable t) {
        List<String> traces = new LinkedList<>();
        addStacktrace(traces, t);

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        traces.forEach((trace) -> {
            arrayBuilder.add(trace);
        });
        return arrayBuilder.build();
    }

    private void addStacktrace(List<String> traces, Throwable t) {
        traces.add(getStacktrace(t));
        if (t.getCause() != null)
            addStacktrace(traces, t.getCause());
    }

    private String getStacktrace(Throwable t) {
        try (StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ex) {
            return null;
        }
    }

    private String getJustClassName(String fullName) {
        int lastDot = fullName.lastIndexOf(DOT) + 1;
        return fullName.substring(lastDot);
    }

    private String getShortFullClassName(String fullName, String justClassName) {
        String[] parts = fullName.split("\\" + DOT);
        try (StringWriter buffer = new StringWriter()) {
            for (String part : parts) {
                if (part.equals(justClassName) || part.length() < 3) {
                    buffer.write(part);
                } else {
                    buffer.write(part.substring(0, 3));
                }
                buffer.write(DOT);
            }
            String r = buffer.toString();

            return r.substring(0, r.lastIndexOf(DOT));
        } catch (IOException ex) {
            return fullName;
        }
    }

    private static final String LEVEL = "level";
    private static final String MESSAGE = "message";
    private static final String FORMATTED_MESSAGE = "formattedMessage";
    private static final String LOGGER_NAME = "loggerName";
    private static final String SOURCE_CLASS_NAME_FULL = "sourceClassNameFull";
    private static final String SOURCE_CLASS_NAME_FULL_SHORT = "sourceClassNameFullShort";
    private static final String SOURCE_CLASS_NAME = "sourceClassName";
    private static final String SOURCE_METHOD_NAME = "sourceMethodName";
    private static final String THREAD_ID = "threadId";
    private static final String THREAD_NAME = "threadName";
    private static final String TIMESTAMP = "timestamp";
    private static final String STACKTRACE = "stacktrace";
    private static final String SEQUENCE_NUMBER = "sequenceNumber";
    private static final String DOT = ".";
}
