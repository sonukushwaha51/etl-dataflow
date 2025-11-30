package com.gcp.labs.etl.dataflow.singleton;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonHolder implements Serializable {

    public static final SingletonHolder INSTANCE = new SingletonHolder();

    private final ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

    public <T> T getOrInitialize(SerializerSupplier<T> serializerSupplier, Class<T> singleton, String className) {
        if (!map.containsKey(className)) {
            synchronized (this) {
                map.put(className, serializerSupplier.get());
                return serializerSupplier.get();
            }
        }
        return singleton.cast(map.get(className));
    }
}
