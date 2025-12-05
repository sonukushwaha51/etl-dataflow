package com.gcp.labs.etl.dataflow.bigquery;

import com.google.cloud.bigquery.storage.v1.TableFieldSchema;
import com.google.cloud.bigquery.storage.v1.TableSchema;
import java.util.ArrayList;
import java.util.List;

public class OrdersTableSchema {

    public TableSchema getOrdersTableSchema() {

        List<TableFieldSchema> fieldSchemas = new ArrayList<>();

        // String field
        TableFieldSchema userIdField = TableFieldSchema.newBuilder()
                .setName("user_id")
                .setType(TableFieldSchema.Type.STRING)
                .setMode(TableFieldSchema.Mode.REQUIRED)
                .build();

        // Repeated Array record field
        TableFieldSchema purchasesField = TableFieldSchema.newBuilder()
                .setName("purchases")
                .setType(TableFieldSchema.Type.STRUCT)
                .setMode(TableFieldSchema.Mode.REPEATED)
                .build();

        fieldSchemas.add(userIdField);

        return TableSchema.newBuilder()
                .addAllFields(fieldSchemas)
                .build();
    }
}
