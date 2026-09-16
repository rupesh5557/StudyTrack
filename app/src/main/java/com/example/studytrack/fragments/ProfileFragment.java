package com.example.studytrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studytrack.R;
import com.example.studytrack.models.Assignment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView txtStudentName;
    private TextView txtStudentDepartment;
    private TextView txtStudentYear;

    private TextView txtProfileTotal;
    private TextView txtProfileCompleted;
    private TextView txtProfilePending;

    private DatabaseReference assignmentsRef;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_profile,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        txtStudentName =
                view.findViewById(R.id.txtStudentName);

        txtStudentDepartment =
                view.findViewById(R.id.txtStudentDepartment);

        txtStudentYear =
                view.findViewById(R.id.txtStudentYear);

        txtProfileTotal =
                view.findViewById(R.id.txtProfileTotal);

        txtProfileCompleted =
                view.findViewById(R.id.txtProfileCompleted);

        txtProfilePending =
                view.findViewById(R.id.txtProfilePending);

        // Temporary student information
        txtStudentName.setText("Student Name");
        txtStudentDepartment.setText("Department: Computer Engineering");
        txtStudentYear.setText("Year: Third Year");

        assignmentsRef =
                FirebaseDatabase.getInstance()
                        .getReference("assignments");

        loadStatistics();
    }

    private void loadStatistics() {

        assignmentsRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        int total = 0;
                        int completed = 0;

                        for (DataSnapshot dataSnapshot :
                                snapshot.getChildren()) {

                            Assignment assignment =
                                    dataSnapshot.getValue(
                                            Assignment.class
                                    );

                            if (assignment != null) {

                                total++;

                                if (assignment.isCompleted()) {
                                    completed++;
                                }
                            }
                        }

                        int pending = total - completed;

                        txtProfileTotal.setText(
                                "Total Assignments: " + total
                        );

                        txtProfileCompleted.setText(
                                "Completed: " + completed
                        );

                        txtProfilePending.setText(
                                "Pending: " + pending
                        );
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        // Nothing to show if statistics fail
                    }
                }
        );
    }
}