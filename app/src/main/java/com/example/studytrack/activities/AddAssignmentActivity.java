package com.example.studytrack.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studytrack.R;
import com.example.studytrack.models.Assignment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddAssignmentActivity extends AppCompatActivity {

    private EditText edtSubject;
    private EditText edtTitle;
    private EditText edtDescription;
    private EditText edtDueDate;

    private Button btnSaveAssignment;

    private DatabaseReference assignmentsRef;

    // Used when editing
    private String assignmentId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_assignment);

        // Views
        edtSubject = findViewById(R.id.edtSubject);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtDueDate = findViewById(R.id.edtDueDate);
        btnSaveAssignment = findViewById(R.id.btnSaveAssignment);

        // Firebase
        assignmentsRef =
                FirebaseDatabase.getInstance()
                        .getReference("assignments");

        // Check whether this is Edit mode
        assignmentId =
                getIntent().getStringExtra("assignmentId");

        if (assignmentId != null) {
            isEditMode = true;

            loadAssignmentForEdit();

        } else {
            // Normal Add mode
            btnSaveAssignment.setText("Save Assignment");
        }

        // Date picker
        edtDueDate.setOnClickListener(v -> showDatePicker());

        // Save button
        btnSaveAssignment.setOnClickListener(v -> saveAssignment());
    }

    private void loadAssignmentForEdit() {

        assignmentsRef.child(assignmentId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    Assignment assignment =
                            snapshot.getValue(Assignment.class);

                    if (assignment == null) {

                        Toast.makeText(
                                this,
                                "Assignment not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                        return;
                    }

                    // Fill existing data
                    edtSubject.setText(
                            assignment.getSubject()
                    );

                    edtTitle.setText(
                            assignment.getTitle()
                    );

                    edtDescription.setText(
                            assignment.getDescription()
                    );

                    edtDueDate.setText(
                            assignment.getDueDate()
                    );

                    btnSaveAssignment.setText("Save Changes");
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to load assignment",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            String date =
                                    dayOfMonth + "/" +
                                            (month + 1) + "/" +
                                            year;

                            edtDueDate.setText(date);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }

    private void saveAssignment() {

        String subject =
                edtSubject.getText().toString().trim();

        String title =
                edtTitle.getText().toString().trim();

        String description =
                edtDescription.getText().toString().trim();

        String dueDate =
                edtDueDate.getText().toString().trim();

        // Validation
        if (subject.isEmpty()) {

            edtSubject.setError("Enter subject");
            edtSubject.requestFocus();
            return;
        }

        if (title.isEmpty()) {

            edtTitle.setError("Enter assignment title");
            edtTitle.requestFocus();
            return;
        }

        if (dueDate.isEmpty()) {

            edtDueDate.setError("Select due date");
            edtDueDate.requestFocus();
            return;
        }

        btnSaveAssignment.setEnabled(false);

        if (isEditMode) {

            // UPDATE existing assignment
            btnSaveAssignment.setText("Updating...");

            assignmentsRef.child(assignmentId)
                    .child("subject")
                    .setValue(subject)
                    .addOnSuccessListener(unused ->
                            updateTitle(
                                    title,
                                    description,
                                    dueDate
                            ))
                    .addOnFailureListener(e ->
                            saveFailed()
                    );

        } else {

            // CREATE new assignment
            btnSaveAssignment.setText("Saving...");

            String id =
                    assignmentsRef.push().getKey();

            if (id == null) {
                saveFailed();
                return;
            }

            Assignment assignment =
                    new Assignment(
                            id,
                            subject,
                            title,
                            description,
                            dueDate,
                            false
                    );

            assignmentsRef.child(id)
                    .setValue(assignment)
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                this,
                                "Assignment added",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    })
                    .addOnFailureListener(e ->
                            saveFailed()
                    );
        }
    }

    private void updateTitle(
            String title,
            String description,
            String dueDate) {

        assignmentsRef.child(assignmentId)
                .child("title")
                .setValue(title)
                .addOnSuccessListener(unused ->
                        updateDescription(
                                description,
                                dueDate
                        ))
                .addOnFailureListener(e ->
                        saveFailed()
                );
    }

    private void updateDescription(
            String description,
            String dueDate) {

        assignmentsRef.child(assignmentId)
                .child("description")
                .setValue(description)
                .addOnSuccessListener(unused ->
                        updateDueDate(dueDate))
                .addOnFailureListener(e ->
                        saveFailed()
                );
    }

    private void updateDueDate(String dueDate) {

        assignmentsRef.child(assignmentId)
                .child("dueDate")
                .setValue(dueDate)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Assignment updated",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e ->
                        saveFailed()
                );
    }

    private void saveFailed() {

        btnSaveAssignment.setEnabled(true);

        btnSaveAssignment.setText(
                isEditMode
                        ? "Save Changes"
                        : "Save Assignment"
        );

        Toast.makeText(
                this,
                "Failed to save assignment",
                Toast.LENGTH_SHORT
        ).show();
    }
}