package com.doctorview.app.models;

import com.doctorview.app.utils.Constants;

import java.util.Map;

/**
 * User account model.
 * Will be stored in the Cloud Firestore "users" collection.
 * The empty constructor is required by Firestore to convert
 * documents into objects automatically.
 */
public class User {

    private String uid;
    private String name;
    private String email;
    private String phone;
    private String role;       // legacy: "patient" or "doctor"
    private String userType;   // new: "PATIENT" or "DOCTOR"
    private String specialty;
    private String hospital;
    private String qualification;
    private int experienceYears;
    private double consultationFee;
    private String about;
    private Map<String, Object> availability;

    public User() {
        // Required by Firestore
    }

    public User(String uid, String name, String email, String phone, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public User(String uid, String name, String email, String phone, String userType,
                String specialty, String hospital, String qualification,
                int experienceYears, double consultationFee) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = Constants.USER_TYPE_PATIENT.equals(userType) ? "patient" : "doctor";
        this.userType = userType;
        this.specialty = specialty;
        this.hospital = hospital;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
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

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
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
