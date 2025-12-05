#!/usr/bin/env bash

mvn -f ../pom.xml clean install exec:java \
    -Dexec.mainClass=com.gcp.labs.etl.dataflow.Dataflow \
    -DskipTests \
    -Dexec.args=" \
        --runner=DirectRunner \
        --region=us-central1 \
        --project=eighth-saga-474816-a6 \
        --eventPubsubSubscription=orders-sub \
        --failureGcsPath=gs://orders-event-bucket/errors/ \
        --tempLocation=gs://dataflow-bucket-quickstart/etl-dataflow/temp/ \
        --stagingLocation=gs://dataflow-bucket-quickstart/etl-dataflow/staging/ \
        --bigTableOrdersInstanceId=orders-event-instance \
        --bigTableOrdersTableId=orders_event \
        --bigQueryTableName=eighth-saga-474816-a6:orders.orders
    "