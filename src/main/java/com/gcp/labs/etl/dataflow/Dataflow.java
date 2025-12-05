package com.gcp.labs.etl.dataflow;

import com.gcp.labs.etl.dataflow.bigquery.EventToBigQueryTableRowDoFn;
import com.gcp.labs.etl.dataflow.bigtable.EventToBigTableRowDoFn;
import com.gcp.labs.etl.dataflow.datastore.DatastoreModule;
import com.gcp.labs.etl.dataflow.datastore.SaveToDatastoreDoFn;
import com.gcp.labs.etl.dataflow.event.Event;
import com.gcp.labs.etl.dataflow.event.MapPubsubMessageToEventDoFn;
import com.gcp.labs.etl.dataflow.options.EtlDataflowPipelineOptions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigtable.BigtableIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubClient;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTagList;
import org.joda.time.Duration;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.gcp.labs.etl.dataflow.tags.EtlDataflowTupleTag.FAILURE_TAG;
import static com.gcp.labs.etl.dataflow.tags.EtlDataflowTupleTag.SUCCESS_TAG;

public class Dataflow {

    public static void main(String[] args) {

        EtlDataflowPipelineOptions etlDataflowPipelineOptions = PipelineOptionsFactory.fromArgs(args).withValidation().as(EtlDataflowPipelineOptions.class);

        String eventPubsubSubscription = PubsubClient.subscriptionPathFromName(etlDataflowPipelineOptions.getProject(), etlDataflowPipelineOptions.getEventPubsubSubscription()).toString();

        Injector injector = Guice.createInjector(
                new DatastoreModule(etlDataflowPipelineOptions)
        );

        Pipeline pipeline = Pipeline.create(etlDataflowPipelineOptions);
        AvroCoder<Event> avroCoder = AvroCoder.of(Event.class, Event.SCHEMA);
        pipeline.getCoderRegistry().registerCoderForClass(Event.class, avroCoder);

        PCollectionTuple eventTuple = pipeline
                .apply("Read Pubsub Message", PubsubIO.readMessagesWithAttributes().withIdAttribute("id").fromSubscription(eventPubsubSubscription))
                .apply("Window", Window.into(FixedWindows.of(Duration.standardHours(1))))
                .apply("Map PubSub Message to Event", ParDo.of(injector.getInstance(MapPubsubMessageToEventDoFn.class))
                        .withOutputTags(SUCCESS_TAG, TupleTagList.of(FAILURE_TAG)));

        eventTuple.get(FAILURE_TAG)
                .apply("WindowFailures", Window.<String>into(FixedWindows.of(Duration.standardHours(1))))
                .apply("Write failure to gcs", TextIO.write()
                    .to(etlDataflowPipelineOptions.getFailureGcsPath() + "event-error" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                    .withSuffix(".txt")
                    .withoutSharding()
                    .withWindowedWrites()
                    .withOutputFilenames().skipIfEmpty());

        eventTuple.get(SUCCESS_TAG)
                .apply("Save to Datastore", ParDo.of(injector.getInstance(SaveToDatastoreDoFn.class)));

        eventTuple.get(SUCCESS_TAG)
                .apply("Covert event to table Row", ParDo.of(injector.getInstance(EventToBigQueryTableRowDoFn.class)))
                .apply("Write to BigQuery", BigQueryIO.writeTableRows()
                        .to(etlDataflowPipelineOptions.getBigQueryTableName().trim())
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));

        /*
        // Read from BigQuery
        PCollection<TableRow> bqTableRows = pipeline
                        .apply("Read from Bigquery", BigQueryIO.readTableRowsWithSchema()
                                .withQueryLocation("US")
                                .fromQuery("query").usingStandardSql()
                                .withQueryTempDataset("temp_dataset"));

        // Define the Date Format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Calculate the Start and End Keys
        // Start reading from the beginning of yesterday's key space (inclusive)
        String startKeyString = yesterday.format(formatter); // e.g., "20251204"
        ByteString startKey = ByteString.copyFromUtf8(startKeyString);

        String endKeyString = today.format(formatter); // e.g., "20251205"
        ByteString endKey = ByteString.copyFromUtf8(endKeyString);

        // Create the ByteKeyRange
        ByteKeyRange keyRange = ByteKeyRange.newBuilder()
            .setStartKey(startKey)
            .setEndKey(endKey)
            .build();

        // Read from Bigtable
        PCollection<Row> bigTableRows = pipeline
                .apply("Read from Bigtable", BigtableIO.read()
                        .withProjectId(etlDataflowPipelineOptions.getProject())
                        .withInstanceId(etlDataflowPipelineOptions.getBigTableOrdersInstanceId())
                        .withTableId(etlDataflowPipelineOptions.getBigTableOrdersTableId())
                        .withKeyRange(keyRange));
         */

        eventTuple.get(SUCCESS_TAG)
                        .apply("Convert to Bigtable row", ParDo.of(injector.getInstance(EventToBigTableRowDoFn.class)))
                        .apply("Write to Bigtable", BigtableIO.write()
                                .withProjectId(etlDataflowPipelineOptions.getProject())
                                .withInstanceId(etlDataflowPipelineOptions.getBigTableOrdersInstanceId())
                                .withTableId(etlDataflowPipelineOptions.getBigTableOrdersTableId()));

        pipeline.run();


    }
}