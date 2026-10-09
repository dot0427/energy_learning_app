package com.example.energy_learning_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class HeatPreviewView extends View {

    // ✅ 只需要質量來決定大小
    private float massKg = 2f; // 0.5~10

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public HeatPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    // ✅ 給 SetupActivity 呼叫：質量改變就更新 preview
    public void setMassKg(float m) {
        massKg = m;
        invalidate();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        // 你全 app 的咖啡色
        int brown = 0xFF5A3600;

        // =========================
        // ✅ 固定的加熱板（不隨溫度變色）
        // =========================
        float plateY = h * 0.78f;

        // 固定淡橘底（不會變深）
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x22FFA726);
        canvas.drawRoundRect(w * 0.18f, plateY, w * 0.82f, plateY + dp(18),
                dp(14), dp(14), paint);

        // 咖啡色邊框
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(brown);
        paint.setStrokeWidth(dp(3));
        canvas.drawRoundRect(w * 0.18f, plateY, w * 0.82f, plateY + dp(18),
                dp(14), dp(14), paint);

        // =========================
        // ✅ 方塊大小隨質量變化（0.5~10kg → 0.8x~1.35x）
        // =========================
        float s = 0.8f + 0.55f * ((massKg - 0.5f) / (10f - 0.5f));
        s = clamp(s, 0.8f, 1.35f);

        float baseW = w * 0.18f;
        float baseH = h * 0.22f;

        float blockW = baseW * s;
        float blockH = baseH * s;

        // ✅ 置中 + 底部貼著加熱板
        float bx = (w - blockW) / 2f;
        float by = plateY - blockH;

        // 固定填色（不隨溫度變深）
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x33FF7043); // 固定淡橘紅（你可換成更淡/更深）
        canvas.drawRoundRect(bx, by, bx + blockW, by + blockH,
                dp(16), dp(16), paint);

        // 咖啡色邊框
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(brown);
        paint.setStrokeWidth(dp(3));
        canvas.drawRoundRect(bx, by, bx + blockW, by + blockH,
                dp(16), dp(16), paint);

        // ✅ 不畫熱氣、不畫任何文字（你要的乾淨版）
    }
}
