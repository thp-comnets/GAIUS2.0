package com.gaius.gaiusapp.utils;

import android.content.Context;
import android.graphics.Matrix;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class TopCropImageView extends AppCompatImageView {
    public TopCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TopCropImageView(Context context) {
        super(context);
    }

    // this makes the image of pages TOP_CROP
    // https://stackoverflow.com/questions/6330084/imageview-scaling-top-crop
    @Override
    protected boolean setFrame(int l, int t, int r, int b)
    {
        Matrix matrix = getImageMatrix();
        float scaleFactor = getWidth()/(float)getDrawable().getIntrinsicWidth();
        matrix.setScale(scaleFactor, scaleFactor, 0, 0);
        setImageMatrix(matrix);
        return super.setFrame(l, t, r, b);
    }

    //if the image shows only white background, try the code below

    // this makes the image of pages TOP_CROP
    // https://gist.github.com/arriolac/3843346
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//        recomputeImgMatrix();
//    }
//
//    @Override
//    protected boolean setFrame(int l, int t, int r, int b) {
//        recomputeImgMatrix();
//        return super.setFrame(l, t, r, b);
//    }
//
//    private void recomputeImgMatrix() {
//        final Matrix matrix = getImageMatrix();
//
//        float scale;
//        final int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
//        final int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
//        final int drawableWidth = getDrawable().getIntrinsicWidth();
//        final int drawableHeight = getDrawable().getIntrinsicHeight();
//
//        if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
//            scale = (float) viewHeight / (float) drawableHeight;
//        } else {
//            scale = (float) viewWidth / (float) drawableWidth;
//        }
//
//        Log.d("thp", "setframe " + getDrawable() + getImageMatrix());
//
//        matrix.setScale(scale, scale);
//        setImageMatrix(matrix);
//    }

}
