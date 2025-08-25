package com.opsify.features.pdf.joiner.service;

import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class PdfJoinerServiceImpl implements PdfJoinerService {

    @Override
    public Path joinPdfs(ObservableList<String> pdfFiles, String outputDir, String outputFileName) throws Exception {
        if (pdfFiles == null || pdfFiles.isEmpty()) {
            throw new IllegalArgumentException("No PDF files provided for joining");
        }

        // Validate all input files exist and are PDFs
        for (String filePath : pdfFiles) {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("File does not exist: " + filePath);
            }
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                throw new IOException("File is not a PDF: " + filePath);
            }
        }

        // Create output directory if it doesn't exist
        File outputFile = getOutputFile(outputDir, outputFileName);

        // Merge the PDFs
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(outputFile.getAbsolutePath());

        for (String filePath : pdfFiles) {
            merger.addSource(new File(filePath));
        }

        // Perform the merge using default
        merger.mergeDocuments(null);

        log.info("Successfully merged {} PDFs to: {}", pdfFiles.size(), outputFile.getAbsolutePath());
        return Paths.get(outputFile.getAbsolutePath());
    }

    private static File getOutputFile(String outputDir, String outputFileName) throws IOException {
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {
                throw new IOException("Failed to create output directory: " + outputDir);
            }
        }

        // Handle file name conflicts
        String baseName = outputFileName;
        if (baseName.toLowerCase().endsWith(".pdf")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }

        File outputFile;
        int counter = 1;
        do {
            String fileName = (counter == 1) ?
                    baseName + ".pdf" :
                    baseName + " (" + counter + ").pdf";
            outputFile = new File(outputDirectory, fileName);
            counter++;
        } while (outputFile.exists());
        return outputFile;
    }
}
