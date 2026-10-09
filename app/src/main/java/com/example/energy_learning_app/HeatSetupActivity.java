package com.example.energy_learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.util.Locale;

public class HeatSetupActivity extends AppCompatActivity {

    private HeatPreviewView heatPreview;

    private Slider sliderMass, sliderC, sliderP, sliderTime;
    private TextView tvMass, tvC, tvP, tvTime;

    private static final float DEF_MASS = 2f;
    private static final float DEF_C = 900f;   // J/kg°C（鋁大概 900）
    private static final float DEF_P = 500f;   // W
    private static final float DEF_T = 10f;    // s
    private static final float DEF_T0 = 25f;    // °C

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_setup);

        bindViews();
        setupListeners();
        applyValues(DEF_MASS, DEF_C, DEF_P, DEF_T, DEF_T0);
    }

    private void bindViews() {
        heatPreview = findViewById(R.id.heatPreview);

        sliderMass = findViewById(R.id.sliderMass);
        sliderC = findViewById(R.id.sliderC);
        sliderP = findViewById(R.id.sliderP);
        sliderTime = findViewById(R.id.sliderTime);

        tvMass = findViewById(R.id.textViewHeatMass);
        tvC = findViewById(R.id.textViewHeatC);
        tvP = findViewById(R.id.textViewHeatP);
        tvTime = findViewById(R.id.textViewHeatTime);

        Button btnReset = findViewById(R.id.buttonHeatReset);
        Button btnNext = findViewById(R.id.buttonHeatNext);

        btnReset.setOnClickListener(v -> applyValues(DEF_MASS, DEF_C, DEF_P, DEF_T, DEF_T0));

        btnNext.setOnClickListener(v -> {
            Intent i = new Intent(this, HeatSimulationActivity.class);
            i.putExtra("mass_kg", sliderMass.getValue());
            i.putExtra("c_j_per_kgc", sliderC.getValue());
            i.putExtra("power_w", sliderP.getValue());
            i.putExtra("time_s", sliderTime.getValue());
            i.putExtra("t0_c", 25f); // 固定初溫
            startActivity(i);
        });
    }

    private void setupListeners() {
        sliderMass.addOnChangeListener((s, v, fromUser) -> {
            tvMass.setText(String.format(Locale.getDefault(), "%.1f kg", v));
            pushPreview();
        });
        sliderC.addOnChangeListener((s, v, fromUser) -> {
            tvC.setText(String.format(Locale.getDefault(), "%.0f J/kg·°C", v));
            pushPreview();
        });
        sliderP.addOnChangeListener((s, v, fromUser) -> {
            tvP.setText(String.format(Locale.getDefault(), "%.0f W", v));
            pushPreview();
        });
        sliderTime.addOnChangeListener((s, v, fromUser) -> {
            tvTime.setText(String.format(Locale.getDefault(), "%.0f s", v));
            pushPreview();
        });
    }

    private void applyValues(float m, float c, float p, float t, float t0) {
        sliderMass.setValue(m);
        sliderC.setValue(c);
        sliderP.setValue(p);
        sliderTime.setValue(t);

        tvMass.setText(String.format(Locale.getDefault(), "%.1f kg", m));
        tvC.setText(String.format(Locale.getDefault(), "%.0f J/kg·°C", c));
        tvP.setText(String.format(Locale.getDefault(), "%.0f W", p));
        tvTime.setText(String.format(Locale.getDefault(), "%.0f s", t));

        pushPreview();
    }

    private void pushPreview() {
        if (heatPreview == null) return;
        heatPreview.setMassKg(sliderMass.getValue());
    }
}