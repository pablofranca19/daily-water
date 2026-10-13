package com.example.dailywater.activities;


import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dailywater.R;

public class WelcomeActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> goalLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int goal = result.getData().getIntExtra(GoalActivity.EXTRA_GOAL, 0);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(GoalActivity.EXTRA_GOAL, goal);
                    startActivity(intent);
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.setGoalButton).setOnClickListener(v ->
                goalLauncher.launch(new Intent(this, GoalActivity.class)));
    }
}
