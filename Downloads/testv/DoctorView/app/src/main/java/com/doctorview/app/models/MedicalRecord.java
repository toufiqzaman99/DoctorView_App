package com.doctorview.app.models;

/**
 * One entry of the user's medical history.
 * Stored in the Cloud Firestore "medical_records" collection.
 * If a file is attached, fileUrl points to Firebase Storage.
 */
public class MedicalRecord {

    private String id;
    private String userId;
    private String title;
    private String type; // Lab Report, Prescription, Visit Summary, Vaccination
    private String date; // yyyy-MM-dd
    private String note;
    private String fileUrl;
    private String fileName;

    public MedicalRecord() {
        // Required by Firestore
    }

    public MedicalRecord(String id, String userId, String title, String type,
                         String date, String note, String fileUrl, String fileName) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.type = type;
        this.date = date;
        this.note = note;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
