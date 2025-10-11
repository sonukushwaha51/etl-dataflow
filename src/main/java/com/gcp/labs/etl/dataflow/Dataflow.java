package com.gcp.labs.etl.dataflow;

import com.gcp.labs.etl.dataflow.event.MapPubsubMessageToEvent;
import com.gcp.labs.etl.dataflow.options.EtlDataflowPipelineOptions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubClient;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

public class Dataflow {
    public static void main(String[] args) {

        EtlDataflowPipelineOptions etlDataflowPipelineOptions = PipelineOptionsFactory.fromArgs(args).as(EtlDataflowPipelineOptions.class);

        String eventPubsubTopic = PubsubClient.topicPathFromName(etlDataflowPipelineOptions.getProject(), etlDataflowPipelineOptions.getPubsubTopic()).toString();
        String eventPubsubSubscription = PubsubClient.subscriptionPathFromName(etlDataflowPipelineOptions.getProject(), etlDataflowPipelineOptions.getPubsubSubscription()).toString();

        Injector injector = Guice.createInjector();

        Pipeline pipeline = Pipeline.create(etlDataflowPipelineOptions);

        PCollection<String> collection = pipeline
                .apply("Read Pubsub Message", PubsubIO.readMessagesWithAttributes().withIdAttribute("id").fromSubscription(eventPubsubSubscription))
                .apply("Map PubSub Message to Event", ParDo.of(injector.getInstance(MapPubsubMessageToEvent.class)));

    }
}