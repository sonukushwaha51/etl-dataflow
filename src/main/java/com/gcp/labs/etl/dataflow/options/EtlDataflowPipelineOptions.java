package com.gcp.labs.etl.dataflow.options;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Validation;
import org.jetbrains.annotations.NotNull;


public interface EtlDataflowPipelineOptions extends DataflowPipelineOptions, PipelineOptions {

    @Description("Project Id")
    @Validation.Required
    @NotNull
    String getProject();

    void setProjectId(String projectId);

    @Description("Pub sub topic on which message will be posted")
    @Validation.Required
    String getPubsubTopic();

    void setPubsubTopic(String pubsubTopic);

    @Description("Subscritption for pubsub topic")
    @Validation.Required
    String getPubsubSubscription();

    void setPubsubSubscription();

}
