package com.gcp.labs.etl.dataflow.event;

import com.gcp.labs.etl.dataflow.singleton.supplier.ObjectMapperSingletonResource;
import com.google.inject.Inject;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapPubsubMessageToEventDoFn extends DoFn<PubsubMessage, Event> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapPubsubMessageToEventDoFn.class);

    private final ObjectMapperSingletonResource objectMapperSingletonResource;

    @Inject
    public MapPubsubMessageToEventDoFn(ObjectMapperSingletonResource objectMapperSingletonResource) {
        this.objectMapperSingletonResource = objectMapperSingletonResource;
    }

    @ProcessElement
    public void processElement(DoFn<PubsubMessage, Event>.ProcessContext context, BoundedWindow boundedWindow) {
        try {
            PubsubMessage message = context.element();
            LOGGER.info("Event received: {}", message.getPayload());
            Event event = objectMapperSingletonResource.getResource().readValue(message.getPayload(), Event.class);
            context.output(event);
        } catch (Exception exception) {
            LOGGER.error("Error while processing data");
            throw new RuntimeException("Exception occurred while processing data from pubsub: "+ exception.getMessage());
        }

    }

}
