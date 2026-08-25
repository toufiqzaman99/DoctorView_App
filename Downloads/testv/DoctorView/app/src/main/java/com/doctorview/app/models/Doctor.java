package com.doctorview.app.models;

import java.util.Map;

/**
 * Doctor model.
 * Will be stored in the Cloud Firestore "doctors" collection.
 */
public class Doctor {

    private String id;
    private String name;
    private String specialty;
    private String hospital;
    private int experienceYears;
    private double fee;
    private double rating;
    private String imageUrl;
    private String about;
    private Map<String, Object> availability;

    public Doctor() {
        // Required by Firestore
    }

    public Doctor(String id, String name, String specialty, String hospital,
                  int experienceYears, double fee, double rating, String imageUrl, String about) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.hospital = hospital;
        this.experienceYears = experienceYears;
        this.fee = fee;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.about = about;
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

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public Map<String, Object> getAvailability() {
        return availability;
    }

    public void setAvailability(Map<String, Object> availability) {
        this.availability = availability;
    }
}
