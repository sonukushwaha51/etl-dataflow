package com.gcp.labs.etl.dataflow.singleton.supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gcp.labs.etl.dataflow.singleton.SerializerSupplier;

public class ObjectMapperSupplier implements SerializerSupplier<ObjectMapper> {
    @Override
    public ObjectMapper get() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
