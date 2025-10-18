package com.gcp.labs.etl.dataflow;

import com.gcp.labs.etl.dataflow.csvProcessor.ExtractCsvFilePathDoFn;
import com.gcp.labs.etl.dataflow.csvProcessor.ReadFromStorageToEventDoFn;
import com.gcp.labs.etl.dataflow.datastore.SaveToDatastoreDoFn;
import com.gcp.labs.etl.dataflow.event.Event;
import com.gcp.labs.etl.dataflow.event.MapPubsubMessageToEventDoFn;
import com.gcp.labs.etl.dataflow.options.EtlDataflowPipelineOptions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubClient;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

public class Dataflow {

    public static void main(String[] args) {

        EtlDataflowPipelineOptions etlDataflowPipelineOptions = PipelineOptionsFactory.fromArgs(args).as(EtlDataflowPipelineOptions.class);

        String eventPubsubTopic = PubsubClient.topicPathFromName(etlDataflowPipelineOptions.getProject(), etlDataflowPipelineOptions.getPubsubTopic()).toString();
        String eventPubsubSubscription = PubsubClient.subscriptionPathFromName(etlDataflowPipelineOptions.getProject(), etlDataflowPipelineOptions.getPubsubSubscription()).toString();

        Injector injector = Guice.createInjector();

        Pipeline pipeline = Pipeline.create(etlDataflowPipelineOptions);

        PCollection<Event> collection = pipeline
                .apply("Read Pubsub Message", PubsubIO.readMessagesWithAttributes().withIdAttribute("id").fromSubscription(eventPubsubSubscription))
                .apply("Map PubSub Message to Event", ParDo.of(injector.getInstance(MapPubsubMessageToEventDoFn.class)));

        PCollection<Event> excelProcessingCollection = pipeline
                .apply("Read from CSV subscription", PubsubIO.readMessagesWithAttributes().withIdAttribute("id").fromSubscription(etlDataflowPipelineOptions.getCsvPublisherSubscription()))
                .apply("Extract CSV file Path", ParDo.of(injector.getInstance(ExtractCsvFilePathDoFn.class)))
                .apply("Read from storage bucket and convert to event", ParDo.of(injector.getInstance(ReadFromStorageToEventDoFn.class)));

        PCollectionList<Event> pCollectionList = PCollectionList.of(collection).and(excelProcessingCollection);
        PCollection<Event> flattenedCollection = pCollectionList
                .apply("Flatten", Flatten.pCollections());

        flattenedCollection
                .apply("Save to Datastore", ParDo.of(injector.getInstance(SaveToDatastoreDoFn.class)));

        pipeline.run();


    }
}