package com.gaius.gaiusapp.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.gestures.MoveGestureDetector;
import com.gaius.gaiusapp.gestures.RotateGestureDetector;
import com.gaius.gaiusapp.widget.entity.MotionEntity;

import java.util.ArrayList;
import java.util.List;

public class MotionView  extends FrameLayout {

    private static final String TAG = MotionView.class.getSimpleName();
    private Context ctx;
    public Boolean disableMovement = false;

    public interface Constants {
        float SELECTED_LAYER_ALPHA = 0.15F;
    }

    public interface MotionViewCallback {
        void onEntitySelected(@Nullable MotionEntity entity);
        void onEntityDoubleTap(@NonNull MotionEntity entity);
    }

    // layers
    private final List<MotionEntity> entities = new ArrayList<>();
    @Nullable
    private MotionEntity selectedEntity;
    private MotionEntity tempSelectedEntity;

    private Paint selectedLayerPaint;

    // callback
    @Nullable
    private MotionViewCallback motionViewCallback;

    // gesture detection
    private ScaleGestureDetector scaleGestureDetector;
    private RotateGestureDetector rotateGestureDetector;
    private MoveGestureDetector moveGestureDetector;
    private GestureDetectorCompat gestureDetectorCompat;

    // constructors
    public MotionView(Context context) {
        super(context);
        init(context);
    }

    public MotionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MotionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MotionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (selectedEntity != null) {
                    //we need this temp entity for the dialog 'cause the dialog will deselect the clicked entity
                    tempSelectedEntity = selectedEntity;
                    PointF p = selectedEntity.getUpperRightCoordinates();
                    Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.minus);

