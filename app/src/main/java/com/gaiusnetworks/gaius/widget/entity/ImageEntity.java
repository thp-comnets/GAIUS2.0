package com.gaius.gaiusapp.widget.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gaius.gaiusapp.utils.Layer;


public class ImageEntity extends MotionEntity {

    @NonNull
    private Bitmap bitmap;
    private Layer layer;
    private Paint paint;
    @Nullable
    private Integer x,y;

    public ImageEntity(@NonNull Layer layer,
                       @NonNull Bitmap bitmap,
                       @IntRange(from = 1) int canvasWidth,
                       @IntRange(from = 1) int canvasHeight,
                       Context ctx,
                       @Nullable Integer x,
                       int y) {
        super(layer, canvasWidth, canvasHeight, ctx, x, y);

        this.bitmap = bitmap;
        this.layer = layer;
        this.x = x;
        this.y = y;

        float width, height;

        //if it is a video, don't use the thumbnail size
//        if (layer.isVideo() && layer.videoHasThumbnail()) {
////            width = canvasWidth;
////            height = canvasHeight;
//        }

        width = bitmap.getWidth();
        height = bitmap.getHeight();

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

    public void updateEntity (Bitmap b, String path) {
        this.bitmap = b;
        this.layer.setPath(path);
        this.layer.setIsNewContent(true);

        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        float widthAspect = 1.0F * canvasWidth / width;
        float heightAspect = 1.0F * canvasHeight / height;
        // fit the smallest size
        holyScale = Math.min(widthAspect, heightAspect);

        // save previous center
        PointF oldCenter = absoluteCenter();

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
    }

    @Override
    public void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        canvas.drawBitmap(bitmap, matrix, drawingPaint);
    }

    @Override
    public int getWidth() {
        return bitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return bitmap.getHeight();
    }

    @Override
    public void release() {
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}