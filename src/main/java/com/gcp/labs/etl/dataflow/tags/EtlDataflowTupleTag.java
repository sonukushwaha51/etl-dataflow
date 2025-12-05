package com.gcp.labs.etl.dataflow.tags;

import com.gcp.labs.etl.dataflow.event.Event;
import org.apache.beam.sdk.values.TupleTag;

public class EtlDataflowTupleTag {

    public static final TupleTag<Event> SUCCESS_TAG = new TupleTag<>() {};

    public static final TupleTag<String> FAILURE_TAG = new TupleTag<>() {};

}
