package com.example.energy_learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.util.Locale;

public class SlideSetupActivity extends AppCompatActivity {

    private SlidePreviewView slidePreview;

    private Slider slideHeight, slideAngle, slideMass, slideFric;

    private TextView textViewSlideHeight, textViewSlideAngle, textViewSlideMass, textViewSlideFric;

    private Button buttonSlideReset, buttonSlideNext;

    private static final float DEFAULT_HEIGHT_M = 5.0f;   // m
    private static final float DEFAULT_ANGLE_DEG = 30.0f; // deg
    private static final float DEFAULT_MASS_KG = 2.0f;    // kg
    private static final float DEFAULT_FRIC_MU = 0.20f;   // μ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_setup);

        bindViews();
        setupRanges();
        setupListeners();

        applyValues(DEFAULT_HEIGHT_M, DEFAULT_ANGLE_DEG, DEFAULT_MASS_KG, DEFAULT_FRIC_MU);
    }

    private void bindViews() {
        slidePreview = findViewById(R.id.slidePreview);

        slideHeight = findViewById(R.id.slideHeight);
        slideAngle  = findViewById(R.id.slideAngle);
        slideMass   = findViewById(R.id.slideMass);
        slideFric   = findViewById(R.id.slideFric);

        textViewSlideHeight = findViewById(R.id.textViewSlideHeight);
        textViewSlideAngle  = findViewById(R.id.textViewSlideAngle);
        textViewSlideMass   = findViewById(R.id.textViewSlideMass);
        textViewSlideFric   = findViewById(R.id.textViewSlideFric);

        buttonSlideReset = findViewById(R.id.buttonSlideReset);
        buttonSlideNext  = findViewById(R.id.buttonSlideNext);
    }

    private void setupRanges() {
        // Height：0~10 m, step 0.5
        slideHeight.setValueFrom(0f);
        slideHeight.setValueTo(10f);
        slideHeight.setStepSize(0.5f);

        // Angle：10~60 deg, step 1
        slideAngle.setValueFrom(10f);
        slideAngle.setValueTo(60f);
        slideAngle.setStepSize(1f);

        // Mass：0.5~10 kg, step 0.5
        slideMass.setValueFrom(0.5f);
        slideMass.setValueTo(10f);
        slideMass.setStepSize(0.5f);

        // Friction μ：0~1, step 0.05
        slideFric.setValueFrom(0f);
        slideFric.setValueTo(1f);
        slideFric.setStepSize(0.05f);
    }

    private void setupListeners() {
        slideHeight.addOnChangeListener((slider, value, fromUser) -> {
            updateHeightText(value);
            if (slidePreview != null) slidePreview.setHeightM(value);
        });

        slideAngle.addOnChangeListener((slider, value, fromUser) -> {
            updateAngleText(value);
            if (slidePreview != null) slidePreview.setAngleDeg(value);
        });

        slideMass.addOnChangeListener((slider, value, fromUser) -> {
            updateMassText(value);
            // 如果你之後想「球大小跟質量」連動，可在 SlidePreviewView 加 setMassKg(value)
        });

        slideFric.addOnChangeListener((slider, value, fromUser) -> {
            updateFrictionText(value);
            // 如果你之後想「摩擦顏色/粒子」連動，可在 SlidePreviewView 加 setFrictionMu(value)
        });

        buttonSlideReset.setOnClickListener(v ->
                applyValues(DEFAULT_HEIGHT_M, DEFAULT_ANGLE_DEG, DEFAULT_MASS_KG, DEFAULT_FRIC_MU)
        );

        buttonSlideNext.setOnClickListener(v -> {
            float h = slideHeight.getValue();
            float angle = slideAngle.getValue();
            float m = slideMass.getValue();
            float mu = slideFric.getValue();

            Intent intent = new Intent(this, SlideSimulationActivity.class);

            intent.putExtra("height_m", h);
            intent.putExtra("angle_deg", angle);
            intent.putExtra("mass_kg", m);
            intent.putExtra("friction_mu", mu);

            startActivity(intent);
            setResult(RESULT_OK, intent);
        });
    }

    private void applyValues(float h, float angle, float m, float mu) {
        slideHeight.setValue(h);
        slideAngle.setValue(angle);
        slideMass.setValue(m);
        slideFric.setValue(mu);

        updateHeightText(h);
        updateAngleText(angle);
        updateMassText(m);
        updateFrictionText(mu);

        if (slidePreview != null) {
            slidePreview.setHeightM(h);
            slidePreview.setAngleDeg(angle);
        }
    }

    private void updateHeightText(float value) {
        textViewSlideHeight.setText(String.format(Locale.getDefault(), "%.1f m", value));
    }

    private void updateAngleText(float value) {
        textViewSlideAngle.setText(String.format(Locale.getDefault(), "%.0f °", value));
    }

    private void updateMassText(float value) {
        textViewSlideMass.setText(String.format(Locale.getDefault(), "%.1f kg", value));
    }

    private void updateFrictionText(float value) {
        textViewSlideFric.setText(String.format(Locale.getDefault(), "%.2f", value));
    }
}
