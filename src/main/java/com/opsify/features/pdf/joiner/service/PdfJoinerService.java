package com.opsify.features.pdf.joiner.service;

import javafx.collections.ObservableList;

import java.nio.file.Path;

public interface PdfJoinerService {
    Path joinPdfs(ObservableList<String> pdfFiles, String outputDir, String outputFileName) throws Exception;
}
