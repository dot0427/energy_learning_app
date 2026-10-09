package com.example.energy_learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.util.Locale;

public class FrictionSetupActivity extends AppCompatActivity {

    private FrictionPreviewView frictionPreview;

    private Slider sliderMass, sliderMuS, sliderMuK, sliderFmax;
    private TextView tvMass, tvMuS, tvMuK, tvFmax;
    private Button btnReset, btnNext;

    private static final float DEFAULT_MASS = 2.0f;
    private static final float DEFAULT_MUS  = 0.40f;
    private static final float DEFAULT_MUK  = 0.30f;
    private static final float DEFAULT_FMAX = 25f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friction_setup);

        bindViews();
        setupListeners();
        applyValues(DEFAULT_MASS, DEFAULT_MUS, DEFAULT_MUK, DEFAULT_FMAX);
    }

    private void bindViews() {
        frictionPreview = findViewById(R.id.frictionPreview);

        sliderMass = findViewById(R.id.sliderMass);
        sliderMuS  = findViewById(R.id.sliderMuS);
        sliderMuK  = findViewById(R.id.sliderMuK);
        sliderFmax = findViewById(R.id.sliderFmax);

        tvMass = findViewById(R.id.textViewFrictionMass);
        tvMuS  = findViewById(R.id.textViewFrictionMuS);
        tvMuK  = findViewById(R.id.textViewFrictionMuK);
        tvFmax = findViewById(R.id.textViewFrictionFmax);

        btnReset = findViewById(R.id.buttonFrictionReset);
        btnNext  = findViewById(R.id.buttonFrictionNext);
    }

    private void setupListeners() {
        sliderMass.addOnChangeListener((s, v, fromUser) -> {
            tvMass.setText(String.format(Locale.getDefault(), "%.1f kg", v));
            if (frictionPreview != null) frictionPreview.setMassKg(v);
        });

        sliderMuS.addOnChangeListener((s, v, fromUser) -> {
            tvMuS.setText(String.format(Locale.getDefault(), "%.2f", v));

            // 讓 μk 不要大於 μs（比較符合物理）
            if (sliderMuK.getValue() > v) sliderMuK.setValue(v);

            if (frictionPreview != null) frictionPreview.setMuS(v);
        });

        sliderMuK.addOnChangeListener((s, v, fromUser) -> {
            float muS = sliderMuS.getValue();
            if (v > muS) {
                sliderMuK.setValue(muS);
                return;
            }
            tvMuK.setText(String.format(Locale.getDefault(), "%.2f", v));
            if (frictionPreview != null) frictionPreview.setMuK(v);
        });

        sliderFmax.addOnChangeListener((s, v, fromUser) -> {
            tvFmax.setText(String.format(Locale.getDefault(), "%.0f N", v));
            if (frictionPreview != null) frictionPreview.setFmaxN(v);
        });

        btnReset.setOnClickListener(v ->
                applyValues(DEFAULT_MASS, DEFAULT_MUS, DEFAULT_MUK, DEFAULT_FMAX)
        );

        btnNext.setOnClickListener(v -> {
            float m   = sliderMass.getValue();
            float muS = sliderMuS.getValue();
            float muK = sliderMuK.getValue();
            float fMax = sliderFmax.getValue();

            Intent intent = new Intent(this, FrictionSimulationActivity.class);
            intent.putExtra("mass_kg", m);
            intent.putExtra("mu_s", muS);
            intent.putExtra("mu_k", muK);
            intent.putExtra("f_max", fMax);

            startActivity(intent);
        });
    }

    private void applyValues(float m, float muS, float muK, float fMax) {
        sliderMass.setValue(m);
        sliderMuS.setValue(muS);
        sliderMuK.setValue(muK);
        sliderFmax.setValue(fMax);

        tvMass.setText(String.format(Locale.getDefault(), "%.1f kg", m));
        tvMuS.setText(String.format(Locale.getDefault(), "%.2f", muS));
        tvMuK.setText(String.format(Locale.getDefault(), "%.2f", muK));
        tvFmax.setText(String.format(Locale.getDefault(), "%.0f N", fMax));

        if (frictionPreview != null) {
            frictionPreview.setMassKg(m);
            frictionPreview.setMuS(muS);
            frictionPreview.setMuK(muK);
            frictionPreview.setFmaxN(fMax);
        }
    }
}
