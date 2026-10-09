package com.example.energy_learning_app.data;

import android.content.Context;
import com.example.energy_learning_app.model.*;
import java.util.ArrayList;
import java.util.List;



public class AchievementRepository {

    public static List<AchievementSection> loadAllSections(Context context) {
        List<AchievementSection> sections = AchievementCatalog.allSections();

        for (AchievementSection sec : sections) {
            for (Achievement ach : sec.achievements) {
                ach.current = AchievementPrefs.getInt(context, ach.progressKey, 0);
                ach.unlocked = AchievementPrefs.isUnlocked(context, ach.id);
                ach.unlockedAt = AchievementPrefs.getUnlockedAt(context, ach.id);
            }
        }
        return sections;
    }
}
