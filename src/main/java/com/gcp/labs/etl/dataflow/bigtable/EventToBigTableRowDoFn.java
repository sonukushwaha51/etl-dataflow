package com.gcp.labs.etl.dataflow.bigtable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.gcp.labs.etl.dataflow.event.Event;
import com.gcp.labs.etl.dataflow.singleton.supplier.ObjectMapperSingletonResource;
import com.google.bigtable.v2.Mutation;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.values.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EventToBigTableRowDoFn extends DoFn<Event, KV<ByteString, Iterable<Mutation>>> {

    public static final String USER_KEY = "USER_";

    private static final String COLUMN_FAMILY = "order_event_details";

    private final ObjectMapperSingletonResource objectMapperSingletonResource;

    private static final Long PAYLOAD_TTL_MILLIS = TimeUnit.DAYS.toMillis(30);

    private static final Logger LOGGER = LoggerFactory.getLogger(EventToBigTableRowDoFn.class);

    @Inject
    public EventToBigTableRowDoFn(ObjectMapperSingletonResource objectMapperSingletonResource) {
        this.objectMapperSingletonResource = objectMapperSingletonResource;
    }

    @ProcessElement
    public void processElement(DoFn<Event, KV<ByteString, Iterable<Mutation>>>.ProcessContext processContext, BoundedWindow boundedWindow) {
        Event event = processContext.element();

        if (event != null) {
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            LOGGER.info("User Id for Bigtable Insertion: {}, key: {}", event.getFields().get("userId"),
                    USER_KEY + event.getEventId() + event.getFields().get("userId"));
            byte[] userKeyBytes = (USER_KEY + event.getEventId() + "_" + event.getFields().get("userId") + "_" + date).getBytes();
            ByteString keyBytes = ByteString.copyFrom(userKeyBytes);

            List<Mutation> mutations = getMutation(event);
            KV<ByteString, Iterable<Mutation>> bigTableRowPair = KV.of(keyBytes, mutations);
            processContext.output(bigTableRowPair);
        }
    }

    private List<Mutation> getMutation(Event event) {
        long timeStampInMicros = event.getTimestamp() * 1000;
        List<Mutation> mutations = new ArrayList<>();
        Map<String, Object> eventMap = objectMapperSingletonResource.getResource().convertValue(event, new TypeReference<>() {
        });
        for (Map.Entry<String, Object> entry : eventMap.entrySet()) {
            Mutation.Builder mutation = Mutation.newBuilder();
            String qualifier = entry.getKey();
            Object fieldValue = entry.getValue();
            String value;
            if (qualifier.equalsIgnoreCase("schema")) {
                if (fieldValue instanceof List<?> || fieldValue instanceof Map<?,?>) {
                    try {
                        value = objectMapperSingletonResource.getResource().writeValueAsString(fieldValue);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    value = fieldValue != null ? String.valueOf(fieldValue) : "";
                }
                mutation.setSetCell(createSetCell(qualifier, value, timeStampInMicros));
                mutations.add(mutation.build());
            }
        }
        return mutations;
    }

    /** * Helper to create a SetCell mutation from strings.
     * If ttlMs is set, the actual timestamp is calculated to be in the future,
     * which Bigtable interprets as the cell's expiration time (TTL).
     */
    private Mutation.SetCell createSetCell(String qualifier, String value, long timestampMicros) {
        Mutation.SetCell.Builder builder = Mutation.SetCell.newBuilder()
                .setFamilyName(EventToBigTableRowDoFn.COLUMN_FAMILY)
                .setColumnQualifier(ByteString.copyFromUtf8(qualifier))
                .setValue(ByteString.copyFrom(value.getBytes(StandardCharsets.UTF_8)));

        long expireTimestampMicros = timestampMicros + (EventToBigTableRowDoFn.PAYLOAD_TTL_MILLIS * 1000);
        builder.setTimestampMicros(expireTimestampMicros);
        return builder.build();
    }
}
