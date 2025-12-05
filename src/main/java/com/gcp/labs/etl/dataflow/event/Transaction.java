package com.gcp.labs.etl.dataflow.event;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public class Transaction {

    private Long transactionId;

    private String transactionMethod;

    private Long transactionDate;

    private Double transactionAmount;

    private Double totalAmount;

    private Double discount;

    public Transaction() {
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionMethod() {
        return transactionMethod;
    }

    public void setTransactionMethod(String transactionMethod) {
        this.transactionMethod = transactionMethod;
    }

    public Long getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Long transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public static final Schema SCHEMA = SchemaBuilder.builder()
            .record(Transaction.class.getSimpleName())
            .namespace(Transaction.class.getPackageName())
            .fields()
            .name("transactionId").type().longType().noDefault()
            .name("transactionMethod").type().stringType().noDefault()
            .name("transactionDate").type().longType().noDefault()
            .name("transactionAmount").type().doubleType().noDefault()
            .name("totalAmount").type().doubleType().noDefault()
            .name("discount").type().doubleType().doubleDefault(0.00)
            .endRecord();
}
