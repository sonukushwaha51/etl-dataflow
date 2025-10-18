package com.gcp.labs.etl.dataflow.singleton.supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcp.labs.etl.dataflow.singleton.SerializerSupplier;

public class ObjectMapperSupplier implements SerializerSupplier<ObjectMapper> {
    @Override
    public ObjectMapper get() {
        return new ObjectMapper();
    }
}
