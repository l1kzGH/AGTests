package com.likz.agtests.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.likz.agtests.config.PluginSettings.CustomerFramework.ANY;
import static com.likz.agtests.config.PluginSettings.GenerationPattern.NO_PATTERN;

public class PluginSettings {

    @JsonProperty("directory")
    private String customerDirectory = "";
    @JsonProperty("framework")
    private int customerFramework = ANY.ordinal();
    @JsonProperty("testname")
    private String customerTestName = "[class_name]Test";
    @JsonProperty("pattern")
    private int generationPattern = NO_PATTERN.ordinal();

    public PluginSettings() {
    }

    public PluginSettings(String customerDirectory,
                          int customerFramework,
                          String customerTestName,
                          int generationPattern) {
        this.customerDirectory = customerDirectory;
        this.customerFramework = customerFramework;
        this.customerTestName = customerTestName;
        this.generationPattern = generationPattern;
    }

    enum CustomerFramework {
        ANY, JUnit;
    }

    enum GenerationPattern {
        NO_PATTERN, AAA_PATTERN, BDD_PATTERN;
    }

    public String getCustomerDirectory() {
        return customerDirectory;
    }

    public int getCustomerFramework() {
        return customerFramework;
    }

    public String getCustomerTestName() {
        return customerTestName;
    }

    public int getGenerationPattern() {
        return generationPattern;
    }

    public void setCustomerDirectory(String customerDirectory) {
        this.customerDirectory = customerDirectory;
    }

    public void setCustomerFramework(int customerFramework) {
        this.customerFramework = customerFramework;
    }

    public void setCustomerTestName(String customerTestName) {
        this.customerTestName = customerTestName;
    }

    public void setGenerationPattern(int generationPattern) {
        this.generationPattern = generationPattern;
    }
}
