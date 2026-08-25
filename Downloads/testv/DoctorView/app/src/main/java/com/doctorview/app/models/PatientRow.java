package com.doctorview.app.models;

/**
 * Lightweight patient list row for the Doctor side
 * (built from the doctor's appointments — no extra collection needed).
 */
public class PatientRow {

    private final String userId;
    private final String name;
    private final String subtitle;

    public PatientRow(String userId, String name, String subtitle) {
        this.userId = userId;
        this.name = name;
        this.subtitle = subtitle;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
