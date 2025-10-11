package com.gcp.labs.etl.dataflow.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapPubsubMessageToEvent extends DoFn<PubsubMessage, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapPubsubMessageToEvent.class);

    private ObjectMapper objectMapper;

    @ProcessElement
    public void processElement(DoFn<PubsubMessage, String>.ProcessContext context, BoundedWindow boundedWindow) {
        try {
            PubsubMessage message = context.element();
            String stringMessage = objectMapper.writeValueAsString(message);
            context.output(stringMessage);
        } catch (Exception exception) {
            throw new RuntimeException("Exception occurred while processing data from pubsub: "+ exception.getMessage());
        }

    }

}
