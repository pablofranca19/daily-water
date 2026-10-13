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
import com.example.dailywater.preferences.WaterPreferences;
import com.example.dailywater.views.WaterProgressView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_SINGLE_AMOUNT = 5000;

    private WaterProgressView waterProgress;
    private EditText consumptionInput;
    private TextView totalText;
    private TextView alertText;
    private ListView recordsList;
    private ArrayAdapter<String> recordsAdapter;

    // Instância da sua classe de persistência (WaterPreferences)
    private WaterPreferences waterPreferences;

    private final ActivityResultLauncher<Intent> goalLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int newGoal = result.getData().getIntExtra(GoalActivity.EXTRA_GOAL, waterPreferences.getDailyGoal());

                    // Salva a nova meta permanentemente usando a sua classe
                    waterPreferences.setDailyGoal(newGoal);

                    waterProgress.setMaxValue(newGoal);
                    refresh();
                }
            });

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa o seu gerenciador de dados persistentes
        waterPreferences = new WaterPreferences(this);

        waterProgress = findViewById(R.id.waterProgress);
        consumptionInput = findViewById(R.id.consumptionInput);
        totalText = findViewById(R.id.totalText);
        alertText = findViewById(R.id.alertText);
        recordsList = findViewById(R.id.recordsList);

        recordsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        recordsList.setAdapter(recordsAdapter);

        // Carrega a meta salva anteriormente na inicialização
        int savedGoal = waterPreferences.getDailyGoal();
        waterProgress.setMaxValue(savedGoal);

        // Carrega o histórico salvo no banco de preferências para preencher a lista
        loadSavedHistory();

        refresh();

        findViewById(R.id.addConsumptionButton).setOnClickListener(v -> addConsumption());

        findViewById(R.id.changeGoalButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, GoalActivity.class);
            intent.putExtra(GoalActivity.EXTRA_GOAL, waterPreferences.getDailyGoal());
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

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        int currentTotal = waterPreferences.getAccumulated() + amount;

        // Formata a string do registro igual o seu amigo fez
        String recordEntry = getString(R.string.record_format, time, amount, currentTotal);

        // Salva permanentemente utilizando a sua WaterPreferences
        waterPreferences.addConsumption(amount, recordEntry);

        recordsAdapter.add(recordEntry);
        recordsList.setSelection(recordsAdapter.getCount() - 1);

        consumptionInput.setText("");
        waterProgress.setConsumed(waterPreferences.getAccumulated());
        refresh();
    }

    private void loadSavedHistory() {
        recordsAdapter.clear();
        Set<String> historySet = waterPreferences.getHistory();
        if (historySet != null) {
            for (String record : historySet) {
                recordsAdapter.add(record);
            }
        }
    }

    private void refresh() {
        int total = waterPreferences.getAccumulated();
        int goal = waterPreferences.getDailyGoal();

        totalText.setText(getString(R.string.total_format, total, goal));
        waterProgress.setConsumed(total);

        boolean exceeded = total > goal;
        alertText.setVisibility(exceeded ? View.VISIBLE : View.GONE);
        if (exceeded) {
            alertText.setText(getString(R.string.goal_exceeded_message, total - goal));
        }
    }
}