package com.example.dailywater.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class WaterPreferences {

    private static final String PREF_NAME = "WaterPrefs";
    private final SharedPreferences prefs;

    public WaterPreferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        checkAndResetDailyData();
    }

    private void checkAndResetDailyData() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = prefs.getString("last_date", "");

        if (!today.equals(lastDate)) {
            prefs.edit()
                    .putString("last_date", today)
                    .putInt("accumulated", 0)
                    .putStringSet("history", new HashSet<>())
                    .apply();
        }
    }

    // ATUALIZADO: Agora aceita o 'ml' e também a string formatada do registro ('recordEntry')
    public void addConsumption(int ml, String recordEntry) {
        checkAndResetDailyData();

        int currentAcc = getAccumulated();
        int newAcc = currentAcc + ml;

        Set<String> history = new HashSet<>(prefs.getStringSet("history", new HashSet<>()));
        history.add(recordEntry);

        prefs.edit()
                .putInt("accumulated", newAcc)
                .putStringSet("history", history)
                .apply();
    }

    public int getAccumulated() {
        return prefs.getInt("accumulated", 0);
    }

    public void setDailyGoal(int goalMl) {
        prefs.edit().putInt("daily_goal", goalMl).apply();
    }

    public int getDailyGoal() {
        return prefs.getInt("daily_goal", 2000);
    }

    public Set<String> getHistory() {
        return prefs.getStringSet("history", new HashSet<>());
    }
}