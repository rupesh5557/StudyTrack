package com.example.studytrack.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studytrack.R;
import com.example.studytrack.activities.AddAssignmentActivity;
import com.example.studytrack.activities.AssignmentDetailsActivity;
import com.example.studytrack.adapters.AssignmentAdapter;
import com.example.studytrack.models.Assignment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView assignmentRecyclerView;
    private AssignmentAdapter assignmentAdapter;

    private View loadingLayout, emptyLayout, errorLayout;

    private TextView txtTotalAssignments, txtCompletedAssignments, txtPendingAssignments;

    private Button btnAddAssignment, btnRetry;

    private List<Assignment> assignmentList;

    private DatabaseReference assignmentsRef;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable timeoutRunnable = () -> {
        showError();
    };

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Views
        assignmentRecyclerView =
                view.findViewById(R.id.assignmentRecyclerView);

        txtTotalAssignments =
                view.findViewById(R.id.txtTotalAssignments);

        txtCompletedAssignments =
                view.findViewById(R.id.txtCompletedAssignments);

        txtPendingAssignments =
                view.findViewById(R.id.txtPendingAssignments);

        btnAddAssignment =
                view.findViewById(R.id.btnAddAssignment);

        loadingLayout = view.findViewById(R.id.loadingLayout);
        emptyLayout = view.findViewById(R.id.emptyLayout);
        errorLayout = view.findViewById(R.id.errorLayout);

        btnRetry = view.findViewById(R.id.btnRetry);

        // List
        assignmentList = new ArrayList<>();

        // RecyclerView
        assignmentAdapter =
                new AssignmentAdapter(
                        assignmentList,
                        assignment -> {

                            Intent intent = new Intent(
                                    requireContext(),
                                    AssignmentDetailsActivity.class
                            );

                            intent.putExtra(
                                    "assignmentId",
                                    assignment.getId()
                            );

                            startActivity(intent);
                        }
                );

        assignmentRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        assignmentRecyclerView.setAdapter(assignmentAdapter);

        // Firebase
        assignmentsRef =
                FirebaseDatabase.getInstance()
                        .getReference("assignments");

        // Add Assignment button
        btnAddAssignment.setOnClickListener(v -> {

            Intent intent = new Intent(
                    requireContext(),
                    AddAssignmentActivity.class
            );

            startActivity(intent);
        });

        btnRetry.setOnClickListener(v -> loadAssignments());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (assignmentsRef != null) {
            loadAssignments();
        }
    }

    private void loadAssignments() {

        showLoading();

        // If Firebase doesn't respond within 8 seconds,
        // show the error screen.
        handler.postDelayed(timeoutRunnable, 8000);

        assignmentsRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        // Firebase responded, so cancel timeout
                        handler.removeCallbacks(timeoutRunnable);

                        assignmentList.clear();

                        for (DataSnapshot dataSnapshot :
                                snapshot.getChildren()) {

                            Assignment assignment =
                                    dataSnapshot.getValue(
                                            Assignment.class
                                    );

                            if (assignment != null) {
                                assignmentList.add(assignment);
                            }
                        }

                        assignmentAdapter.notifyDataSetChanged();

                        updateStatistics();

                        if (assignmentList.isEmpty()) {
                            showEmpty();
                        } else {
                            showContent();
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        // Cancel timeout because Firebase responded
                        handler.removeCallbacks(timeoutRunnable);

                        showError();
                    }
                }
        );
    }

    private void showLoading() {

        loadingLayout.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);

        assignmentRecyclerView.setVisibility(View.GONE);
    }

    private void showContent() {

        loadingLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);

        assignmentRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {

        loadingLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);

        assignmentRecyclerView.setVisibility(View.GONE);
    }

    private void showError() {

        loadingLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);

        assignmentRecyclerView.setVisibility(View.GONE);
    }

    private void updateStatistics() {

        int total = assignmentList.size();
        int completed = 0;

        for (Assignment assignment : assignmentList) {

            if (assignment.isCompleted()) {
                completed++;
            }
        }

        int pending = total - completed;

        txtTotalAssignments.setText(
                "Total\n" + total
        );

        txtCompletedAssignments.setText(
                "Completed\n" + completed
        );

        txtPendingAssignments.setText(
                "Pending\n" + pending
        );
    }
}