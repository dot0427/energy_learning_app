package com.example.energy_learning_app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.energy_learning_app.R;
import com.example.energy_learning_app.data.*;
import com.example.energy_learning_app.model.*;
import com.example.energy_learning_app.ui.*;
import com.example.energy_learning_app.ui.util.ToastUtil;

import java.util.List;

public class AchievementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        RecyclerView rv = findViewById(R.id.rvAchievements);

        List<com.example.energy_learning_app.model.AchievementSection> sections =
                com.example.energy_learning_app.data.AchievementRepository.loadAllSections(this);

        for (AchievementSection s : sections) {
            s.collapsed = true; // 預設全部收合
        }

        AchievementSectionAdapter adapter =
                new AchievementSectionAdapter(sections, achievement -> {
                    AchievementDetailBottomSheet sheet =
                            AchievementDetailBottomSheet.newInstance(
                                    achievement.title,
                                    achievement.description,
                                    achievement.current,
                                    achievement.target,
                                    achievement.unlocked,
                                    achievement.unlockedAt
                            );
                    sheet.show(getSupportFragmentManager(), "ACH_DETAIL");
                });

        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 返回首頁
        findViewById(R.id.btnBackHome).setOnClickListener(v -> {
            finish();

        });

    }
}