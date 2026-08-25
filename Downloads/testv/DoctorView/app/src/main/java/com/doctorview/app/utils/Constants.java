package com.doctorview.app.utils;

/**
 * Central place for all fixed values used across the app.
 * Keeping names here prevents typos when the same collection
 * name is used in many screens.
 */
public final class Constants {

    private Constants() {
        // No instances
    }

    // Cloud Firestore collection names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_DOCTORS = "doctors";
    public static final String COLLECTION_APPOINTMENTS = "appointments";
    public static final String COLLECTION_MEDICAL_RECORDS = "medical_records";
    public static final String COLLECTION_NEWS = "news";
    public static final String COLLECTION_DISEASES = "diseases";

    // Realtime Database root for chat messages
    public static final String RTDB_CHATS = "chats";

    // User roles (legacy values, kept for backward compatibility)
    public static final String ROLE_PATIENT = "patient";
    public static final String ROLE_DOCTOR = "doctor";

    // New userType values stored on the profile document
    public static final String USER_TYPE_PATIENT = "PATIENT";
    public static final String USER_TYPE_DOCTOR = "DOCTOR";

    // Appointment status values (stored lowercase for backward compatibility)
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    // Availability day keys
    public static final String[] DAYS = {
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"
    };

    // Medical record types
    public static final String RECORD_TYPE_LAB = "Lab Report";
    public static final String RECORD_TYPE_PRESCRIPTION = "Prescription";
    public static final String RECORD_TYPE_VISIT = "Visit Summary";
    public static final String RECORD_TYPE_VACCINATION = "Vaccination";
}
