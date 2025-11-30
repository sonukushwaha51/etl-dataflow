package com.gcp.labs.etl.dataflow.event;

import lombok.*;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    private String eventId;

    private long timestamp;

    private String eventUuid;

    private Map<String, Object> fields;

    public static final Schema SCHEMA = SchemaBuilder.record("Event")
            .namespace("com.gcp.labs.etl.dataflow.event") // Ensure the namespace matches your package
            .fields()
            .name("eventId").type().stringType().noDefault()
            .name("timestamp").type().longType().noDefault()
            .name("eventUuid").type().stringType().noDefault()
            .name("fields").type().map().values()
            .unionOf()
            .stringType().and()
            .longType().and()
            .booleanType().and()
            .doubleType().and()
            .array().items().map().values().unionOf().stringType().and().booleanType().and().longType().and().doubleType().endUnion()
            .endUnion()
            .noDefault()
            .endRecord();

}
