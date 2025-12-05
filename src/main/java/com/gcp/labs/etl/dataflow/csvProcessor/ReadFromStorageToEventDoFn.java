package com.gcp.labs.etl.dataflow.csvProcessor;

import com.gcp.labs.etl.dataflow.event.Event;
import com.gcp.labs.etl.dataflow.singleton.supplier.ObjectMapperSingletonResource;
import com.google.inject.Inject;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReadFromStorageToEventDoFn extends DoFn<String, Event> {

    private final ObjectMapperSingletonResource objectMapperSingletonResource;

    @Inject
    public ReadFromStorageToEventDoFn(ObjectMapperSingletonResource objectMapperSingletonResource) {
        this.objectMapperSingletonResource = objectMapperSingletonResource;
    }

    @ProcessElement
    public void processElement(DoFn<String, Event>.ProcessContext context, BoundedWindow boundedWindow) {
        String csvFilePath = context.element();
        if (csvFilePath != null) {
            try (FileReader fileReader = new FileReader(csvFilePath)) {
                CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(fileReader);
                for (CSVRecord csvRecord : csvParser) {
                    Map<String, Object> fields = new HashMap<>();
                    fields.put("firstName", csvRecord.get("firstName"));
                    fields.put("email", csvRecord.get("email"));
                    fields.put("customerIdentifier", csvRecord.get("identifier"));
                    fields.put("skuId", csvRecord.get("skuId"));
                    Event event = new Event();
                    event.setEventId(csvRecord.get("eventId"));
                    event.setEventUuid(UUID.randomUUID().toString());
                    event.setTimestamp(Instant.now().toEpochMilli());
                    event.setFields(fields);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
