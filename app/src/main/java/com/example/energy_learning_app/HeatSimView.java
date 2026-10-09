package com.example.energy_learning_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class HeatSimView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float progress = 0f;   // 0..1
    private float tNow = 25f;      // °C

    public HeatSimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setProgress(float p, float tempC) {
        progress = Math.max(0f, Math.min(1f, p));
        tNow = tempC;
        invalidate();
    }

    private float dp(float v){ return v * getResources().getDisplayMetrics().density; }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        int brown = 0xFF5A3600;

        // 加熱板
        float plateY = h*0.80f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x22FFA726);
        canvas.drawRoundRect(w*0.18f, plateY, w*0.82f, plateY+dp(18), dp(14), dp(14), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(brown);
        paint.setStrokeWidth(dp(3));
        canvas.drawRoundRect(w*0.18f, plateY, w*0.82f, plateY+dp(18), dp(14), dp(14), paint);

        // 方塊（溫度越高越偏橘紅）
        float blockW=w*0.18f, blockH=h*0.22f;
        float bx=w*0.41f, by=plateY-blockH;

        // 把溫度映射到 0..1（0~80°C）
        float t01 = Math.max(0f, Math.min(1f, tNow/80f));
        int fill = lerpColor(0x22FFA726, 0x88FF7043, t01);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fill);
        canvas.drawRoundRect(bx, by, bx+blockW, by+blockH, dp(16), dp(16), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(brown);
        paint.setStrokeWidth(dp(3));
        canvas.drawRoundRect(bx, by, bx+blockW, by+blockH, dp(16), dp(16), paint);

        // 熱氣（progress 越大越明顯、越長）
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(0x66FF7043);

        float steamTop = by - dp(18) - dp(22)*progress;
        for (int i=0;i<3;i++){
            float x = bx + blockW*(0.25f + i*0.25f);
            canvas.drawLine(x, by-dp(8), x, steamTop, paint);
        }

        // 溫度文字
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(brown);
        paint.setTextSize(dp(16));
        canvas.drawText(String.format(java.util.Locale.getDefault(),"T = %.1f °C", tNow), w*0.08f, h*0.18f, paint);
    }

    private int lerpColor(int c1, int c2, float t){
        int a1=(c1>>24)&0xff, r1=(c1>>16)&0xff, g1=(c1>>8)&0xff, b1=c1&0xff;
        int a2=(c2>>24)&0xff, r2=(c2>>16)&0xff, g2=(c2>>8)&0xff, b2=c2&0xff;
        int a=(int)(a1+(a2-a1)*t);
        int r=(int)(r1+(r2-r1)*t);
        int g=(int)(g1+(g2-g1)*t);
        int b=(int)(b1+(b2-b1)*t);
        return (a<<24)|(r<<16)|(g<<8)|b;
    }
}
