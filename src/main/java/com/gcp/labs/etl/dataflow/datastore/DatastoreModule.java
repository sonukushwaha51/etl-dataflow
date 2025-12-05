package com.gcp.labs.etl.dataflow.datastore;

import com.gcp.labs.etl.dataflow.options.EtlDataflowPipelineOptions;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DatastoreModule extends AbstractModule {

    private final EtlDataflowPipelineOptions etlDataflowPipelineOptions;

    @Inject
    public DatastoreModule(EtlDataflowPipelineOptions etlDataflowPipelineOptions) {
        this.etlDataflowPipelineOptions = etlDataflowPipelineOptions;
    }

    @Provides
    @Singleton
    public Datastore provideDatastore() {
        return DatastoreOptions.newBuilder()
                .setNamespace("default")
                .setProjectId(etlDataflowPipelineOptions.getProject())
                .build()
                .getService();
    }
}
