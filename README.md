# etl-dataflow

# Run Dataflow in local

Run dataflow_run.sh

# Writing to BigQuery
We should convert message to `TableRow` in order to write to bigQuery.
`.apply("Write to BigQuery", BigQueryIO.writeTableRows()
.to(etlDataflowPipelineOptions.getBigQueryTableName())
.withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
.withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));`

* BigQueryIO.Write.CreateDisposition.CREATE_NEVER - Will not create table in bigquery if table does not exist
* BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED -  Will create table if it is not present. It requires schema to be passed as well using `withSchema(TableSchema schema)`. FOr schema, Refer `OrdersTableSchema` class
* BigQueryIO.Write.WriteDisposition.WRITE_APPEND) - Will append the existing row if it already exists.
* 
**For Writing to bigQuery, we should add bigquery table name in pipeline options in below format**

`project-id:dataset_name.table_name`

# Writing to Bigtable

In bigtable Data is written as Row. For writing as Row we need to have a PCollection of `KV<String, List<Mutation>>` where,
* key - ByteString row key
* list of mutation - List of columns to be added to bigtable

Mutation is built from Mutation.SetCell

`mutation.setSetCell(createSetCell(COLUMN_FAMILY, field.getName(), value, timeStampInMicros, PAYLOAD_TTL_MILLIS));`

Cells are created using Mutation.Cell Builder and take ColumnFamily, Column Qualifiers (column name), column value, timestampMicros (time to leave value)

`private Mutation.SetCell createSetCell(String familyName, String qualifier, String value, long timestampMicros, Long ttlMs) {
    Mutation.SetCell.Builder builder = Mutation.SetCell.newBuilder()
    .setFamilyName(familyName)
    .setColumnQualifier(ByteString.copyFromUtf8(qualifier))
    .setValue(ByteString.copyFrom(value.getBytes(StandardCharsets.UTF_8)));
    if (ttlMs != null) {
        long expireTimestampMicros = timestampMicros + (ttlMs * 1000);
        builder.setTimestampMicros(expireTimestampMicros);
    }
    return builder.build();
}`

For writing to bigtable, we need projectId, instanceId and tableId
