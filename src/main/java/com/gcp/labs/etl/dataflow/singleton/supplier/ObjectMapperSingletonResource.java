package com.gcp.labs.etl.dataflow.singleton.supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcp.labs.etl.dataflow.singleton.SerializerSupplier;
import com.gcp.labs.etl.dataflow.singleton.SingletonResource;
import com.google.inject.Inject;

public class ObjectMapperSingletonResource extends SingletonResource<ObjectMapper> {

    @Inject
    public ObjectMapperSingletonResource(SerializerSupplier<ObjectMapper> serializerSupplier) {
        super(serializerSupplier);
    }

    @Override
    public Class<ObjectMapper> getResourceClass() {
        return ObjectMapper.class;
    }
}
