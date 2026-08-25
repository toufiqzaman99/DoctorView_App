package com.doctorview.app.models;

/**
 * Appointment model.
 * Stored in the Cloud Firestore "appointments" collection.
 * Status is one of: "pending", "confirmed", "completed", "cancelled".
 * The date is stored as "yyyy-MM-dd" so lists can sort it easily.
 */
public class Appointment {

    private String id;
    private String userId;
    private String doctorId;
    private String doctorName;
    private String specialty;
    private String date;
    private String time;
    private String status;
    private String note;
    private String patientName;
    private String reason;

    public Appointment() {
        // Required by Firestore
    }

    public Appointment(String id, String userId, String doctorId, String doctorName,
                       String date, String time, String status) {
        this.id = id;
        this.userId = userId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
