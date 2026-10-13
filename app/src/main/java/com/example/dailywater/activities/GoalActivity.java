package com.example.dailywater.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dailywater.R;

public class GoalActivity extends AppCompatActivity{
    public static final String EXTRA_GOAL = "extra_goal";

    private static final int MAX_GOAL = 20000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        EditText goalInput = findViewById(R.id.goalInput);

        int currentGoal = getIntent().getIntExtra(EXTRA_GOAL, 0);
        if (currentGoal > 0) {
            goalInput.setText(String.valueOf(currentGoal));
            goalInput.setSelection(goalInput.getText().length());
        }

        findViewById(R.id.saveGoalButton).setOnClickListener(v -> {
            int goal;
            try {
                goal = Integer.parseInt(goalInput.getText().toString().trim());
            } catch (NumberFormatException e) {
                goal = 0;
            }

            if (goal <= 0 || goal > MAX_GOAL) {
                goalInput.setError(getString(R.string.invalid_goal));
                return;
            }

            setResult(RESULT_OK, new Intent().putExtra(EXTRA_GOAL, goal));
            finish();
        });
    }

}
