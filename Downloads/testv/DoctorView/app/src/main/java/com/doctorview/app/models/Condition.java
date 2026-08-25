package com.doctorview.app.models;

import java.util.Arrays;
import java.util.List;

/**
 * One condition in the symptom-analysis rule base.
 * Holds the condition name, its typical symptoms, simple advice
 * and a severity level used to color the result card.
 */
public class Condition {

    // Severity levels (used by SymptomAnalyzer's rule base)
    public static final String SEVERITY_LOW = "low";
    public static final String SEVERITY_MEDIUM = "medium";
    public static final String SEVERITY_HIGH = "high";
    public static final String SEVERITY_EMERGENCY = "emergency";

    private final String name;
    private final List<String> symptoms;
    private final String advice;
    private final String severity;

    public Condition(String name, String advice, String severity, String... symptoms) {
        this.name = name;
        this.advice = advice;
        this.severity = severity;
        this.symptoms = Arrays.asList(symptoms);
    }

    public String getName() {
        return name;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public String getAdvice() {
        return advice;
    }

    public String getSeverity() {
        return severity;
    }
}
