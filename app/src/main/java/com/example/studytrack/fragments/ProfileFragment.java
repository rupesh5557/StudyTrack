package com.example.studytrack.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

    private EditText edtStudentName;
    private EditText edtStudentDepartment;
    private EditText edtStudentYear;
    private Button btnSaveProfile;

    private TextView txtProfileTotal;
    private TextView txtProfileCompleted;
    private TextView txtProfilePending;

    private DatabaseReference assignmentsRef;

    private SharedPreferences preferences;

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

        edtStudentName =
                view.findViewById(R.id.edtStudentName);

        edtStudentDepartment =
                view.findViewById(R.id.edtStudentDepartment);

        edtStudentYear =
                view.findViewById(R.id.edtStudentYear);

        btnSaveProfile =
                view.findViewById(R.id.btnSaveProfile);

        txtProfileTotal =
                view.findViewById(R.id.txtProfileTotal);

        txtProfileCompleted =
                view.findViewById(R.id.txtProfileCompleted);

        txtProfilePending =
                view.findViewById(R.id.txtProfilePending);


        // Local profile storage

        preferences =
                requireContext().getSharedPreferences(
                        "StudyTrackProfile",
                        Context.MODE_PRIVATE
                );

        loadProfile();


        // Save profile

        btnSaveProfile.setOnClickListener(v -> {

            String name =
                    edtStudentName.getText().toString().trim();

            String department =
                    edtStudentDepartment.getText().toString().trim();

            String year =
                    edtStudentYear.getText().toString().trim();


            if (name.isEmpty()) {
                edtStudentName.setError("Enter your name");
                edtStudentName.requestFocus();
                return;
            }

            if (department.isEmpty()) {
                edtStudentDepartment.setError("Enter your department");
                edtStudentDepartment.requestFocus();
                return;
            }

            if (year.isEmpty()) {
                edtStudentYear.setError("Enter your year");
                edtStudentYear.requestFocus();
                return;
            }


            preferences.edit()
                    .putString("name", name)
                    .putString("department", department)
                    .putString("year", year)
                    .apply();


            Toast.makeText(
                    requireContext(),
                    "Profile saved",
                    Toast.LENGTH_SHORT
            ).show();

        });


        // Firebase

        assignmentsRef =
                FirebaseDatabase.getInstance()
                        .getReference("assignments");

        loadStatistics();
    }


    private void loadProfile() {

        String name =
                preferences.getString("name", "");

        String department =
                preferences.getString("department", "");

        String year =
                preferences.getString("year", "");


        edtStudentName.setText(name);
        edtStudentDepartment.setText(department);
        edtStudentYear.setText(year);
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