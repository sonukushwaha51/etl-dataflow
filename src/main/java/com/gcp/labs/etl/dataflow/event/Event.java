package com.gcp.labs.etl.dataflow.event;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

@Data
@Builder
public class Event {

    private String eventId;

    private long timestamp;

    private String eventUuid;

    private Map<String, Object> fields;

}
