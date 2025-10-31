package com.gcp.labs.etl.dataflow.datastore;

import com.gcp.labs.etl.dataflow.event.Event;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;

public class SaveToDatastoreDoFn extends DoFn<Event, Void> {

    public SaveToDatastoreDoFn() {

    }

    @ProcessElement
    public void processElement(DoFn<Event, Void>.ProcessContext context, BoundedWindow boundedWindow) {
        Event event = context.element();
    }
}
