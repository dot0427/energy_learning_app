package com.example.energy_learning_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SlideSimView extends View {

    private float angleDeg = 30f;
    private float progress = 0f; // 0(top) -> 1(bottom)

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SlideSimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeWidth(dp(6));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setAngleDeg(float angleDeg) {
        this.angleDeg = angleDeg;
        invalidate();
    }

    public void setProgress(float progress) {
        this.progress = clamp(progress, 0f, 1f);
        invalidate();
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        float groundY = h * 0.85f;
        float baseX = w * 0.88f;
        float baseY = groundY;

        float L = h * 0.70f;

        double theta = Math.toRadians(Math.max(5f, angleDeg));
        float run = (float) (L * Math.cos(theta));
        float rise = (float) (L * Math.sin(theta));

        float topX = baseX - run;
        float topY = baseY - rise;

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF3E2723);
        paint.setStrokeWidth(dp(5));
        canvas.drawLine(w * 0.05f, groundY, w * 0.95f, groundY, paint);

        float projX = topX;
        float projY = baseY;

        android.graphics.Path triPath = new android.graphics.Path();
        triPath.moveTo(baseX, baseY);
        triPath.lineTo(topX, topY);
        triPath.lineTo(projX, projY);
        triPath.close();

        int alpha = 120;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(android.graphics.Color.argb(alpha, 255, 167, 38));
        canvas.drawPath(triPath, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(0xFF5A3600);
        canvas.drawPath(triPath, paint);

        paint.setColor(0xFF5A3600);
        paint.setStrokeWidth(dp(4));
        canvas.drawLine(topX, topY, baseX, baseY, paint);

        float ballX = topX + (baseX - topX) * progress;
        float ballY = topY + (baseY - topY) * progress;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF222222);
        canvas.drawCircle(ballX, ballY, dp(10), paint);
    }
}
