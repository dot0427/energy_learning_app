package com.example.energy_learning_app.model;

public class Achievement {

    public final String id;          // 唯一ID，例如 "ANSWER_20"
    public final String title;       // 成就名稱
    public final String description; // 成就敘述（點進去看）
    public final int target;         // 目標值（例如 20）
    public final String progressKey; // 對應 SharedPreferences 的計數 key

    // 動態資料（從 SharedPreferences 讀出來）
    public int current;              // 目前進度
    public boolean unlocked;         // 是否已解鎖
    public long unlockedAt;          // 解鎖時間 (epoch millis)，未解鎖=0

    public Achievement(String id, String title, String description,
                       int target, String progressKey) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.target = target;
        this.progressKey = progressKey;

        // 預設值
        this.current = 0;
        this.unlocked = false;
        this.unlockedAt = 0L;
    }

    public int progressPercent() {
        if (target <= 0) return 0;
        int p = (int) Math.floor((current * 100.0) / target);
        return Math.max(0, Math.min(100, p));
    }

    public boolean isCompleted() {
        return current >= target;
    }
}
