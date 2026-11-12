
package com.example.taskmanager;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    EditText taskInput;
    Spinner categorySpinner, prioritySpinner;
    LinearLayout taskListContainer;
    TextView emptyState, taskCountText;
    ProgressBar taskProgress;

    ArrayList<Task> tasks = new ArrayList<>();

    String[] categories = {"Study", "Personal", "Work", "Other"};
    String[] priorities = {"High", "Medium", "Low"};

    final int DARK = Color.rgb(75, 63, 88);
    final int LILAC = Color.rgb(229, 218, 247);
    final int SEA_GREEN = Color.rgb(218, 241, 231);
    final int YELLOW = Color.rgb(255, 246, 205);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskInput = findViewById(R.id.taskInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        taskListContainer = findViewById(R.id.taskListContainer);
        emptyState = findViewById(R.id.emptyState);
        taskCountText = findViewById(R.id.taskCountText);
        taskProgress = findViewById(R.id.taskProgress);

        Button addTaskButton = findViewById(R.id.addTaskButton);

        setupSpinner(categorySpinner, categories);
        setupSpinner(prioritySpinner, priorities);

        addTaskButton.setOnClickListener(v -> addTask());

        updateTaskDisplay();
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    private void addTask() {

        String name = taskInput.getText().toString().trim();

        if (name.isEmpty()) {
            taskInput.setError("Please enter a task");
            return;
        }

        String category = categorySpinner.getSelectedItem().toString();
        String priority = prioritySpinner.getSelectedItem().toString();

        tasks.add(new Task(name, category, priority));

        taskInput.setText("");

        sortTasks();
        updateTaskDisplay();

        Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
    }

    private int priorityValue(String priority) {
        switch (priority) {
            case "High": return 3;
            case "Medium": return 2;
            case "Low": return 1;
            default: return 0;
        }
    }

    private void sortTasks() {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task a, Task b) {

                int priorityDifference =
                        priorityValue(b.priority) - priorityValue(a.priority);

                if (priorityDifference != 0) {
                    return priorityDifference;
                }

                if (a.completed != b.completed) {
                    return a.completed ? 1 : -1;
                }

                return 0;
            }
        });
    }

    private void updateTaskDisplay() {

        taskListContainer.removeAllViews();

        int completed = 0;

        for (Task task : tasks) {
            if (task.completed) completed++;
            createTaskCard(task);
        }

        int total = tasks.size();

        taskCountText.setText(
                completed + " of " + total + " tasks completed"
        );

        taskProgress.setMax(Math.max(total, 1));
        taskProgress.setProgress(completed);

        emptyState.setVisibility(
                total == 0 ? android.view.View.VISIBLE
                        : android.view.View.GONE
        );
    }

    private void createTaskCard(Task task) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));

        GradientDrawable background = new GradientDrawable();
        background.setColor(categoryColor(task.category));
        background.setCornerRadius(dp(18));
        card.setBackground(background);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1, -2
                );

        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        // Task name and checkbox

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setButtonTintList(
                ColorStateList.valueOf(DARK)
        );
        checkBox.setChecked(task.completed);

        topRow.addView(checkBox);

        TextView taskName = new TextView(this);
        taskName.setText(task.name);
        taskName.setTextColor(DARK);
        taskName.setTextSize(16);
        taskName.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(0, -2, 1);

        topRow.addView(taskName, nameParams);

        // Bin icon

        TextView deleteButton = new TextView(this);
        deleteButton.setText("🗑️");
        deleteButton.setTextSize(21);
        deleteButton.setGravity(Gravity.CENTER);
        deleteButton.setPadding(dp(10), dp(5), dp(5), dp(5));

        topRow.addView(deleteButton);
        card.addView(topRow);

        // Category and priority labels

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        bottomRow.setPadding(dp(42), dp(5), 0, 0);

        TextView category = makeLabel(task.category, Color.WHITE);
        bottomRow.addView(category);

        TextView priority = makeLabel(
                task.priority + " Priority",
                priorityColor(task.priority)
        );

        LinearLayout.LayoutParams priorityParams =
                new LinearLayout.LayoutParams(-2, -2);

        priorityParams.setMargins(dp(8), 0, 0, 0);
        bottomRow.addView(priority, priorityParams);

        card.addView(bottomRow);

        // Completed appearance

        if (task.completed) {
            taskName.setPaintFlags(
                    taskName.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG
            );
            taskName.setTextColor(Color.GRAY);
        }

        // Checkbox action

        checkBox.setOnCheckedChangeListener((button, checked) -> {
            task.completed = checked;
            sortTasks();
            updateTaskDisplay();
        });

        // Delete action

        deleteButton.setOnClickListener(v -> {
            tasks.remove(task);
            updateTaskDisplay();
            Toast.makeText(this, "Task deleted!",
                    Toast.LENGTH_SHORT).show();
        });

        taskListContainer.addView(card);
    }

    private TextView makeLabel(String text, int color) {

        TextView label = new TextView(this);
        label.setText("  " + text + "  ");
        label.setTextColor(DARK);
        label.setTextSize(12);
        label.setPadding(dp(8), dp(5), dp(8), dp(5));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(20));

        label.setBackground(bg);

        return label;
    }

    private int categoryColor(String category) {
        switch (category) {
            case "Study": return LILAC;
            case "Personal": return SEA_GREEN;
            case "Work": return YELLOW;
            default: return Color.rgb(239, 230, 245);
        }
    }

    private int priorityColor(String priority) {
        switch (priority) {
            case "High":
                return Color.rgb(255, 207, 207);
            case "Medium":
                return YELLOW;
            case "Low":
                return SEA_GREEN;
            default:
                return Color.WHITE;
        }
    }

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density);
    }

    // Task class

    private static class Task {
        String name;
        String category;
        String priority;
        boolean completed;

        Task(String name, String category, String priority) {
            this.name = name;
            this.category = category;
            this.priority = priority;
            this.completed = false;
        }
    }
}
