package com.gaiusnetworks.gaius.widget.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gaiusnetworks.gaius.utils.Layer;


public class RectEntity extends MotionEntity {
    @NonNull
    private Bitmap bitmap;
    private int color;
    private Paint paint;
    private Canvas canvas;
    private RectF rectF;

    public RectEntity(@NonNull Layer layer,
                      @IntRange(from = 1) int canvasWidth,
                      @IntRange(from = 1) int canvasHeight,
                      Context ctx,
                      @Nullable Integer x,
                      int y,
                      int w,
                      int h) {

        super(layer, canvasWidth, canvasHeight, ctx, x, y);
        rectF = new RectF();
        color = Color.GRAY;

        if (x != null) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        canvas = new Canvas(bitmap);

        paint = new Paint();
        paint.setColor(this.color);
        canvas.drawRect(0,0, bitmap.getWidth(), bitmap.getHeight(), paint);

        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        float widthAspect = 1.0F * canvasWidth / width;
        float heightAspect = 1.0F * canvasHeight / height;
        // fit the smallest size
        holyScale = Math.min(widthAspect, heightAspect);

        // initial position of the entity
        srcPoints[0] = 0;
        srcPoints[1] = 0;
        srcPoints[2] = width;
        srcPoints[3] = 0;
        srcPoints[4] = width;
        srcPoints[5] = height;
        srcPoints[6] = 0;
        srcPoints[7] = height;
        srcPoints[8] = 0;
        srcPoints[8] = 0;
    }

    @Override
    public void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        PointF p1 = getUpperLeftCoordinates();
        PointF p2 = getLowerRightCoordinates();

        rectF.set(p1.x,p1.y, p2.x, p2.y);
        canvas.drawRect(rectF, paint);

//        canvas.drawBitmap(bitmap, matrix, drawingPaint);
    }

    @Override
    public int getWidth() {
        return bitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return bitmap.getHeight();
    }

    public int getColor() {return color;}


    public void changeColor (int selectedColor) {
        color = selectedColor;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
//        canvas.drawRect(0,0, bitmap.getWidth(), bitmap.getHeight(), paint);

//        // border
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(7.0f);
//        paint.setColor(Color.BLACK);
//        canvas.drawRect(0,0, bitmap.getWidth(), bitmap.getHeight(), paint);
    }

    @Override
    public void release() {
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public void increaseWidth() {
        Bitmap b = Bitmap.createScaledBitmap(bitmap, this.getWidth() + 10, this.getHeight(), false);
        updateEntity(b);
    }

    public void decreaseWidth() {
        if (this.getWidth() > 10) {
            Bitmap b = Bitmap.createScaledBitmap(bitmap, this.getWidth() - 10, this.getHeight(), false);
            updateEntity(b);
        }
    }

    public void increaseHeight() {
        Bitmap b = Bitmap.createScaledBitmap(bitmap, this.getWidth(), this.getHeight() + 10, false);
        updateEntity(b);
    }

    public void decreaseHeight() {
        if (this.getHeight() > 10) {
            Bitmap b = Bitmap.createScaledBitmap(bitmap, this.getWidth(), this.getHeight() - 10, false);
            updateEntity(b);
        }
    }

    public void updateEntity (Bitmap b) {
        // save previous center
        PointF oldCenter = absoluteCenter();

        bitmap = b;

        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        // initial position of the entity
        srcPoints[0] = 0;
        srcPoints[1] = 0;
        srcPoints[2] = width;
        srcPoints[3] = 0;
        srcPoints[4] = width;
        srcPoints[5] = height;
        srcPoints[6] = 0;
        srcPoints[7] = height;
        srcPoints[8] = 0;
        srcPoints[8] = 0;

        moveCenterTo(oldCenter);

        canvas = new Canvas(bitmap);
    }

}

