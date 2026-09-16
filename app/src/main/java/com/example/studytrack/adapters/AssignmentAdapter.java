package com.example.studytrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studytrack.R;
import com.example.studytrack.models.Assignment;

import java.util.List;

public class AssignmentAdapter
        extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    private List<Assignment> assignmentList;
    private OnAssignmentClickListener listener;

    public interface OnAssignmentClickListener {
        void onAssignmentClick(Assignment assignment);
    }

    public AssignmentAdapter(
            List<Assignment> assignmentList,
            OnAssignmentClickListener listener) {

        this.assignmentList = assignmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.assignment_item, parent, false);

        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AssignmentViewHolder holder, int position) {

        Assignment assignment = assignmentList.get(position);

        holder.txtSubject.setText(assignment.getSubject());
        holder.txtTitle.setText(assignment.getTitle());
        holder.txtDueDate.setText("Due: " + assignment.getDueDate());

        holder.itemView.setOnClickListener(v -> {
            listener.onAssignmentClick(assignment);
        });
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    static class AssignmentViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtSubject;
        TextView txtTitle;
        TextView txtDueDate;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);

            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDueDate = itemView.findViewById(R.id.txtDueDate);
        }
    }
}
