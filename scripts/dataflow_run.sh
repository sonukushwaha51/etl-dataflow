#!/bin/sh

mvn -f ../pom.xml clean install -DskipTests \
    -Dexec.args= " \
        --runner=DataflowRunner \
        --region=us-central1 \
        --project=dataflow-local \
        --pubsubTopic=event-management \
        --pubsubSubscription=event-management-sub \
        --csvPublisherSubscription=csv-publisher-sub \
        --csvFilePath=gs//dataflow-bucket-quickstart/event-csv
    "