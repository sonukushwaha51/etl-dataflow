package com.gcp.labs.etl.dataflow.event;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public class Purchase {

    private String skuId;

    private Double currentPrice;

    public Purchase() {
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public static final Schema SCHEMA = SchemaBuilder.builder()
            .record(Purchase.class.getSimpleName())
            .namespace(Purchase.class.getPackageName())
            .fields()
            .name("skuId").type().stringType().noDefault()
            .name("currentPrice").type().doubleType().noDefault()
            .endRecord();

}
