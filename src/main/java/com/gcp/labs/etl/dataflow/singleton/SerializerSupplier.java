package com.gcp.labs.etl.dataflow.singleton;

import java.io.Serializable;
import java.util.function.Supplier;

public interface SerializerSupplier<T> extends Supplier<T>, Serializable {

}
