package com.qamp.app.sources.cropper;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

final class CropImageAnimation extends Animation implements Animation.AnimationListener {
    private final float[] mAnimMatrix = new float[9];
    private final float[] mAnimPoints = new float[8];
    private final RectF mAnimRect = new RectF();
    private final CropOverlayView mCropOverlayView;
    private final float[] mEndBoundPoints = new float[8];
    private final RectF mEndCropWindowRect = new RectF();
    private final float[] mEndImageMatrix = new float[9];
    private final ImageView mImageView;
    private final float[] mStartBoundPoints = new float[8];
    private final RectF mStartCropWindowRect = new RectF();
    private final float[] mStartImageMatrix = new float[9];

    public CropImageAnimation(ImageView cropImageView, CropOverlayView cropOverlayView) {
        this.mImageView = cropImageView;
        this.mCropOverlayView = cropOverlayView;
        setDuration(300);
        setFillAfter(true);
        setInterpolator(new AccelerateDecelerateInterpolator());
        setAnimationListener(this);
    }

    public void setStartState(float[] boundPoints, Matrix imageMatrix) {
        reset();
        System.arraycopy(boundPoints, 0, this.mStartBoundPoints, 0, 8);
        this.mStartCropWindowRect.set(this.mCropOverlayView.getCropWindowRect());
        imageMatrix.getValues(this.mStartImageMatrix);
    }

    public void setEndState(float[] boundPoints, Matrix imageMatrix) {
        System.arraycopy(boundPoints, 0, this.mEndBoundPoints, 0, 8);
        this.mEndCropWindowRect.set(this.mCropOverlayView.getCropWindowRect());
        imageMatrix.getValues(this.mEndImageMatrix);
    }

    /* access modifiers changed from: protected */
    public void applyTransformation(float interpolatedTime, Transformation t) {
        this.mAnimRect.left = this.mStartCropWindowRect.left + ((this.mEndCropWindowRect.left - this.mStartCropWindowRect.left) * interpolatedTime);
        this.mAnimRect.top = this.mStartCropWindowRect.top + ((this.mEndCropWindowRect.top - this.mStartCropWindowRect.top) * interpolatedTime);
        this.mAnimRect.right = this.mStartCropWindowRect.right + ((this.mEndCropWindowRect.right - this.mStartCropWindowRect.right) * interpolatedTime);
        this.mAnimRect.bottom = this.mStartCropWindowRect.bottom + ((this.mEndCropWindowRect.bottom - this.mStartCropWindowRect.bottom) * interpolatedTime);
        this.mCropOverlayView.setCropWindowRect(this.mAnimRect);
        for (int i = 0; i < this.mAnimPoints.length; i++) {
            this.mAnimPoints[i] = this.mStartBoundPoints[i] + ((this.mEndBoundPoints[i] - this.mStartBoundPoints[i]) * interpolatedTime);
        }
        this.mCropOverlayView.setBounds(this.mAnimPoints, this.mImageView.getWidth(), this.mImageView.getHeight());
        for (int i2 = 0; i2 < this.mAnimMatrix.length; i2++) {
            this.mAnimMatrix[i2] = this.mStartImageMatrix[i2] + ((this.mEndImageMatrix[i2] - this.mStartImageMatrix[i2]) * interpolatedTime);
        }
        Matrix m = this.mImageView.getImageMatrix();
        m.setValues(this.mAnimMatrix);
        this.mImageView.setImageMatrix(m);
        this.mImageView.invalidate();
        this.mCropOverlayView.invalidate();
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        this.mImageView.clearAnimation();
    }

    public void onAnimationRepeat(Animation animation) {
    }
}
