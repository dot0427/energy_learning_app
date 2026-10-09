package com.example.energy_learning_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class FrictionPreviewView extends View {

    private float massKg = 2f;
    private float muS = 0.40f;
    private float muK = 0.30f;
    private float fMaxN = 25f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrow = new Path();

    public FrictionPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setMassKg(float v) { massKg = v; invalidate(); }
    public void setMuS(float v)    { muS = v; invalidate(); }
    public void setMuK(float v)    { muK = v; invalidate(); }
    public void setFmaxN(float v)  { fMaxN = v; invalidate(); }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        int brown = 0xFF5A3600;
        int dark  = 0xFF3E2723;
        int fill  = 0x22FFA726;

        float groundY = h * 0.80f;

        // 地面
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5));
        paint.setColor(dark);
        canvas.drawLine(w * 0.06f, groundY, w * 0.94f, groundY, paint);

        // 方塊
        float blockW = w * 0.18f;
        float blockH = h * 0.18f;
        float x = w * 0.25f;
        float y = groundY - blockH;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fill);
        canvas.drawRoundRect(x, y, x + blockW, y + blockH, dp(12), dp(12), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(brown);
        paint.setStrokeWidth(dp(3));
        canvas.drawRoundRect(x, y, x + blockW, y + blockH, dp(12), dp(12), paint);

        // 推力箭頭（右向）
        float ax1 = x + blockW + dp(12);
        float ay1 = y + blockH * 0.50f;
        float ax2 = ax1 + dp(60);

        paint.setStrokeWidth(dp(3));
        canvas.drawLine(ax1, ay1, ax2, ay1, paint);

        arrow.reset();
        arrow.moveTo(ax2, ay1);
        arrow.lineTo(ax2 - dp(10), ay1 - dp(8));
        arrow.moveTo(ax2, ay1);
        arrow.lineTo(ax2 - dp(10), ay1 + dp(8));
        canvas.drawPath(arrow, paint);

        // 文字
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(brown);
        paint.setTextSize(dp(14));

        canvas.drawText("F (推力)", ax1, ay1 - dp(12), paint);
        canvas.drawText("m=" + String.format(java.util.Locale.getDefault(), "%.1fkg", massKg), x, y - dp(10), paint);
        canvas.drawText("μs=" + String.format(java.util.Locale.getDefault(), "%.2f", muS)
                + "  μk=" + String.format(java.util.Locale.getDefault(), "%.2f", muK), x + dp(120), y - dp(10), paint);
    }
}
