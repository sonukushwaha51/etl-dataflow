package com.gcp.labs.etl.dataflow.datastore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gcp.labs.etl.dataflow.event.Event;
import com.gcp.labs.etl.dataflow.singleton.supplier.ObjectMapperSingletonResource;
import com.google.cloud.datastore.*;
import com.google.inject.Inject;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class DatastoreService implements Serializable {

    private transient Datastore datastore;

    private final ObjectMapperSingletonResource objectMapperSingletonResource;

    private final static String ORDER_MESSAGE_KIND = "Order_Message";

    private final static String CUSTOMER_KIND = "Customer";

    private final Logger LOGGER = LoggerFactory.getLogger(DatastoreService.class);

    private static final RetryConfig RETRY_CONFIG = RetryConfig.custom()
            .waitDuration(Duration.ofSeconds(10))
            .maxAttempts(5)
            .failAfterMaxAttempts(true)
            .retryOnException(exception -> exception instanceof DatastoreException)
            .build();

    // Lazy initialization synchronized for safety on worker threads
    public synchronized Datastore getDatastore() {
        if (this.datastore == null) {
            // Initialization happens HERE, on the worker machine, when first called
            this.datastore = DatastoreOptions.getDefaultInstance()
                    .getService();
        }
        return this.datastore;
    }

    @Inject
    public DatastoreService(ObjectMapperSingletonResource objectMapperSingletonResource) {
        this.objectMapperSingletonResource = objectMapperSingletonResource;
    }

    public void addMessagesToDatastore(Event event) {
       String userId = (String) event.getFields().get("userId");
       Retry.of("PUT_RETRY", RETRY_CONFIG).executeRunnable(() -> {
            Transaction transaction = getDatastore().newTransaction();
           try {
               if (userId != null) {
                   Key userKey = getCustomerKey().newKey(userId);
                   transaction.put(getUserEntity(userKey));
               }
               Entity orderMessageEntity = getOrderMessageEntity(event, userId);
               transaction.put(orderMessageEntity);
               transaction.commit();
           } catch (Exception exception) {
                LOGGER.error("Error while saving the order purchases from event: {}", event, exception);
               transaction.rollback();
           } finally {
               if (transaction.isActive()) {
                   transaction.rollback();
               }
           }
       });
    }

    public Entity getUserEntity(Key userId) {
        return Entity.newBuilder(userId)
                .set("modified_date", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .build();
    }

    public Entity getOrderMessageEntity(Event event, String userId) throws JsonProcessingException {
        Key messageKey = getMessageKey(userId).newKey(UUID.randomUUID().toString());
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getTransaction().getTransactionDate()), ZoneId.of("Asia/Kolkata"));
        return Entity.newBuilder(messageKey)
                .set("event_id", event.getEventId())
                .set("order_number", event.getOrderNumber())
                .set("order_timestamp", event.getTimestamp())
                .set("purchases", objectMapperSingletonResource.getResource().writeValueAsString(event.getPurchases()))
                .set("user_id", userId)
                .set("email", (String) event.getFields().get("email"))
                .set("transaction_method", event.getTransaction().getTransactionMethod())
                .set("transaction_id", event.getTransaction().getTransactionId())
                .set("transaction_amount", event.getTransaction().getTransactionAmount())
                .set("transaction_date", zonedDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .set("message_updated_date", String.valueOf(ZonedDateTime.now()))
                .build();
    }

    public KeyFactory getMessageKey(String userId) {
        return getDatastore().newKeyFactory().addAncestor(PathElement.of("Customer", userId)).setKind(ORDER_MESSAGE_KIND);
    }

    public KeyFactory getCustomerKey() {
        return getDatastore().newKeyFactory().setKind(CUSTOMER_KIND);
    }
}
