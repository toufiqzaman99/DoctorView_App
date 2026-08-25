package com.doctorview.app.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.MedicalRecord;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Add Record: title, type, date, note and an optional file
 * uploaded to Firebase Storage. The record is saved to Firestore.
 */
public class AddRecordFragment extends Fragment {

    private final String[] recordTypes = {
            Constants.RECORD_TYPE_LAB,
            Constants.RECORD_TYPE_PRESCRIPTION,
            Constants.RECORD_TYPE_VISIT,
            Constants.RECORD_TYPE_VACCINATION
    };

    private TextInputLayout tilTitle;
    private TextInputEditText etTitle;
    private ChipGroup chipGroupType;
    private TextInputEditText etDate;
    private TextInputEditText etNote;
    private TextView tvFileName;
    private Button btnPickFile;
    private Button btnSave;
    private ProgressBar progressBar;
    private ActivityResultLauncher<String> filePickerLauncher;

    private String selectedType = Constants.RECORD_TYPE_LAB;
    private String selectedDate; // yyyy-MM-dd
    private String fileUrl;
    private String fileName;

    public AddRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pick any file from the device (no storage permission needed — SAF)
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), this::onFilePicked);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilTitle = view.findViewById(R.id.tilTitle);
        etTitle = view.findViewById(R.id.etTitle);
        chipGroupType = view.findViewById(R.id.chipGroupType);
        etDate = view.findViewById(R.id.etDate);
        etNote = view.findViewById(R.id.etNote);
        tvFileName = view.findViewById(R.id.tvFileName);
        btnPickFile = view.findViewById(R.id.btnPickFile);
        btnSave = view.findViewById(R.id.btnSave);
        progressBar = view.findViewById(R.id.progressBar);
        TextInputLayout tilDate = view.findViewById(R.id.tilDate);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        buildTypeChips();

        // The date defaults to today, but can be changed with the calendar
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date()));
        tilDate.setOnClickListener(v -> showDatePicker());
        etDate.setOnClickListener(v -> showDatePicker());

        btnPickFile.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        btnSave.setOnClickListener(v -> saveRecord());
    }

    /** Creates one selectable chip per record type. */
    private void buildTypeChips() {
        for (int i = 0; i < recordTypes.length; i++) {
            Chip chip = new Chip(requireContext());
            chip.setText(recordTypes[i]);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            chipGroupType.addView(chip);
        }
        chipGroupType.setSingleSelection(true);
        chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedType = chip.getText().toString();
                }
            }
        });
    }

    /** Opens the calendar dialog (any date allowed — records can be in the past). */
    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.date_picker_title)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Date date = new Date(selection);
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
            etDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(date));
        });
        picker.show(getParentFragmentManager(), "recordDatePicker");
    }

    /** Uploads the picked file to Firebase Storage and keeps its download URL. */
    private void onFilePicked(Uri uri) {
        if (uri == null) {
            return;
        }
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            return;
        }

        fileName = queryDisplayName(uri);
        StorageReference fileRef = FirebaseHelper.getStorage()
                .getReference("medical_records")
                .child(user.getUid())
                .child(System.currentTimeMillis() + "_" + fileName);

        showUploading(true);
        fileRef.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    fileUrl = downloadUri.toString();
                    tvFileName.setVisibility(View.VISIBLE);
                    tvFileName.setText(fileName);
                    showUploading(false);
                    AppUtils.showToast(requireContext(), R.string.file_attached);
                })
                .addOnFailureListener(e -> {
                    showUploading(false);
                    AppUtils.showToast(requireContext(), R.string.upload_failed);
                });
    }

    /** Reads the picked file's display name (e.g. "blood_test.pdf"). */
    private String queryDisplayName(Uri uri) {
        try (Cursor cursor = requireContext().getContentResolver()
                .query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = cursor.getString(index);
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall through to the generated name
        }
        return "file_" + System.currentTimeMillis();
    }

    /** Validates the input and saves the record to Firestore. */
    private void saveRecord() {
        tilTitle.setError(null);

        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            tilTitle.setError(getString(R.string.title_required));
            return;
        }
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            return;
        }

        String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";
        MedicalRecord record = new MedicalRecord(null, user.getUid(), title, selectedType,
                selectedDate, note, fileUrl, fileName);

        showSaving(true);
        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_MEDICAL_RECORDS)
                .add(record)
                .addOnSuccessListener(documentReference -> {
                    AppUtils.showToast(requireContext(), R.string.record_saved);
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .addOnFailureListener(e -> {
                    showSaving(false);
                    AppUtils.showToast(requireContext(), R.string.save_error);
                });
    }

    private void showUploading(boolean uploading) {
        progressBar.setVisibility(uploading ? View.VISIBLE : View.GONE);
        btnPickFile.setEnabled(!uploading);
    }

    private void showSaving(boolean saving) {
        btnSave.setEnabled(!saving);
        progressBar.setVisibility(saving ? View.VISIBLE : View.GONE);
        btnPickFile.setEnabled(!saving);
    }
}
