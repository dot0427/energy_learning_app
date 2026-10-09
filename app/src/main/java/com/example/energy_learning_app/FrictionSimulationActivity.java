package com.example.energy_learning_app;

import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.energy_learning_app.data.*;
import com.example.energy_learning_app.model.*;
import com.example.energy_learning_app.ui.*;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FrictionSimulationActivity extends AppCompatActivity {

    private static final float g = 9.8f;
    private static final long DURATION_MS = 2500;
    private static final int COLOR_BROWN = 0xFF5A3600;

    private FrictionSimView frictionSimView;
    private LineChart chart;
    private TextView tvFormula;

    private ImageButton btnReplay;
    private Button btnPractice, btnExplain, btnReset;

    private ValueAnimator animator;

    private LineDataSet dsKE, dsHeat;
    private LineData lineData;

    private Typeface tfRegular;

    private AiTeacher aiTeacher;
    private AiTutorFlow tutorFlow;

    // params
    private float massKg, muS, muK, fMax;

    // sim state
    private float xNorm = 0f;
    private float v = 0f;           // m/s (示意)
    private float x = 0f;           // m (示意)
    private long lastPlayTimeMs = 0;

    // chart axis
    private float totalTimeSec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friction_simulation);

        tfRegular = ResourcesCompat.getFont(this, R.font.gensenrounded_regular);

        bindViews();
        readParams();

        totalTimeSec = DURATION_MS / 1000f;

        setupChart();
        setupButtons();

        aiTeacher = new AiTeacher(BuildConfig.OPENAI_API_KEY);
        tutorFlow = new AiTutorFlow(this, aiTeacher);
        tutorFlow.setDisableWhileLoadingViews(btnExplain, btnPractice);

        startSim();
    }

    private void bindViews() {
        frictionSimView = findViewById(R.id.frictionSimView);
        chart = findViewById(R.id.Fricchart);
        tvFormula = findViewById(R.id.textViewFricFormula);

        btnReplay = findViewById(R.id.buttonFricReplay);
        btnPractice = findViewById(R.id.buttonFricPrac);
        btnExplain = findViewById(R.id.buttonFricExplain);
        btnReset = findViewById(R.id.buttonFricReset);

        tvFormula.setTextColor(COLOR_BROWN);
        if (tfRegular != null) tvFormula.setTypeface(tfRegular);
    }

    private void readParams() {
        massKg = getIntent().getFloatExtra("mass_kg", 2f);
        muS   = getIntent().getFloatExtra("mu_s", 0.40f);
        muK   = getIntent().getFloatExtra("mu_k", 0.30f);
        fMax  = getIntent().getFloatExtra("f_max", 25f);
    }

    private void setupButtons() {
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

        btnReset.setOnClickListener(v -> finish()); // 回上一頁（你的流程跟滑梯一致）

        // --- AI 講解按鈕 ---
        btnExplain.setOnClickListener(v -> {
            String prompt = buildFrictionTutorPrompt();
            tutorFlow.showExplain(prompt);
        });

        // --- AI 練習按鈕 ---
        btnPractice.setOnClickListener(v -> {
            String prompt = buildFrictionTutorPrompt();
            tutorFlow.startQuiz(prompt);
        });
    }

    private void startSim() {
        resetState();

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DURATION_MS);

        animator.addUpdateListener(a -> {
            long tMs = a.getCurrentPlayTime();
            float tSec = tMs / 1000f;
            if (tSec > totalTimeSec) tSec = totalTimeSec;

            float dt = (tMs - lastPlayTimeMs) / 1000f;
            if (dt < 0) dt = 0;
            lastPlayTimeMs = tMs;

            // 推力：線性從 0 增加到 Fmax
            float fApplied = (tSec / totalTimeSec) * fMax;

            float N = massKg * g;
            float fStaticMax = muS * N;
            float fKinetic = muK * N;

            boolean moving;
            float fFriction;
            float aNet;

            if (v == 0f && x == 0f && fApplied < fStaticMax) {
                // 還沒動：靜摩擦「剛好抵消推力」
                moving = false;
                fFriction = fApplied;
                aNet = 0f;
                v = 0f;
            } else if (fApplied <= fStaticMax && v == 0f) {
                // 臨界前（仍不動）
                moving = false;
                fFriction = fApplied;
                aNet = 0f;
            } else {
                // 開始滑動：動摩擦固定 μkN，淨力 = F - fk
                moving = true;
                fFriction = fKinetic;
                aNet = (fApplied - fKinetic) / massKg;

                // 簡單積分
                v = Math.max(0f, v + aNet * dt);
                x = Math.max(0f, x + v * dt);
            }

            // 視覺位置（把 x 轉成 0..1）
            xNorm = Math.min(1f, x / 6f); // 6m 當作滿版距離（示意）
            frictionSimView.setState(xNorm, fApplied, fFriction, moving);

            // 能量（示意）
            float ke = 0.5f * massKg * v * v;
            // 熱：用摩擦做功近似（f * x）
            float heat = fFriction * x;

            updateFormula(tSec, fApplied, fFriction, fStaticMax, ke, heat, moving);

            // chart：固定時間軸 0~totalTimeSec
            dsKE.addEntry(new Entry(tSec, ke));
            dsHeat.addEntry(new Entry(tSec, heat));

            lineData.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        });

        animator.start();
    }

    private void restartSim() {
        if (animator != null) animator.cancel();
        if (tutorFlow != null) tutorFlow.clearCache();
        startSim();
    }

    private void resetState() {
        xNorm = 0f;
        v = 0f;
        x = 0f;
        lastPlayTimeMs = 0;

        if (dsKE != null) dsKE.clear();
        if (dsHeat != null) dsHeat.clear();
        if (lineData != null) lineData.notifyDataChanged();
        if (chart != null) {
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }

    private void pauseSim() {
        if (animator != null && animator.isRunning()) animator.pause();
    }

    private void resumeSim() {
        if (animator != null) animator.resume();
    }

    private void updateFormula(
            float tSec,
            float fApplied,
            float fFriction,
            float fStaticMax,
            float ke,
            float heat,
            boolean moving
    ) {
        tvFormula.setText(String.format(Locale.getDefault(),
                "參數：m=%.1fkg, μs=%.2f, μk=%.2f, Fmax=%.0fN\n\n" +
                        "N = mg = %.1f N\n" +
                        "靜摩擦上限 f_s,max = μs·N = %.1f N\n" +
                        "目前推力 F(t) = %.1f N\n" +
                        "目前摩擦 f(t) = %.1f N\n" +
                        "狀態：%s\n\n" +
                        "KE(t)=%.2f J\n" +
                        "Heat(t)≈ f·x = %.2f J",
                massKg, muS, muK, fMax,
                massKg * g,
                fStaticMax,
                fApplied,
                fFriction,
                moving ? "動摩擦（已滑動）" : "靜摩擦（未滑動）",
                ke, heat));
    }

    private String buildFrictionTutorPrompt() {
        float N = massKg * g;
        float fStaticMax = muS * N;
        float fKinetic = muK * N;

        // 用「目前狀態」做 prompt（你按講解時，會抓到當下的 v/x）
        // 若你想固定用「跑完的結果」，我下面也教你怎麼做
        float keNow = 0.5f * massKg * v * v;

        // 目前摩擦力（依你 sim 判斷邏輯）
        float fFrictionNow;
        boolean movingNow;
        // 用你同一套判斷：只用當下推力近似（用最後一幀時間推力）
        float tSec = Math.min(totalTimeSec, lastPlayTimeMs / 1000f);
        float fAppliedNow = (tSec / totalTimeSec) * fMax;

        if ((v == 0f && x == 0f && fAppliedNow < fStaticMax) || (fAppliedNow <= fStaticMax && v == 0f)) {
            movingNow = false;
            fFrictionNow = fAppliedNow; // 靜摩擦抵消推力
        } else {
            movingNow = true;
            fFrictionNow = fKinetic;
        }

        float heatNow = fFrictionNow * x; // 你原本就是用 f*x 當 heat

        // 這裡用 PromptBuilder 的 friction prompt
        return PromptBuilder.buildFrictionTutorPrompt(
                massKg,
                muK,              // 出題以動摩擦為主（比較常用）
                N,
                x,
                -heatNow,         // 摩擦做功常用負值：W_f ≈ -Heat（你也可以直接填 -f*x）
                heatNow
        );
    }

    private void setupChart() {
        // KE
        dsKE = new LineDataSet(new ArrayList<>(), "KE (J)");
        dsKE.setColor(COLOR_BROWN);
        dsKE.setLineWidth(2.5f);
        dsKE.setDrawCircles(false);
        dsKE.setDrawValues(false);

        // Heat（用較淺的咖啡色）
        dsHeat = new LineDataSet(new ArrayList<>(), "Heat (J)");
        dsHeat.setColor(0xFF8D6E63);
        dsHeat.setLineWidth(2.5f);
        dsHeat.setDrawCircles(false);
        dsHeat.setDrawValues(false);

        lineData = new LineData(dsKE, dsHeat);
        chart.setData(lineData);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        // 不可互動、不滑動、不縮放
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(COLOR_BROWN);
        if (tfRegular != null) xAxis.setTypeface(tfRegular);

        // 固定時間軸 0~totalTimeSec
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(totalTimeSec);
        xAxis.setLabelCount(6, true);
        xAxis.setGranularity(0.5f);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setTextColor(COLOR_BROWN);
        if (tfRegular != null) chart.getAxisLeft().setTypeface(tfRegular);

        chart.getAxisLeft().setAxisMinimum(0f);

        chart.invalidate();
    }

    @Override
    protected void onDestroy() {
        if (animator != null) animator.cancel();
        super.onDestroy();
    }
}
