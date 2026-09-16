package com.example.studytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studytrack.R;
import com.example.studytrack.models.Assignment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AssignmentDetailsActivity extends AppCompatActivity {

    private TextView txtDetailSubject;
    private TextView txtDetailTitle;
    private TextView txtDetailDueDate;
    private TextView txtDetailDescription;

    private Button btnComplete;
    private Button btnEdit;
    private Button btnDelete;

    private DatabaseReference assignmentRef;

    private String assignmentId;

    private Assignment currentAssignment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_assignment_details);

        // Get assignment ID from Intent
        assignmentId = getIntent().getStringExtra("assignmentId");

        if (assignmentId == null) {
            Toast.makeText(
                    this,
                    "Assignment not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Views
        txtDetailSubject =
                findViewById(R.id.txtDetailSubject);

        txtDetailTitle =
                findViewById(R.id.txtDetailTitle);

        txtDetailDueDate =
                findViewById(R.id.txtDetailDueDate);

        txtDetailDescription =
                findViewById(R.id.txtDetailDescription);

        btnComplete =
                findViewById(R.id.btnComplete);

        btnEdit =
                findViewById(R.id.btnEdit);

        btnDelete =
                findViewById(R.id.btnDelete);

        // Firebase reference to this assignment
        assignmentRef =
                FirebaseDatabase.getInstance()
                        .getReference("assignments")
                        .child(assignmentId);

        loadAssignment();

        btnComplete.setOnClickListener(v -> markAsCompleted());

        btnDelete.setOnClickListener(v -> showDeleteDialog());

        // Edit will be added next
        btnEdit.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AssignmentDetailsActivity.this,
                    AddAssignmentActivity.class
            );

            intent.putExtra(
                    "assignmentId",
                    assignmentId
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (assignmentRef != null) {
            loadAssignment();
        }
    }

    private void loadAssignment() {

        assignmentRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        currentAssignment =
                                snapshot.getValue(Assignment.class);

                        if (currentAssignment == null) {

                            Toast.makeText(
                                    AssignmentDetailsActivity.this,
                                    "Assignment not found",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                            return;
                        }

                        displayAssignment();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                AssignmentDetailsActivity.this,
                                "Failed to load assignment",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void displayAssignment() {

        txtDetailSubject.setText(
                currentAssignment.getSubject()
        );

        txtDetailTitle.setText(
                currentAssignment.getTitle()
        );

        txtDetailDueDate.setText(
                "Due: " + currentAssignment.getDueDate()
        );

        txtDetailDescription.setText(
                currentAssignment.getDescription()
        );

        if (currentAssignment.isCompleted()) {

            btnComplete.setText("Completed ✓");
            btnComplete.setEnabled(false);

        } else {

            btnComplete.setText("Mark as Completed");
            btnComplete.setEnabled(true);
        }
    }

    private void markAsCompleted() {

        assignmentRef.child("completed")
                .setValue(true)
                .addOnSuccessListener(unused -> {

                    currentAssignment.setCompleted(true);

                    btnComplete.setText("Completed ✓");
                    btnComplete.setEnabled(false);

                    Toast.makeText(
                            this,
                            "Assignment completed",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to update assignment",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void showDeleteDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Assignment")
                .setMessage(
                        "Are you sure you want to delete this assignment?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteAssignment()
                )
                .show();
    }

    private void deleteAssignment() {

        assignmentRef.removeValue()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Assignment deleted",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to delete assignment",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}