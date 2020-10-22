package io.quarkiverse.loggingui.quarkus.logging.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;

final class ObjectMapperProducer {

    private ObjectMapperProducer() {
    }

    static ObjectMapper get() {
        ObjectMapper objectMapper = null;
        ArcContainer container = Arc.container();
        if (container != null) {
            objectMapper = container.instance(ObjectMapper.class).get();
        }
        return objectMapper != null ? objectMapper : new ObjectMapper();
    }
}