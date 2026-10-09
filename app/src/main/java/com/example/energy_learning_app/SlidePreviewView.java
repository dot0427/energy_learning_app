package com.example.energy_learning_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SlidePreviewView extends View {

    // 使用者調的是「高度」
    private float heightM = 5f;   // 0~10 (m)
    private float angleDeg = 30f; // 10~60 (deg)

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path triPath = new Path();
    private final RectF arcRect = new RectF();

    public SlidePreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setHeightM(float h) {
        heightM = h;
        invalidate();
    }

    public void setAngleDeg(float a) {
        angleDeg = a;
        invalidate();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        // ===== 地面 & 固定底點 =====
        float groundY = h * 0.85f;
        float baseX = w * 0.85f;
        float baseY = groundY;

        // ===== 高度映射（m -> px）=====
        float maxRisePx = h * 0.60f;
        float rise = (heightM / 10f) * maxRisePx;

        // ===== 角度 -> run =====
        float safeAngle = Math.max(5f, angleDeg); // 避免 tan(0)
        double thetaRad = Math.toRadians(safeAngle);
        float run = (float) (rise / Math.tan(thetaRad));

        // 左邊界避免跑出畫面
        float leftLimit = w * 0.10f;
        if (baseX - run < leftLimit) {
            run = baseX - leftLimit;
            rise = (float) (run * Math.tan(thetaRad));
        }

        // 上端點
        float topX = baseX - run;
        float topY = baseY - rise;

        // 垂直投影點（直角三角形的直角點）
        float projX = topX;
        float projY = baseY;

        // ===== 畫地面 =====
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5));
        paint.setColor(0xFF3E2723);
        canvas.drawLine(w * 0.05f, groundY, w * 0.95f, groundY, paint);

        // ===== 三角形填色 =====
        triPath.reset();
        triPath.moveTo(baseX, baseY);
        triPath.lineTo(topX, topY);
        triPath.lineTo(projX, projY);
        triPath.close();

        int alpha = 70 + (int) (heightM / 10f * 140); // 70~210
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(android.graphics.Color.argb(alpha, 255, 167, 38));
        canvas.drawPath(triPath, paint);

        // 外框
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(0xFF5A3600);
        canvas.drawPath(triPath, paint);

        // 斜面線
        canvas.drawLine(topX, topY, baseX, baseY, paint);

        // 底點（固定）
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF5A3600);
        canvas.drawCircle(baseX, baseY, dp(6), paint);

        // 上端點小點（可刪）
        paint.setColor(0xFF8D6E63);
        canvas.drawCircle(topX, topY, dp(4), paint);

        // =========================================================
        // ✅ 高度箭頭 + h
        // =========================================================
        // 在垂直邊旁邊畫高度箭頭：從投影點(projX, baseY)到上端(topY)
        float arrowX = projX - dp(12); // 往左偏一點避免壓到邊線
        float yBottom = baseY;
        float yTop = topY;

        // 箭頭主線
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(0xFF5A3600);
        canvas.drawLine(arrowX, yBottom, arrowX, yTop, paint);

        // 箭頭兩端小橫線（看起來像量測線）
        canvas.drawLine(arrowX - dp(6), yBottom, arrowX + dp(6), yBottom, paint);
        canvas.drawLine(arrowX - dp(6), yTop, arrowX + dp(6), yTop, paint);

        // 箭頭尖（上）
        drawArrowHead(canvas, arrowX, yTop, 270); // 270° = 朝上
        // 箭頭尖（下）
        drawArrowHead(canvas, arrowX, yBottom, 90); // 90° = 朝下

        // h 標示（放在箭頭中間）
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(dp(14));
        paint.setColor(0xFF5A3600);
        float midY = (yTop + yBottom) / 2f;
        canvas.drawText("h", arrowX - dp(18), midY + dp(5), paint);

        // =========================================================
        // ✅ 角度弧線 + θ
        // =========================================================
        // 角度在底點：地面(往左的水平線)與斜面之間
        float r = dp(28); // 弧線半徑
        arcRect.set(baseX - r, baseY - r, baseX + r, baseY + r);

        // 地面方向是 180°（向左），斜面方向相對地面上抬 safeAngle
        // drawArc 的角度：0°在右、90°下、180°左、270°上
        float startAngle = 180f;           // 從向左開始
        float sweepAngle = safeAngle;     // 往上掃（用負號）

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(0xFF5A3600);
        canvas.drawArc(arcRect, startAngle, sweepAngle, false, paint);

        // 角平分線角度（從地面向左算起）
        float midAngle = startAngle + safeAngle / 2f;

        // 文字離底點的距離（略大於弧線半徑）
        float labelR = r + dp(14);

        // Canvas 的角度是「螢幕角度」
        // 所以直接用 cos / sin 算位置
        float thetaLabelX = baseX + (float) Math.cos(Math.toRadians(midAngle)) * labelR;
        float thetaLabelY = baseY + (float) Math.sin(Math.toRadians(midAngle)) * labelR;

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(dp(14));
        paint.setColor(0xFF5A3600);

        // 小修正：讓文字置中比較好看
        canvas.drawText("θ", thetaLabelX - dp(6), thetaLabelY + dp(6), paint);
    }

    private void drawArrowHead(Canvas canvas, float x, float y, float directionDeg) {
        float size = dp(6);
        double dir = Math.toRadians(directionDeg);

        // 兩個翅膀角度
        double left = dir + Math.toRadians(25);
        double right = dir - Math.toRadians(25);

        float x1 = x + (float) (Math.cos(left) * size);
        float y1 = y + (float) (Math.sin(left) * size);
        float x2 = x + (float) (Math.cos(right) * size);
        float y2 = y + (float) (Math.sin(right) * size);

        canvas.drawLine(x, y, x1, y1, paint);
        canvas.drawLine(x, y, x2, y2, paint);
    }
}
