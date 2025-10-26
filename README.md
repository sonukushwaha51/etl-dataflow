# etl-dataflow

# Run Dataflow in local

`mvn clean install -DskipTests exec:java -Dexec.mainClass='com.gcp.labs.etl.dataflow.Dataflow' -Dexec.args='--runner=DataflowRunner --region=us-central1 --project=dataflow-local --pubsubTopic=event-management --pubsubSubscription=event-management-sub --csvPublisherSubscription=csv-publisher-sub --csvFilePath=gs://dataflow-bucket-quickstart/event-csv'
`