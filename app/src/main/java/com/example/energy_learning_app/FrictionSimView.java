package com.example.energy_learning_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class FrictionSimView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrow = new Path();

    private float xNorm = 0f; // 0..1
    private float fApplied = 0f;
    private float fFriction = 0f;
    private boolean moving = false;

    public FrictionSimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setState(float xNorm, float fApplied, float fFriction, boolean moving) {
        this.xNorm = clamp01(xNorm);
        this.fApplied = fApplied;
        this.fFriction = fFriction;
        this.moving = moving;
        invalidate();
    }

    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

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

        float groundY = h * 0.82f;

        // 地面
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5));
        paint.setColor(dark);
        canvas.drawLine(w * 0.06f, groundY, w * 0.94f, groundY, paint);

        // 方塊位置
        float blockW = w * 0.16f;
        float blockH = h * 0.18f;

        float leftMin = w * 0.12f;
        float leftMax = w * 0.82f - blockW;
        float x = leftMin + (leftMax - leftMin) * xNorm;
        float y = groundY - blockH;

        // 方塊
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fill);
        canvas.drawRoundRect(x, y, x + blockW, y + blockH, dp(14), dp(14), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(brown);
        paint.setStrokeWidth(dp(3));
        canvas.drawRoundRect(x, y, x + blockW, y + blockH, dp(14), dp(14), paint);

        // 推力箭頭（右）
        drawForceArrow(canvas, x + blockW + dp(10), y + blockH * 0.50f, +1, fApplied, "F");

        // 摩擦力箭頭（左）
        drawForceArrow(canvas, x - dp(10), y + blockH * 0.70f, -1, fFriction, "f");

        // 狀態文字
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(brown);
        paint.setTextSize(dp(16));
        canvas.drawText(moving ? "動摩擦：開始滑動" : "靜摩擦：卡住不動", w * 0.08f, h * 0.18f, paint);
    }

    private void drawForceArrow(Canvas canvas, float startX, float y, int dir, float force, String label) {
        int brown = 0xFF5A3600;

        // 讓箭頭長度跟力大小有感（但限制最大）
        float len = dp(30) + dp(Math.min(80, force * 2f));

        float endX = startX + dir * len;

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(brown);
        paint.setStrokeWidth(dp(3));
        canvas.drawLine(startX, y, endX, y, paint);

        arrow.reset();
        arrow.moveTo(endX, y);
        arrow.lineTo(endX - dir * dp(12), y - dp(8));
        arrow.moveTo(endX, y);
        arrow.lineTo(endX - dir * dp(12), y + dp(8));
        canvas.drawPath(arrow, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(dp(14));
        canvas.drawText(label, endX + dir * dp(6), y - dp(10), paint);
    }
}
