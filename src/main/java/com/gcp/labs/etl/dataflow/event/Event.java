package com.gcp.labs.etl.dataflow.event;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.List;
import java.util.Map;

public class Event {

    private String eventId;

    private String orderNumber;

    private long timestamp;

    private String eventUuid;

    private List<Purchase> purchases;

    private Transaction transaction;

    private Map<String, Object> fields;

    public Event() {
    }

    public String getEventId() {
        return eventId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEventUuid() {
        return eventUuid;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setEventUuid(String eventUuid) {
        this.eventUuid = eventUuid;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public static final Schema SCHEMA = SchemaBuilder.record(Event.class.getSimpleName())
            .namespace(Event.class.getPackageName()) // Ensure the namespace matches your package
            .fields()
            .name("eventId").type().stringType().noDefault()
            .name("orderNumber").type().stringType().noDefault()
            .name("timestamp").type().longType().noDefault()
            .name("eventUuid").type().stringType().noDefault()
            .name("purchases").type().array().items(Purchase.SCHEMA).noDefault()
            .name("transaction").type(Transaction.SCHEMA).noDefault()
            .name("fields").type().map().values()
            .unionOf()
            .stringType().and()
            .longType().and()
            .booleanType().and()
            .doubleType().and()
            .nullType().and()
            .array().items().map().values().unionOf().stringType().and().booleanType().and().longType().and().doubleType().endUnion()
            .endUnion()
            .noDefault()
            .endRecord();

}
