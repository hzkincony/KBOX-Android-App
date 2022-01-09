package com.kincony.KControl.ui.scan;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.kincony.KControl.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ScannerView extends View {
    private static final long LASER_ANIMATION_DELAY_MS = 10l;
    private static final int DOT_OPACITY = 0xa0;

    private static final int DOT_TTL_MS = 500;

    private final Paint maskPaint;
    private final Paint laserPaint;
    private final Paint dotPaint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final Map<ResultPoint, Long> dots = new HashMap<ResultPoint, Long>(
            16);
    private Rect frame, framePreview;
    private final Paint textPaint;

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192,
            128, 64};
    private int scannerAlpha;
    private float i = 0;// 动画变量
    private static final long ANIMATION_DELAY = 1L;

    private Bitmap topBitmap;
    private Bitmap leftBitmap;
    private Bitmap rightBitmap;
    private Bitmap bootomBitmap;
    private Bitmap lineBitmap;
    private RectF topRectF;
    private RectF leftRectF;
    private RectF rightRectF;
    private RectF bootomRectF;
    private RectF lineRectF;


    public ScannerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final Resources res = getResources();
        maskColor = res.getColor(R.color.scan_mask);
        resultColor = res.getColor(R.color.scan_result_view);
        final int laserColor = res.getColor(R.color.scan_laser);
        final int dotColor = res.getColor(R.color.scan_dot);
        scannerAlpha = 0;

        maskPaint = new Paint();
        maskPaint.setStyle(Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#FFFFFF"));
        textPaint.setStyle(Style.FILL_AND_STROKE);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        // sp to px
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, context.getResources().getDisplayMetrics()));
        // dp to px
        int DOT_SIZE = (int) (0.1 * context.getResources().getDisplayMetrics().density + 0.5f);

        laserPaint = new Paint();
        laserPaint.setColor(laserColor);
        laserPaint.setStrokeWidth(DOT_SIZE);
        laserPaint.setStyle(Style.STROKE);

        dotPaint = new Paint();
        dotPaint.setColor(dotColor);
        dotPaint.setAlpha(DOT_OPACITY);
        dotPaint.setStyle(Style.STROKE);
        dotPaint.setStrokeWidth(DOT_SIZE);
        dotPaint.setAntiAlias(true);

        topBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.code_top);
        leftBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.code_left);
        rightBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.code_right);
        bootomBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.code_bottom);
        lineBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.code_line);

        topRectF = new RectF();
        leftRectF = new RectF();
        rightRectF = new RectF();
        bootomRectF = new RectF();
        lineRectF = new RectF();
    }

    public void setFraming(final Rect frame,
                           final Rect framePreview) {
        this.frame = frame;
        this.framePreview = framePreview;
        Log.e("mating", "ScannerView -> setFraming()");



		/*topRectF = new RectF(frame.left, frame.top, frame.right, frame.top + 6);
		leftRectF = new RectF(frame.left, frame.top, frame.left + 6, frame.bottom);
		rightRectF = new RectF(frame.right - 6, frame.top, frame.right, frame.bottom);
		bootomRectF = new RectF(frame.left, frame.bottom - 6, frame.right, frame.bottom);
		lineRectF = new RectF(frame.left + 2, i + frame.top, frame.right - 1,
				i + 4 + frame.top);*/

        invalidate();
    }

    public void drawResultBitmap(final Bitmap bitmap) {
        resultBitmap = bitmap;

        invalidate();
    }

    public void addDot(final ResultPoint dot) {
        dots.put(dot, System.currentTimeMillis());

        invalidate();
    }

    @Override
    public void onDraw(final Canvas canvas) {
        if (frame == null) return;

        final long now = System.currentTimeMillis();

        final int width = canvas.getWidth();
        final int height = canvas.getHeight();

        // draw mask darkened
        maskPaint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, maskPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, maskPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, maskPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, maskPaint);


        Rect rect = new Rect();
        textPaint.getTextBounds(getResources().getString(R.string.scan_qr_code_warn), 0, getResources().getString(R.string.scan_qr_code_warn).length(), rect);
        canvas.drawText(
                getResources().getString(R.string.scan_qr_code_warn),
                Resources.getSystem().getDisplayMetrics().widthPixels / 2,
                frame.bottom + rect.height() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getContext().getResources().getDisplayMetrics()),
                textPaint
        );

        if (resultBitmap != null) {
            canvas.drawBitmap(resultBitmap, null, frame, maskPaint);
        } else {
            // draw red "laser scanner" to show decoding is active
            final boolean laserPhase = (now / 600) % 2 == 0;
            laserPaint.setAlpha(laserPhase ? 160 : 255);
            canvas.drawRect(frame, laserPaint);

            topRectF.set(frame.left, frame.top, frame.right, frame.top + 6);
            leftRectF.set(frame.left, frame.top, frame.left + 6, frame.bottom);
            rightRectF.set(frame.right - 6, frame.top, frame.right, frame.bottom);
            bootomRectF.set(frame.left, frame.bottom - 6, frame.right, frame.bottom);
            lineRectF.set(frame.left + 2, i + frame.top, frame.right - 1,
                    i + 4 + frame.top);
            /**
             * 4条边框
             */
            canvas.drawBitmap(topBitmap, null, topRectF, null);
            canvas.drawBitmap(leftBitmap, null, leftRectF, null);
            canvas.drawBitmap(rightBitmap, null, rightRectF, null);
            canvas.drawBitmap(bootomBitmap, null, bootomRectF, null);

            /**
             * 绘制一条中间的红线
             * */
            maskPaint.setColor(maskColor);
            maskPaint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            canvas.drawBitmap(lineBitmap, null, lineRectF, maskPaint);

            /**
             * 当我们获得结果的时候，我们只需要更新框框内部的内容 中间的框
             */
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
            if (i < (frame.top + frame.height() - 2)) {
                i += 10;
                if (i > (frame.height() - 2)) {
                    i = 0;
                }
                invalidate();
            }
            // draw points
            final int frameLeft = frame.left;
            final int frameTop = frame.top;
            final float scaleX = frame.width() / (float) framePreview.width();
            final float scaleY = frame.height() / (float) framePreview.height();

            for (final Iterator<Map.Entry<ResultPoint, Long>> i = dots.entrySet().iterator(); i.hasNext(); ) {
                final Map.Entry<ResultPoint, Long> entry = i.next();
                final long age = now - entry.getValue();
                if (age < DOT_TTL_MS) {
                    dotPaint.setAlpha((int) ((DOT_TTL_MS - age) * 256 / DOT_TTL_MS));

                    final ResultPoint point = entry.getKey();
                    canvas.drawPoint(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY), dotPaint);
                } else {
                    i.remove();
                }
            }

            // schedule redraw
            postInvalidateDelayed(LASER_ANIMATION_DELAY_MS);
        }
    }
}
