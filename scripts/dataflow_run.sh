#!/usr/bin/env bash

mvn -f ../pom.xml clean install exec.java \
    -Dexec.mainClass=com.gcp.labs.etldataflow.Dataflow \
    -DskipTests \
    -Dexec.args= " \
        --runner=DirectRunner \
        --region=us-central1 \
        --project=eighth-saga-474816-a6 \
        --eventPubsubSubscription=event-management-sub \
        --failureGcsPath=gs://event-bucket/failure/ \
        --tempLocation=gs://dataflow-bucket-quickstart/etl-dataflow/temp/ \
        --stagingLocation=gs://dataflow-bucket-quickstart/etl-dataflow/staging/
    "