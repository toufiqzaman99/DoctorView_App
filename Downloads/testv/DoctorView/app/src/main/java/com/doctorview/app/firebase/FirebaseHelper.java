package com.doctorview.app.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Central helper that gives every screen access to the same Firebase services.
 *
 * IMPORTANT: before Firebase works for real, replace
 * app/google-services.json with the file from YOUR Firebase Console
 * project (see README.md → "Connect Firebase").
 */
public class FirebaseHelper {

    private static FirebaseAuth auth;
    private static FirebaseFirestore firestore;
    private static DatabaseReference realtimeDatabase;
    private static FirebaseStorage storage;

    private FirebaseHelper() {
        // Static helper — no instances needed
    }

    /** Firebase Authentication — login / register / logout. */
    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    /** Cloud Firestore — doctors, appointments, users, medical records. */
    public static FirebaseFirestore getFirestore() {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
        }
        return firestore;
    }

    /** Realtime Database — live chat between patient and doctor. */
    public static DatabaseReference getRealtimeDatabase() {
        if (realtimeDatabase == null) {
            realtimeDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return realtimeDatabase;
    }

    /** Firebase Storage — attached files of medical records. */
    public static FirebaseStorage getStorage() {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        return storage;
    }
}
