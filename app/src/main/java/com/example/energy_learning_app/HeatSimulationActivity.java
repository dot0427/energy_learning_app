package com.example.energy_learning_app;

import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast; // 新增 Toast

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

public class HeatSimulationActivity extends AppCompatActivity {

    private static final long DURATION_MS = 2500;
    private static final int COLOR_BROWN = 0xFF5A3600;

    private HeatSimView heatSimView;
    private LineChart chart;
    private TextView tvFormula;

    private ImageButton btnReplay;
    private Button btnPractice, btnExplain, btnReset;

    private ValueAnimator animator;

    private LineDataSet dsQ, dsT;
    private LineData lineData;

    private Typeface tfRegular;

    private AiTeacher aiTeacher;
    private AiTutorFlow tutorFlow;

    // params
    private float massKg, c, powerW, timeS, t0C;

    private float totalTimeSec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_simulation);

        tfRegular = ResourcesCompat.getFont(this, R.font.gensenrounded_regular);

        bindViews();
        readParams();

        totalTimeSec = timeS; // 讓橫軸就是「加熱時間秒數」

        setupChart();
        setupButtons();

        aiTeacher = new AiTeacher(BuildConfig.OPENAI_API_KEY);
        tutorFlow = new AiTutorFlow(this, aiTeacher);
        tutorFlow.setDisableWhileLoadingViews(btnExplain, btnPractice);

        startSim();
    }

    private void bindViews() {
        heatSimView = findViewById(R.id.heatSimView);
        chart = findViewById(R.id.Heatchart);
        tvFormula = findViewById(R.id.textViewHeatFormula);

        btnReplay = findViewById(R.id.buttonHeatReplay);
        btnPractice = findViewById(R.id.buttonHeatPrac);
        btnExplain = findViewById(R.id.buttonHeatExplain);
        btnReset = findViewById(R.id.buttonHeatReset);

        tvFormula.setTextColor(COLOR_BROWN);
        if (tfRegular != null) tvFormula.setTypeface(tfRegular);
    }

    private void readParams() {
        massKg = getIntent().getFloatExtra("mass_kg", 2f);
        c      = getIntent().getFloatExtra("c_j_per_kgc", 900f);
        powerW = getIntent().getFloatExtra("power_w", 500f);
        timeS  = getIntent().getFloatExtra("time_s", 10f);
        t0C    = getIntent().getFloatExtra("t0_c", 25f);

        if (timeS <= 0) timeS = 10f;
        if (massKg <= 0) massKg = 2f;
        if (c <= 0) c = 900f;
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

        btnReset.setOnClickListener(v -> finish());

        // --- AI 講解按鈕 (單位換算版) ---
        btnExplain.setOnClickListener(v -> {
            String prompt = buildHeatTutorPrompt();
            tutorFlow.showExplain(prompt);
        });

        // --- AI 練習按鈕 ---
        btnPractice.setOnClickListener(v -> {
            String prompt = buildHeatTutorPrompt();
            tutorFlow.startQuiz(prompt);
        });
    }


    private void startSim() {
        resetChart();

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DURATION_MS);

        animator.addUpdateListener(a -> {
            float p = (float) a.getAnimatedValue();      // 0..1
            float t = p * timeS;                         // 0..timeS

            float Q = powerW * t;                        // J
            float dT = Q / (massKg * c);                 // °C
            float T = t0C + dT;                          // °C

            heatSimView.setProgress(p, T);
            updateFormula(t, Q, dT, T);

            dsQ.addEntry(new Entry(t, Q));
            dsT.addEntry(new Entry(t, T));

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

    private void pauseSim() {
        if (animator != null && animator.isRunning()) animator.pause();
    }

    private void resumeSim() {
        if (animator != null) animator.resume();
    }

    private void updateFormula(float t, float Q, float dT, float T) {
        tvFormula.setText(String.format(Locale.getDefault(),
                "參數：m=%.1fkg, c=%.0fJ/kg·°C, P=%.0fW, t_max=%.0fs, T0=%.0f°C\n\n" +
                        "Q(t)=P·t = %.1f J\n" +
                        "ΔT(t)=Q/(mc)= %.2f °C\n" +
                        "T(t)=T0+ΔT = %.2f °C",
                massKg, c, powerW, timeS, t0C,
                Q, dT, T));
    }

    private String buildHeatTutorPrompt() {
        // 你這頁的公式就是 Q = P*t, ΔT = Q/(mc), T = T0 + ΔT
        // AI 最好用「加熱到最後」的結果來講解
        float Qend = powerW * timeS;               // J
        float dTend = Qend / (massKg * c);         // °C
        float Tend = t0C + dTend;                  // °C

        return PromptBuilder.buildHeatTutorPrompt(
                "自訂材質",        // 如果你沒有材質選單先給個字串即可
                massKg,
                c,
                t0C,
                Tend,
                Qend
        );
    }

    private void setupChart() {
        dsQ = new LineDataSet(new ArrayList<>(), "Q (J)");
        dsQ.setColor(COLOR_BROWN);
        dsQ.setLineWidth(2.5f);
        dsQ.setDrawCircles(false);
        dsQ.setDrawValues(false);

        dsT = new LineDataSet(new ArrayList<>(), "T (°C)");
        dsT.setColor(0xFF8D6E63);
        dsT.setLineWidth(2.5f);
        dsT.setDrawCircles(false);
        dsT.setDrawValues(false);

        lineData = new LineData(dsQ, dsT);
        chart.setData(lineData);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        // 不可互動、不移動、不縮放
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

        // 固定時間軸 0~timeS
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(totalTimeSec);
        xAxis.setLabelCount(6, true);
        xAxis.setGranularity(Math.max(1f, timeS / 5f));

        chart.getAxisRight().setEnabled(false);

        chart.getAxisLeft().setTextColor(COLOR_BROWN);
        if (tfRegular != null) chart.getAxisLeft().setTypeface(tfRegular);

        chart.invalidate();
    }

    private void resetChart() {
        if (dsQ != null) dsQ.clear();
        if (dsT != null) dsT.clear();
        if (lineData != null) lineData.notifyDataChanged();
        if (chart != null) {
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }

    @Override
    protected void onDestroy() {
        if (animator != null) animator.cancel();
        super.onDestroy();
    }
}
