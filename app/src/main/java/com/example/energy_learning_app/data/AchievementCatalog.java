package com.example.energy_learning_app.data;

import com.example.energy_learning_app.model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AchievementCatalog {

    public static List<AchievementSection> allSections() {
        List<AchievementSection> sections = new ArrayList<>();

        // 🔵 答題總數
        sections.add(new AchievementSection(
                "TOTAL",
                "答題總數",
                "挑戰進度系統",
                Arrays.asList(
                        new Achievement("ANSWER_20",  "小試身手 · Energetic Rookie",      "剛暖身開始衝 💪", 20,  AchievementPrefs.KEY_TOTAL_ANSWERED),
                        new Achievement("ANSWER_50",  "永不言棄 · Momentum Starter",     "慣性越滾越大",   50,  AchievementPrefs.KEY_TOTAL_ANSWERED),
                        new Achievement("ANSWER_100", "能量學徒 · Energy Explorer",      "已經有料了 🔥",   100, AchievementPrefs.KEY_TOTAL_ANSWERED),
                        new Achievement("ANSWER_200", "能量博士 · Thermal Thinker",      "知識發光階段 ✨", 200, AchievementPrefs.KEY_TOTAL_ANSWERED),
                        new Achievement("ANSWER_500", "守恆王者 · Conservation Master",  "學科大魔王 👑",   500, AchievementPrefs.KEY_TOTAL_ANSWERED)
                )
        ));

        // 🟢 100% 答對率（以「一次提交=一次」為單位）
        sections.add(new AchievementSection(
                "PERFECT",
                "完美滿分",
                "答對率 100% ≥ N 次",
                Arrays.asList(
                        new Achievement("PERFECT_10", "全滿能 · Perfect Kinetic",        "連續累積 10 次滿分回合", 10, AchievementPrefs.KEY_PERFECT_SESSION),
                        new Achievement("PERFECT_20", "全對神角 · No Loss Mode",         "連續累積 20 次滿分回合", 20, AchievementPrefs.KEY_PERFECT_SESSION),
                        new Achievement("PERFECT_30", "零摩擦傳說 · Zero Friction Legend", "能量不流失就是你 🥇",    30, AchievementPrefs.KEY_PERFECT_SESSION)
                )
        ));

        // 🔴 0%（搞笑）
        sections.add(new AchievementSection(
                "ZERO",
                "全錯彩蛋",
                "答對率 0% ≥ N 次",
                Arrays.asList(
                        new Achievement("ZERO_10", "熵增之力 · Chaos Player",         "能量去了哪？不知道，爽就完事", 10, AchievementPrefs.KEY_ZERO_SESSION),
                        new Achievement("ZERO_20", "熱寂邁進 · Entropy Rising",       "你正在逼近宇宙的冷靜",          20, AchievementPrefs.KEY_ZERO_SESSION),
                        new Achievement("ZERO_30", "宇宙終態 · Big Freeze Survivor",  "梗很硬但超好笑 ❄️",              30, AchievementPrefs.KEY_ZERO_SESSION)
                )
        ));

        // 🟣 檢查王（回報）
        sections.add(new AchievementSection(
                "REPORT",
                "檢查王",
                "回報題目錯誤 ≥ N 次",
                Arrays.asList(
                        new Achievement("REPORT_10", "🕵 公式巡警 · Equation Inspector",      "守護公式正確性", 10, AchievementPrefs.KEY_REPORT_COUNT),
                        new Achievement("REPORT_20", "🧪 物理糾察隊 · Physics Debugger",      "抓 bug 抓到起飛", 20, AchievementPrefs.KEY_REPORT_COUNT),
                        new Achievement("REPORT_30", "🧠 真理執行者 · Law of Nature Auditor", "這稱號一看就 OP", 30, AchievementPrefs.KEY_REPORT_COUNT)
                )
        ));

        // 🟡 重播
        sections.add(new AchievementSection(
                "REPLAY",
                "重播成癮",
                "重播動畫 / 再看同圖 ≥ N 次",
                Arrays.asList(
                        new Achievement("REPLAY_10", "🔁 反覆驗證者 · Retry Thinker",      "科學精神代表",      10, AchievementPrefs.KEY_REPLAY_COUNT),
                        new Achievement("REPLAY_20", "🔁 模擬沉迷 · Simulation Addict",   "你真的很投入",      20, AchievementPrefs.KEY_REPLAY_COUNT),
                        new Achievement("REPLAY_50", "🔁 永動機候選人 · Perpetual Engine", "笑點最高 🤣",        50, AchievementPrefs.KEY_REPLAY_COUNT)
                )
        ));

        return sections;
    }
}
