package com.gcp.labs.etl.dataflow.bigquery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gcp.labs.etl.dataflow.event.Event;
import com.gcp.labs.etl.dataflow.event.Purchase;
import com.gcp.labs.etl.dataflow.singleton.supplier.ObjectMapperSingletonResource;
import com.google.api.services.bigquery.model.TableRow;
import com.google.inject.Inject;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventToBigQueryTableRowDoFn extends DoFn<Event, TableRow> {

    private final ObjectMapperSingletonResource objectMapperSingletonResource;

    private static final Logger LOGGER = LoggerFactory.getLogger(EventToBigQueryTableRowDoFn.class);
    @Inject
    public EventToBigQueryTableRowDoFn(ObjectMapperSingletonResource objectMapperSingletonResource) {
        this.objectMapperSingletonResource = objectMapperSingletonResource;
    }

    @ProcessElement
    public void processElement(DoFn<Event, TableRow>.ProcessContext context, BoundedWindow boundedWindow) {
        Event event = context.element();
        if (event != null) {
            String purchaseString;
            try {
                List<Purchase> purchases = event.getPurchases();
                purchaseString = objectMapperSingletonResource.getResource().writeValueAsString(purchases);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
            List<TableRow> purchaseTableRows = new ArrayList<>();
            for (Purchase purchase : event.getPurchases()) {
                TableRow purchaseRow = new TableRow();
                purchaseRow.set("skuId", purchase.getSkuId());
                purchaseRow.set("currentPrice", purchase.getCurrentPrice());
                purchaseTableRows.add(purchaseRow);
            }
            DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getTransaction().getTransactionDate()), ZoneId.of("Asia/Kolkata"));
            TableRow tableRow = new TableRow();
            tableRow.set("order_number", event.getOrderNumber());
            tableRow.set("eventId", event.getEventId());
            tableRow.set("userId", event.getFields().get("userId"));
            tableRow.set("transactionDate", zonedDateTime.format(customFormatter));
            tableRow.set("orderDeliveredDate", event.getFields().get("orderDeliveredDate"));
            tableRow.set("orderShippedDate", event.getFields().get("orderShippedDate"));
            tableRow.set("transactionAmount", event.getTransaction().getTotalAmount());
            tableRow.set("discount", event.getTransaction().getDiscount());
            tableRow.set("totalAmount", event.getTransaction().getTotalAmount());
            tableRow.set("orderInsertTime", LocalDateTime.now().format(customFormatter));
            tableRow.set("payment_method", event.getTransaction().getTransactionMethod());
            tableRow.set("purchasedItems", purchaseString);
            tableRow.set("purchases", purchaseTableRows);
            tableRow.set("email", event.getFields().get("email"));
            if (tableRow.isEmpty()) {
                LOGGER.error("Empty table row. hence rejecting write to bigquery");
            }
            context.output(tableRow);
        }
    }
}
