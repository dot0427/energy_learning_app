package com.example.energy_learning_app.model;

import java.util.List;

public class AchievementSection {
    public final String id;       // e.g. "TOTAL", "PERFECT"
    public final String title;    // e.g. "答題總數"
    public final String subtitle; // e.g. "挑戰進度系統"
    public final List<Achievement> achievements;

    // UI 狀態（不一定要存 prefs，先用記憶體就好）
    public boolean collapsed = false;

    public AchievementSection(String id, String title, String subtitle, List<Achievement> achievements) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.achievements = achievements;
    }
}
