package com.gcp.labs.etl.dataflow.singleton;

import java.io.Serializable;

public abstract class SingletonResource<T> implements Serializable {

    private final SerializerSupplier<T> serializerSupplier;

    public SingletonResource(SerializerSupplier<T> serializerSupplier) {
        this.serializerSupplier = serializerSupplier;
    }

    public abstract Class<T> getResourceClass();
    public T getResource() {
        return SingletonHolder.INSTANCE.getOrInitialize(serializerSupplier, getResourceClass(), getClass().getName());
    }

}
