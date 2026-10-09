package com.example.energy_learning_app.data;

import android.content.Context;
import android.content.SharedPreferences;

public class AchievementPrefs {
    public static final String PREF_NAME = "achievements_pref";

    public static final String KEY_TOTAL_ANSWERED = "stat_total_answered";     // 總答題數
    public static final String KEY_PERFECT_SESSION = "stat_perfect_session";  // 100%答對(一次提交算一次)
    public static final String KEY_ZERO_SESSION = "stat_zero_session";        // 0%答對(一次提交算一次)
    public static final String KEY_REPORT_COUNT = "stat_report_count";        // 回報題目錯誤次數
    public static final String KEY_REPLAY_COUNT = "stat_replay_count";        // 重播次數

    public static String keyUnlocked(String id) {
        return "ach_" + id + "_unlocked";
    }

    public static String keyUnlockedAt(String id) {
        return "ach_" + id + "_unlocked_at";
    }

    public static SharedPreferences sp(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int getInt(Context context, String key, int def) {
        return sp(context).getInt(key, def);
    }

    public static void putInt(Context context, String key, int value) {
        sp(context).edit().putInt(key, value).apply();
    }

    public static void inc(Context context, String key, int delta) {
        SharedPreferences pref = sp(context);
        int v = pref.getInt(key, 0);
        pref.edit().putInt(key, v + delta).apply();
    }

    public static boolean isUnlocked(Context context, String id) {
        return sp(context).getBoolean(keyUnlocked(id), false);
    }

    public static long getUnlockedAt(Context context, String id) {
        return sp(context).getLong(keyUnlockedAt(id), 0L);
    }

    public static void setUnlocked(Context context, String id, long timeMillis) {
        sp(context).edit()
                .putBoolean(keyUnlocked(id), true)
                .putLong(keyUnlockedAt(id), timeMillis)
                .apply();
    }
}
