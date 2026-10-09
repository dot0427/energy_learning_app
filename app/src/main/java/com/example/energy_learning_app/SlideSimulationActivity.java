package com.example.energy_learning_app;

import android.animation.ValueAnimator;
import android.app.AlertDialog; // 新增：用於顯示 AI 視窗
import android.content.DialogInterface; // 新增：用於按鈕點擊監聽
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.energy_learning_app.data.*;
import com.example.energy_learning_app.model.*;
import com.example.energy_learning_app.ui.*;
import com.example.energy_learning_app.ui.util.ToastUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SlideSimulationActivity extends AppCompatActivity {

    private SlideSimView slideSimView;
    private TextView tvFormula;

    private ImageButton btnReplay;
    private Button btnPractice, btnExplain, btnReset;

    private ValueAnimator animator;

    private LineChart keChart;
    private LineDataSet keDataSet;
    private LineData keLineData;

    // 讓 x 軸是固定時間 0~totalTimeSec（不移動、不滑）
    private float totalTimeSec;
    private int sampleIndex = 0;
    private float dtSec;

    // 參數（從設定頁帶來）
    private float heightM, angleDeg, massKg, mu;

    private AiTeacher aiTeacher;
    private AiTutorFlow tutorFlow;

    private static final float g = 9.8f;
    private static final long DURATION_MS = 2500; // 2.5 秒
    private static final int MAX_SAMPLES = 50;     // 固定顯示完整（0~2.5秒）用 50 個點很夠看

    // 統一色
    private static final int COLOR_BROWN = 0xFF5A3600;

    // 字體
    private Typeface tfRegular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_simulation);

        tfRegular = ResourcesCompat.getFont(this, R.font.gensenrounded_regular);

        bindViews();
        readParams();
        setupUI();
        setupButtons();
        aiTeacher = new AiTeacher(BuildConfig.OPENAI_API_KEY);
        tutorFlow = new AiTutorFlow(this, aiTeacher);
        tutorFlow.setDisableWhileLoadingViews(btnExplain, btnPractice);

        totalTimeSec = DURATION_MS / 1000f;
        dtSec = totalTimeSec / (float) MAX_SAMPLES;

        setupChart();  // 讀完 totalTimeSec、dtSec 後再 setup
        startSim();
    }

    private void bindViews() {
        slideSimView = findViewById(R.id.slideSimView);
        tvFormula = findViewById(R.id.textViewSlideFormula);

        keChart = findViewById(R.id.SlideChart);

        btnReplay = findViewById(R.id.buttonSlideReplay);
        btnPractice = findViewById(R.id.buttonSlidePrac);
        btnExplain = findViewById(R.id.buttonSlideExplain);
        btnReset = findViewById(R.id.buttonSlideReset);

        // ✅ 公式字體一致
        if (tfRegular != null) tvFormula.setTypeface(tfRegular);
        tvFormula.setTextColor(COLOR_BROWN);
    }

    private void readParams() {
        heightM = getIntent().getFloatExtra("height_m", 5f);
        angleDeg = getIntent().getFloatExtra("angle_deg", 30f);
        massKg = getIntent().getFloatExtra("mass_kg", 2f);
        mu = getIntent().getFloatExtra("friction_mu", 0.2f);
    }

    private void setupUI() {
        slideSimView.setAngleDeg(angleDeg);
    }

    private void setupButtons() {
        // 重播按鈕
        btnReplay.setOnClickListener(v -> {
            List<Achievement> unlocked = AchievementManager.onEvent(
                    this,
                    AchievementEvent.REPLAY,
                    0,
                    false,
                    false,
                    0
            );
            AchievementNotifier.notifyUnlocked(this, unlocked);

            restartSim();
        });

        // 重置/返回按鈕
        btnReset.setOnClickListener(v -> {
            finish(); // 結束目前頁面，返回上一頁（設定頁）
        });

        // --- AI 講解按鈕 ---
        btnExplain.setOnClickListener(v -> {
            pauseSim();
            String prompt = buildSlidePromptForAi();
            tutorFlow.showExplain(prompt);
        });

        // --- AI 練習按鈕 ---
        btnPractice.setOnClickListener(v -> {
            String prompt = buildSlidePromptForAi();
            tutorFlow.startQuiz(prompt);
        });
    }

    private String buildSlidePromptForAi() {
        float pe0 = massKg * g * heightM;

        float heatEnd = mu * 1f * pe0 * 0.4f;
        heatEnd = Math.max(0f, Math.min(heatEnd, pe0));

        float keEnd = Math.max(0f, pe0 - heatEnd);

        return PromptBuilder.buildSlideTutorPrompt(
                massKg, heightM, mu, angleDeg,
                pe0, keEnd, heatEnd
        );
    }

    private void startSim() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DURATION_MS);

        animator.addUpdateListener(a -> {
            float p = (float) a.getAnimatedValue(); // 0..1
            slideSimView.setProgress(p);

            updateFormula(p);

            float ke = computeKE(p);

            // ✅ 用真正時間（秒）當 X，保證在 0~totalTimeSec
            float tSec = a.getCurrentPlayTime() / 1000f;

            // 以防最後一幀略超過
            if (tSec > totalTimeSec) tSec = totalTimeSec;

            keDataSet.addEntry(new Entry(tSec, ke));

            keLineData.notifyDataChanged();
            keChart.notifyDataSetChanged();
            keChart.invalidate();
        });

        animator.start();
    }

    private void restartSim() {
        if (animator != null) animator.cancel();
        if (tutorFlow != null) tutorFlow.clearCache();

        slideSimView.setProgress(0f);

        sampleIndex = 0;
        keDataSet.clear();
        keLineData.notifyDataChanged();
        keChart.notifyDataSetChanged();
        keChart.invalidate();

        startSim();
    }

    private void pauseSim() {
        if (animator != null && animator.isRunning()) animator.pause();
    }

    private void updateFormula(float p) {
        float pe0 = massKg * g * heightM;

        float hNow = (1f - p) * heightM;
        float peNow = massKg * g * hNow;

        float heat = mu * p * pe0 * 0.4f;
        heat = Math.max(0f, Math.min(heat, pe0));

        float ke = Math.max(0f, pe0 - peNow - heat);

        tvFormula.setText(String.format(Locale.getDefault(),
                "參數：h=%.1fm, θ=%.0f°, m=%.1fkg, μ=%.2f\n\n" +
                        "PE0 = mgh = %.1f J\n" +
                        "目前高度 h(t)=%.2f m → PE(t)=%.1f J\n" +
                        "Heat(t) ≈ %.1f J（摩擦示意）\n" +
                        "KE(t) ≈ PE0 - PE(t) - Heat(t) = %.1f J",
                heightM, angleDeg, massKg, mu,
                pe0, hNow, peNow, heat, ke));
    }

    private float computeKE(float p) {
        float pe0 = massKg * g * heightM;
        float peNow = massKg * g * ((1f - p) * heightM);
        float heat = mu * p * pe0 * 0.4f;
        heat = Math.max(0f, Math.min(heat, pe0));
        return Math.max(0f, pe0 - peNow - heat);
    }

    private void setupChart() {
        keDataSet = new LineDataSet(new ArrayList<>(), "KE (J)");
        keDataSet.setColor(COLOR_BROWN);
        keDataSet.setLineWidth(2.5f);
        keDataSet.setDrawCircles(false);
        keDataSet.setDrawValues(false);

        keLineData = new LineData(keDataSet);
        keChart.setData(keLineData);

        keChart.getDescription().setEnabled(false);
        keChart.getLegend().setEnabled(false);

        keChart.setTouchEnabled(false);
        keChart.setDragEnabled(false);
        keChart.setScaleEnabled(false);
        keChart.setPinchZoom(false);
        keChart.setDoubleTapToZoomEnabled(false);

        XAxis xAxis = keChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        xAxis.setTextColor(COLOR_BROWN);
        if (tfRegular != null) xAxis.setTypeface(tfRegular);

        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(totalTimeSec);
        xAxis.setLabelCount(6, true); // 0,0.5,1.0,... 你可調
        xAxis.setGranularity(0.5f);

        keChart.getAxisRight().setEnabled(false);

        keChart.getAxisLeft().setDrawGridLines(true);
        keChart.getAxisLeft().setTextColor(COLOR_BROWN);
        if (tfRegular != null) keChart.getAxisLeft().setTypeface(tfRegular);

        float pe0 = massKg * g * heightM;
        keChart.getAxisLeft().setAxisMinimum(0f);
        keChart.getAxisLeft().setAxisMaximum(pe0 * 1.05f);

        keChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        if (animator != null) animator.cancel();
        super.onDestroy();
    }
}
