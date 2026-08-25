package com.doctorview.app.models;

import java.util.List;

/**
 * One entry of the disease information library.
 * Stored in the Cloud Firestore "diseases" collection.
 */
public class Disease {

    private String id;
    private String name;
    private String category; // Respiratory, Infectious, Chronic, Digestive, ...
    private String overview;
    private List<String> symptoms;
    private List<String> causes;
    private List<String> prevention;
    private List<String> treatment;

    public Disease() {
        // Required by Firestore
    }

    public Disease(String id, String name, String category, String overview,
                   List<String> symptoms, List<String> causes,
                   List<String> prevention, List<String> treatment) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.overview = overview;
        this.symptoms = symptoms;
        this.causes = causes;
        this.prevention = prevention;
        this.treatment = treatment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getCauses() {
        return causes;
    }

    public void setCauses(List<String> causes) {
        this.causes = causes;
    }

    public List<String> getPrevention() {
        return prevention;
    }

    public void setPrevention(List<String> prevention) {
        this.prevention = prevention;
    }

    public List<String> getTreatment() {
        return treatment;
    }

    public void setTreatment(List<String> treatment) {
        this.treatment = treatment;
    }
}
