package com.gcp.labs.etl.dataflow.event;

import com.gcp.labs.etl.dataflow.singleton.supplier.ObjectMapperSingletonResource;
import com.google.inject.Inject;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static com.gcp.labs.etl.dataflow.tags.EtlDataflowTupleTag.FAILURE_TAG;
import static com.gcp.labs.etl.dataflow.tags.EtlDataflowTupleTag.SUCCESS_TAG;

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
            String eventString = StringUtils.toEncodedString(message.getPayload(), StandardCharsets.UTF_8);
            LOGGER.info("Event received: {}", eventString);
            Event event = objectMapperSingletonResource.getResource().readValue(eventString, Event.class);
            context.output(SUCCESS_TAG, event);
        } catch (Exception exception) {
            context.output(FAILURE_TAG, exception.toString());
            LOGGER.error("Error while processing data",exception);
        }

    }

}
