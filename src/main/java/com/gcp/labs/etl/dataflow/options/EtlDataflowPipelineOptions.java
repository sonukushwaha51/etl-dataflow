package com.gcp.labs.etl.dataflow.options;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Validation;


public interface EtlDataflowPipelineOptions extends DataflowPipelineOptions, PipelineOptions {

    @Description("Subscritption for pubsub topic")
    @Validation.Required
    String getEventPubsubSubscription();

    void setEventPubsubSubscription(String eventPubsubSubscription);

    @Validation.Required
    String getFailureGcsPath();

    void setFailureGcsPath(String failureGcsPath);

    String getBigTableOrdersInstanceId();

    void setBigTableOrdersInstanceId(String bigTableOrdersInstanceId);

    String getBigTableOrdersTableId();

    void setBigTableOrdersTableId(String bigTableOrdersTableId);

    String getBigQueryTableName();

    void setBigQueryTableName(String bigQueryTableName);

}
