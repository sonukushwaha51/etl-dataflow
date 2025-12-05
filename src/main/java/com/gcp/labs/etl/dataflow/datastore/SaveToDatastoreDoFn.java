package com.gcp.labs.etl.dataflow.datastore;

import com.gcp.labs.etl.dataflow.event.Event;
import com.gcp.labs.etl.dataflow.singleton.supplier.ObjectMapperSingletonResource;
import com.google.inject.Inject;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveToDatastoreDoFn extends DoFn<Event, Void> {

    private final DatastoreService datastoreService;

    private final ObjectMapperSingletonResource objectMapperSingletonResource;

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveToDatastoreDoFn.class);

    @Inject
    public SaveToDatastoreDoFn(DatastoreService datastoreService, ObjectMapperSingletonResource objectMapperSingletonResource) {
        this.datastoreService = datastoreService;
        this.objectMapperSingletonResource = objectMapperSingletonResource;
    }

    @ProcessElement
    public void processElement(DoFn<Event, Void>.ProcessContext context, BoundedWindow boundedWindow) {
        Event event = context.element();
        LOGGER.info("Order event received: {}, with eventUuid: {}", event.getEventId(), event.getEventUuid());
        datastoreService.addMessagesToDatastore(event);
    }
}
