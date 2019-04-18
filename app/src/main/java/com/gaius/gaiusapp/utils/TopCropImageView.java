package com.gaius.gaiusapp.utils;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TopCropImageView extends ImageView {
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
}
