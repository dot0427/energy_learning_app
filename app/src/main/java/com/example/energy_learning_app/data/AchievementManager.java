package com.example.energy_learning_app.data;


import android.content.Context;

import com.example.energy_learning_app.model.*;

import java.util.ArrayList;
import java.util.List;

public class AchievementManager {

    /**
     * 事件觸發入口：更新統計數據，檢查是否有新解鎖
     * @return 本次「新解鎖」的成就列表（可能為空）
     */
    public static List<Achievement> onEvent(
            Context context,
            String event,
            int answeredDelta,      // 例如一次提交4題就傳4
            boolean isPerfect,      // 本次提交是否100%
            boolean isZero,         // 本次提交是否0%
            int extraDelta
    ) {
        // 1) 更新計數
        if (AchievementEvent.ANSWER_SUBMIT.equals(event)) {
            if (answeredDelta > 0) {
                int delta = Math.max(1, extraDelta);
                AchievementPrefs.inc(context, AchievementPrefs.KEY_TOTAL_ANSWERED, delta);
            }
            if (isPerfect) {
                AchievementPrefs.inc(context, AchievementPrefs.KEY_PERFECT_SESSION, 1);
            }
            if (isZero) {
                AchievementPrefs.inc(context, AchievementPrefs.KEY_ZERO_SESSION, 1);
            }
        } else if (AchievementEvent.REPORT_QUESTION.equals(event)) {
            AchievementPrefs.inc(context, AchievementPrefs.KEY_REPORT_COUNT, 1);
        } else if (AchievementEvent.REPLAY.equals(event)) {
            AchievementPrefs.inc(context, AchievementPrefs.KEY_REPLAY_COUNT, 1);
        }

        // 2) 檢查解鎖（只回傳「這次新解鎖」）
        return checkAndUnlock(context);
    }

    private static List<Achievement> checkAndUnlock(Context context) {
        List<AchievementSection> sections = AchievementRepository.loadAllSections(context);
        List<Achievement> newlyUnlocked = new java.util.ArrayList<>();
        long now = System.currentTimeMillis();

        for (AchievementSection sec : sections) {
            for (Achievement a : sec.achievements) {
                if (!a.unlocked && a.current >= a.target) {
                    AchievementPrefs.setUnlocked(context, a.id, now);
                    a.unlocked = true;
                    a.unlockedAt = now;
                    newlyUnlocked.add(a);
                }
            }
        }
        return newlyUnlocked;
    }
}
