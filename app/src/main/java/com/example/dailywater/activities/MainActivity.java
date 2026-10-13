package com.example.dailywater.activities;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dailywater.R;
import com.example.dailywater.views.WaterProgressView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class MainActivity extends AppCompatActivity {

    private static final int DEFAULT_GOAL = 2000;
    private static final int MAX_SINGLE_AMOUNT = 5000;

    private WaterProgressView waterProgress;
    private EditText consumptionInput;
    private TextView totalText;
    private TextView alertText;
    private ListView recordsList;
    private ArrayAdapter<String> recordsAdapter;

    private int goal = DEFAULT_GOAL;
    private int total = 0;

    private final ActivityResultLauncher<Intent> goalLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    goal = result.getData().getIntExtra(GoalActivity.EXTRA_GOAL, goal);
                    waterProgress.setMaxValue(goal);
                    refresh();
                }
            });

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waterProgress = findViewById(R.id.waterProgress);
        consumptionInput = findViewById(R.id.consumptionInput);
        totalText = findViewById(R.id.totalText);
        alertText = findViewById(R.id.alertText);
        recordsList = findViewById(R.id.recordsList);

        recordsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        recordsList.setAdapter(recordsAdapter);

        goal = getIntent().getIntExtra(GoalActivity.EXTRA_GOAL, DEFAULT_GOAL);
        waterProgress.setMaxValue(goal);
        refresh();

        findViewById(R.id.addConsumptionButton).setOnClickListener(v -> addConsumption());

        findViewById(R.id.changeGoalButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, GoalActivity.class);
            intent.putExtra(GoalActivity.EXTRA_GOAL, goal);
            goalLauncher.launch(intent);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addConsumption() {
        int amount;
        try {
            amount = Integer.parseInt(consumptionInput.getText().toString().trim());
        } catch (NumberFormatException e) {
            amount = 0;
        }

        if (amount <= 0 || amount > MAX_SINGLE_AMOUNT) {
            consumptionInput.setError(getString(R.string.invalid_amount));
            return;
        }

        total += amount;
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        recordsAdapter.add(getString(R.string.record_format, time, amount, total));
        recordsList.setSelection(recordsAdapter.getCount() - 1);

        consumptionInput.setText("");
        waterProgress.setConsumed(total);
        refresh();
    }

    private void refresh() {
        totalText.setText(getString(R.string.total_format, total, goal));

        boolean exceeded = total > goal;
        alertText.setVisibility(exceeded ? View.VISIBLE : View.GONE);
        if (exceeded) {
            alertText.setText(getString(R.string.goal_exceeded_message, total - goal));
        }
    }

}