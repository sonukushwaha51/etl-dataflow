package com.gcp.labs.etl.dataflow.csvProcessor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvFile {

    private String csvFilePath;

    private long timestamp;

    private String csvFileName;
}