                    if (p.x - mBitmap.getWidth()/2 <= event.getX() && p.x + mBitmap.getWidth()/2 >= event.getX() &&  p.y - mBitmap.getHeight()/2 <= event.getY() && p.y + mBitmap.getHeight()/2 >= event.getY()) {
                        new AlertDialog.Builder(ctx)
//                .setTitle("Title")
                                .setMessage(R.string.delte_confirm)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        // yasir we need to properly deallocate resources (fixme)
                                        entities.remove(tempSelectedEntity);
                                        invalidate();

//                                        deletedSelectedEntity(tempSelectedEntity);
                                    }})
                                .setNegativeButton(R.string.no, null).show();
                    }
                }
            }

            if (scaleGestureDetector != null) {
                if (!disableMovement) {
                    scaleGestureDetector.onTouchEvent(event);
//                rotateGestureDetector.onTouchEvent(event);
                    moveGestureDetector.onTouchEvent(event);
                }
                gestureDetectorCompat.onTouchEvent(event);
            }
            return true;
        }
    };

    public MotionEntity getSelectedEntity() {
        return selectedEntity;
    }

    public List<MotionEntity> getEntities() {
        return entities;
    }

    public void setMotionViewCallback(@Nullable MotionViewCallback callback) {
        this.motionViewCallback = callback;
    }

    public void addEntity(@Nullable MotionEntity entity) {
        if (entity != null) {
            entities.add(entity);
            selectEntity(entity, false);
        }
    }

    public void addEntityAndPosition(@Nullable MotionEntity entity) {
        if (entity != null) {
            initEntityBorder(entity);
            initialTranslateAndScale(entity);
            entities.add(entity);
            selectEntity(entity, true);
        }
    }

    private void initEntityBorder(@NonNull MotionEntity entity ) {
        // init stroke
        int strokeSize = getResources().getDimensionPixelSize(R.dimen.stroke_size);
        Paint borderPaint = new Paint();
        borderPaint.setStrokeWidth(strokeSize);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(ContextCompat.getColor(getContext(), R.color.stroke_color));



        entity.setBorderPaint(borderPaint);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // dispatch draw is called after child views is drawn.
        // the idea that is we draw background stickers, than child views (if any), and than selected item
        // to draw on top of child views - do it in dispatchDraw(Canvas)
        // to draw below that - do it in onDraw(Canvas)
        if (selectedEntity != null) {
            selectedEntity.draw(canvas, selectedLayerPaint);

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawAllEntities(canvas);
        super.onDraw(canvas);
    }

    /**
     * draws all entities on the canvas
     * @param canvas Canvas where to draw all entities
     */
    private void drawAllEntities(Canvas canvas) {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).draw(canvas, null);
        }
    }

    /**
     * as a side effect - the method deselects Entity (if any selected)
     * @return bitmap with all the Entities at their current positions
     */
    public Bitmap getThumbnailImage() {
        selectEntity(null, false);

        Bitmap bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        // IMPORTANT: always create white background, cos if the image is saved in JPEG format,
        // which doesn't have transparent pixels, the background will be black
        bmp.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bmp);
        drawAllEntities(canvas);

        return bmp;
    }

    private void updateUI() {
        invalidate();
    }

    private void handleTranslate(PointF delta) {
        if (selectedEntity != null) {
            float newCenterX = selectedEntity.absoluteCenterX() + delta.x;
            float newCenterY = selectedEntity.absoluteCenterY() + delta.y;
            // limit entity center to screen bounds
            boolean needUpdateUI = false;
            if (newCenterX >= 0 && newCenterX <= getWidth()) {
                selectedEntity.getLayer().postTranslate(delta.x / getWidth(), 0.0F);
                needUpdateUI = true;
            }
            if (newCenterY >= 0 && newCenterY <= getHeight()) {
                selectedEntity.getLayer().postTranslate(0.0F, delta.y / getHeight());
                needUpdateUI = true;
            }
            if (needUpdateUI) {
                updateUI();
            }
        }
    }

    private void initialTranslateAndScale(@NonNull MotionEntity entity) {
        if (entity.getX() == null) {
            entity.moveToCanvasCenter();
        } else {
            entity.moveToCoordinates();
        }
        entity.getLayer().setScale((float) entity.getWidth()/entity.getCavasWidth());
    }

    private void selectEntity(@Nullable MotionEntity entity, boolean updateCallback) {

        if (selectedEntity != null) {
            selectedEntity.setIsSelected(false);
        }

        if (entity != null) {
            entity.setIsSelected(true);
        }
        selectedEntity = entity;
        invalidate();
        if (updateCallback && motionViewCallback != null) {
            motionViewCallback.onEntitySelected(entity);
        }
    }

    public void unselectEntity() {
        if (selectedEntity != null) {
            selectEntity(null, true);
        }
    }

    @Nullable
    private MotionEntity findEntityAtPoint(float x, float y) {
        MotionEntity selected = null;
        PointF p = new PointF(x, y);
        for (int i = entities.size() - 1; i >= 0; i--) {
            if (entities.get(i).pointInLayerRect(p)) {
                selected = entities.get(i);
                break;
            }
        }
        return selected;
    }

    private void updateSelectionOnTap(MotionEvent e) {
        MotionEntity entity = findEntityAtPoint(e.getX(), e.getY());
        selectEntity(entity, true);
    }

    private void updateOnLongPress(MotionEvent e) {
        // if layer is currently selected and point inside layer - move it to front
        if (selectedEntity != null) {
            PointF p = new PointF(e.getX(), e.getY());
            if (selectedEntity.pointInLayerRect(p)) {
                bringLayerToFront(selectedEntity);
            }
        }
    }

    private void bringLayerToFront(@NonNull MotionEntity entity) {
        // removing and adding brings layer to front
        if (entities.remove(entity)) {
            entities.add(entity);
            invalidate();
        }
    }

    private void moveEntityToBack(@Nullable MotionEntity entity) {
        if (entity == null) {
            return;
        }
        if (entities.remove(entity)) {
            entities.add(0, entity);
            invalidate();
        }
    }

    public void flipSelectedEntity() {
        if (selectedEntity == null) {
            return;
        }
        selectedEntity.getLayer().flip();
        invalidate();
    }

    public void moveSelectedBack() {
        moveEntityToBack(selectedEntity);
    }

    private void init(@NonNull Context context) {
        // I fucking love Android
        setWillNotDraw(false);

        this.ctx = context;

        selectedLayerPaint = new Paint();
        selectedLayerPaint.setAlpha((int) (255 * Constants.SELECTED_LAYER_ALPHA));
        selectedLayerPaint.setAntiAlias(true);

        // init listeners
        this.scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
//        this.rotateGestureDetector = new RotateGestureDetector(context, new RotateListener());
        this.moveGestureDetector = new MoveGestureDetector(context, new MoveListener());
        this.gestureDetectorCompat = new GestureDetectorCompat(context, new TapsListener());

        setOnTouchListener(onTouchListener);

        updateUI();
    }

    // memory
    public void release() {
        for (MotionEntity entity : entities) {
            entity.release();
        }
    }

    // gesture detectors

    public void deletedSelectedEntity(MotionEntity mSelectedEntity) {

        if (mSelectedEntity == null) {
            return;
        }
        if (entities.remove(mSelectedEntity)) {
            mSelectedEntity.release();
            mSelectedEntity = null;
            invalidate();
        }
    }

    private class TapsListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (motionViewCallback != null && selectedEntity != null) {
                motionViewCallback.onEntityDoubleTap(selectedEntity);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            updateOnLongPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            updateSelectionOnTap(e);
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (selectedEntity != null) {
                float scaleFactorDiff = detector.getScaleFactor();
                selectedEntity.getLayer().postScale(scaleFactorDiff - 1.0F);
                updateUI();
            }
            return true;
        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            if (selectedEntity != null) {
                selectedEntity.getLayer().postRotate(-detector.getRotationDegreesDelta());
                updateUI();
            }
            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            handleTranslate(detector.getFocusDelta());
            return true;
        }
    }
}
