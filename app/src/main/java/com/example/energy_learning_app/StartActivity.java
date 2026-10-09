package com.example.energy_learning_app;

import android.content.Intent;
import android.content.SharedPreferences; // 用於儲存資料
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.energy_learning_app.ui.util.ToastUtil; // 用於客製Toast

public class StartActivity extends AppCompatActivity {

    Button mButtonSlide, mButtonFric, mButtonHeat, mButtonAch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 修正：必須在這裡「呼叫」方法，程式才會執行初始化
        initUserData();

        mButtonSlide = findViewById(R.id.buttonSlide);
        mButtonFric = findViewById(R.id.buttonFriction);
        mButtonHeat = findViewById(R.id.buttonHeat);
        mButtonAch = findViewById(R.id.buttonAch);
        
        // --- 滑梯 ---
        mButtonSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show(StartActivity.this, "進入滑梯實驗設定");
                Intent intentSlide = new Intent(StartActivity.this, SlideSetupActivity.class);
                startActivity(intentSlide);
            }
        });
        // --- 摩擦 ---
        mButtonFric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show(StartActivity.this, "進入摩擦實驗設定");
                Intent intentFric = new Intent(StartActivity.this, FrictionSetupActivity.class);
                startActivity(intentFric);
            }
        });
        // --- 加熱 ---
        mButtonHeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show(StartActivity.this, "進入加熱實驗設定");
                Intent intentHeat = new Intent(StartActivity.this, HeatSetupActivity.class);
                startActivity(intentHeat);
            }
        });
        // --- 4. 成就按鈕 ---
        mButtonAch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAch = new Intent(StartActivity.this, AchievementActivity.class);
                startActivity(intentAch);
            }
        });
    }

        private void initUserData() {
            SharedPreferences sp = getSharedPreferences("EnergyAppParams", MODE_PRIVATE);
            boolean isFirstRun = sp.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isFirstRun", false);
            editor.putInt("totalExperiments", 0); // 總實驗次數歸零
            editor.apply();
        }
    }
}
