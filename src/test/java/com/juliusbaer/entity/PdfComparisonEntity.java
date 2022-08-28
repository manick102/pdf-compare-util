package com.juliusbaer.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Builder
@Getter
@Setter
public class PdfComparisonEntity {

    private String tapFilePath;
    private String assetFilePath;
    private String smrFilePath;
    private Integer index;
    private String identifier;
    private String removedFirstPageSmrFilePath;
    private String mergedFilePath;
    private String resultFilePath;

}
