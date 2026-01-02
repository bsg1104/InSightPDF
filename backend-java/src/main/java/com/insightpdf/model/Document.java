package com.insightpdf.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filename;
    private String inputPath;
    private String outputPath;
    private String status;
    private Instant processedAt;
    private double processingTime;
    private int redactedCount;
    private int version = 1;

    // getters/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String f) {
        this.filename = f;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String p) {
        this.inputPath = p;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String p) {
        this.outputPath = p;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String s) {
        this.status = s;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant t) {
        this.processedAt = t;
    }

    public double getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(double v) {
        this.processingTime = v;
    }

    public int getRedactedCount() {
        return redactedCount;
    }

    public void setRedactedCount(int c) {
        this.redactedCount = c;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int v) {
        this.version = v;
    }
}