package com.gcp.labs.etl.dataflow.csvProcessor;

import com.gcp.labs.etl.dataflow.singleton.supplier.ObjectMapperSingletonResource;
import com.google.inject.Inject;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExtractCsvFilePathDoFn extends DoFn<PubsubMessage, String> {

    private final Logger LOGGER = LoggerFactory.getLogger(ExtractCsvFilePathDoFn.class);

    private final ObjectMapperSingletonResource objectMapperSingletonResource;

    @Inject
    public ExtractCsvFilePathDoFn(ObjectMapperSingletonResource objectMapperSingletonResource) {
        this.objectMapperSingletonResource = objectMapperSingletonResource;
    }

    @ProcessElement
    public void processElement(DoFn<PubsubMessage, String>.ProcessContext context, BoundedWindow boundedWindow) {
        PubsubMessage pubsubMessage = context.element();
        CsvFile csvFile = null;
        try {
            csvFile = objectMapperSingletonResource.getResource().readValue(pubsubMessage.getPayload(), CsvFile.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert csvFile != null;
        String csvFilePath = csvFile.getCsvFilePath();
        LOGGER.info("CSV file path: {}", csvFilePath);
        context.output(csvFilePath);
    }
}
