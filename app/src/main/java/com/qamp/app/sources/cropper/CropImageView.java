package com.qamp.app.sources.cropper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.exifinterface.media.ExifInterface;

import com.qamp.app.R;

import java.lang.ref.WeakReference;
import java.util.UUID;


public class CropImageView extends FrameLayout {
    private CropImageAnimation mAnimation;
    private boolean mAutoZoomEnabled;
    private Bitmap mBitmap;
    private WeakReference<BitmapCroppingWorkerTask> mBitmapCroppingWorkerTask;
    private WeakReference<BitmapLoadingWorkerTask> mBitmapLoadingWorkerTask;
    private final CropOverlayView mCropOverlayView;
    private int mDegreesRotated;
    private boolean mFlipHorizontally;
    private boolean mFlipVertically;
    private final Matrix mImageInverseMatrix;
    private final Matrix mImageMatrix;
    private final float[] mImagePoints;
    private int mImageResource;
    private final ImageView mImageView;
    private int mInitialDegreesRotated;
    private int mLayoutHeight;
    private int mLayoutWidth;
    private Uri mLoadedImageUri;
    private int mLoadedSampleSize;
    private int mMaxZoom;
    private OnCropImageCompleteListener mOnCropImageCompleteListener;
    /* access modifiers changed from: private */
    public OnSetCropOverlayReleasedListener mOnCropOverlayReleasedListener;
    /* access modifiers changed from: private */
    public OnSetCropOverlayMovedListener mOnSetCropOverlayMovedListener;
    private OnSetCropWindowChangeListener mOnSetCropWindowChangeListener;
    private OnSetImageUriCompleteListener mOnSetImageUriCompleteListener;
    private final ProgressBar mProgressBar;
    private RectF mRestoreCropWindowRect;
    private int mRestoreDegreesRotated;
    private boolean mSaveBitmapToInstanceState;
    private Uri mSaveInstanceStateBitmapUri;
    private final float[] mScaleImagePoints;
    private ScaleType mScaleType;
    private boolean mShowCropOverlay;
    private boolean mShowProgressBar;
    private boolean mSizeChanged;
    private float mZoom;
    private float mZoomOffsetX;
    private float mZoomOffsetY;

    public enum CropShape {
        RECTANGLE,
        OVAL
    }

    public enum Guidelines {
        OFF,
        ON_TOUCH,
        ON
    }

    public interface OnCropImageCompleteListener {
        void onCropImageComplete(CropImageView cropImageView, CropResult cropResult);
    }

    public interface OnSetCropOverlayMovedListener {
        void onCropOverlayMoved(Rect rect);
    }

    public interface OnSetCropOverlayReleasedListener {
        void onCropOverlayReleased(Rect rect);
    }

    public interface OnSetCropWindowChangeListener {
        void onCropWindowChanged();
    }

    public interface OnSetImageUriCompleteListener {
        void onSetImageUriComplete(CropImageView cropImageView, Uri uri, Exception exc);
    }

    public enum RequestSizeOptions {
        NONE,
        SAMPLING,
        RESIZE_INSIDE,
        RESIZE_FIT,
        RESIZE_EXACT
    }

    public enum ScaleType {
        FIT_CENTER,
        CENTER,
        CENTER_CROP,
        CENTER_INSIDE
    }

    public CropImageView(Context context) {
        this(context, (AttributeSet) null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Bundle bundle;
        this.mImageMatrix = new Matrix();
        this.mImageInverseMatrix = new Matrix();
        this.mImagePoints = new float[8];
        this.mScaleImagePoints = new float[8];
        this.mSaveBitmapToInstanceState = false;
        this.mShowCropOverlay = true;
        this.mShowProgressBar = true;
        this.mAutoZoomEnabled = true;
        this.mLoadedSampleSize = 1;
        this.mZoom = 1.0f;
        CropImageOptions options = null;
        Intent intent = context instanceof Activity ? ((Activity) context).getIntent() : null;
        if (!(intent == null || (bundle = intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)) == null)) {
            options = (CropImageOptions) bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS);
        }
        if (options == null) {
            options = new CropImageOptions();
            if (attrs != null) {
                TypedArray ta = context.obtainStyledAttributes(attrs, com.mesibo.messaging.R.styleable.CropImageView, 0, 0);
                try {
                    options.fixAspectRatio = ta.getBoolean(com.mesibo.messaging.R.styleable.CropImageView_cropFixAspectRatio, options.fixAspectRatio);
                    options.aspectRatioX = ta.getInteger(com.mesibo.messaging.R.styleable.CropImageView_cropAspectRatioX, options.aspectRatioX);
                    options.aspectRatioY = ta.getInteger(com.mesibo.messaging.R.styleable.CropImageView_cropAspectRatioY, options.aspectRatioY);
                    options.scaleType = ScaleType.values()[ta.getInt(com.mesibo.messaging.R.styleable.CropImageView_cropScaleType, options.scaleType.ordinal())];
                    options.autoZoomEnabled = ta.getBoolean(com.mesibo.messaging.R.styleable.CropImageView_cropAutoZoomEnabled, options.autoZoomEnabled);
                    options.multiTouchEnabled = ta.getBoolean(com.mesibo.messaging.R.styleable.CropImageView_cropMultiTouchEnabled, options.multiTouchEnabled);
                    options.maxZoom = ta.getInteger(com.mesibo.messaging.R.styleable.CropImageView_cropMaxZoom, options.maxZoom);
                    options.cropShape = CropShape.values()[ta.getInt(com.mesibo.messaging.R.styleable.CropImageView_cropShape, options.cropShape.ordinal())];
                    options.guidelines = Guidelines.values()[ta.getInt(com.mesibo.messaging.R.styleable.CropImageView_cropGuidelines, options.guidelines.ordinal())];
                    options.snapRadius = ta.getDimension(com.mesibo.messaging.R.styleable.CropImageView_cropSnapRadius, options.snapRadius);
                    options.touchRadius = ta.getDimension(com.mesibo.messaging.R.styleable.CropImageView_cropTouchRadius, options.touchRadius);
                    options.initialCropWindowPaddingRatio = ta.getFloat(com.mesibo.messaging.R.styleable.CropImageView_cropInitialCropWindowPaddingRatio, options.initialCropWindowPaddingRatio);
                    options.borderLineThickness = ta.getDimension(com.mesibo.messaging.R.styleable.CropImageView_cropBorderLineThickness, options.borderLineThickness);
                    options.borderLineColor = ta.getInteger(com.mesibo.messaging.R.styleable.CropImageView_cropBorderLineColor, options.borderLineColor);
                    options.borderCornerThickness = ta.getDimension(com.mesibo.messaging.R.styleable.CropImageView_cropBorderCornerThickness, options.borderCornerThickness);
                    options.borderCornerOffset = ta.getDimension(com.mesibo.messaging.R.styleable.CropImageView_cropBorderCornerOffset, options.borderCornerOffset);
                    options.borderCornerLength = ta.getDimension(com.mesibo.messaging.R.styleable.CropImageView_cropBorderCornerLength, options.borderCornerLength);
                    options.borderCornerColor = ta.getInteger(com.mesibo.messaging.R.styleable.CropImageView_cropBorderCornerColor, options.borderCornerColor);
                    options.guidelinesThickness = ta.getDimension(com.mesibo.messaging.R.styleable.CropImageView_cropGuidelinesThickness, options.guidelinesThickness);
                    options.guidelinesColor = ta.getInteger(com.mesibo.messaging.R.styleable.CropImageView_cropGuidelinesColor, options.guidelinesColor);
                    options.backgroundColor = ta.getInteger(com.mesibo.messaging.R.styleable.CropImageView_cropBackgroundColor, options.backgroundColor);
                    options.showCropOverlay = ta.getBoolean(com.mesibo.messaging.R.styleable.CropImageView_cropShowCropOverlay, this.mShowCropOverlay);
                    options.showProgressBar = ta.getBoolean(com.mesibo.messaging.R.styleable.CropImageView_cropShowProgressBar, this.mShowProgressBar);
                    options.borderCornerThickness = ta.getDimension(com.mesibo.messaging.R.styleable.CropImageView_cropBorderCornerThickness, options.borderCornerThickness);
                    options.minCropWindowWidth = (int) ta.getDimension(com.mesibo.messaging.R.styleable.CropImageView_cropMinCropWindowWidth, (float) options.minCropWindowWidth);
                    options.minCropWindowHeight = (int) ta.getDimension(com.mesibo.messaging.R.styleable.CropImageView_cropMinCropWindowHeight, (float) options.minCropWindowHeight);
                    options.minCropResultWidth = (int) ta.getFloat(com.mesibo.messaging.R.styleable.CropImageView_cropMinCropResultWidthPX, (float) options.minCropResultWidth);
                    options.minCropResultHeight = (int) ta.getFloat(com.mesibo.messaging.R.styleable.CropImageView_cropMinCropResultHeightPX, (float) options.minCropResultHeight);
                    options.maxCropResultWidth = (int) ta.getFloat(com.mesibo.messaging.R.styleable.CropImageView_cropMaxCropResultWidthPX, (float) options.maxCropResultWidth);
                    options.maxCropResultHeight = (int) ta.getFloat(com.mesibo.messaging.R.styleable.CropImageView_cropMaxCropResultHeightPX, (float) options.maxCropResultHeight);
                    options.flipHorizontally = ta.getBoolean(com.mesibo.messaging.R.styleable.CropImageView_cropFlipHorizontally, options.flipHorizontally);
                    options.flipVertically = ta.getBoolean(com.mesibo.messaging.R.styleable.CropImageView_cropFlipHorizontally, options.flipVertically);
                    this.mSaveBitmapToInstanceState = ta.getBoolean(com.mesibo.messaging.R.styleable.CropImageView_cropSaveBitmapToInstanceState, this.mSaveBitmapToInstanceState);
                    if (ta.hasValue(com.mesibo.messaging.R.styleable.CropImageView_cropAspectRatioX) && ta.hasValue(com.mesibo.messaging.R.styleable.CropImageView_cropAspectRatioX) && !ta.hasValue(com.mesibo.messaging.R.styleable.CropImageView_cropFixAspectRatio)) {
                        options.fixAspectRatio = true;
                    }
                } finally {
                    ta.recycle();
                }
            }
        }
        options.validate();
        this.mScaleType = options.scaleType;
        this.mAutoZoomEnabled = options.autoZoomEnabled;
        this.mMaxZoom = options.maxZoom;
        this.mShowCropOverlay = options.showCropOverlay;
        this.mShowProgressBar = options.showProgressBar;
        this.mFlipHorizontally = options.flipHorizontally;
        this.mFlipVertically = options.flipVertically;
        View v = LayoutInflater.from(context).inflate(R.layout.crop_image_view, this, true);
        this.mImageView = (ImageView) v.findViewById(R.id.ImageView_image);
        this.mImageView.setScaleType(ImageView.ScaleType.MATRIX);
        this.mCropOverlayView = (CropOverlayView) v.findViewById(R.id.CropOverlayView);
        this.mCropOverlayView.setCropWindowChangeListener(new CropOverlayView.CropWindowChangeListener() {
            public void onCropWindowChanged(boolean inProgress) {
                CropImageView.this.handleCropWindowChanged(inProgress, true);
                OnSetCropOverlayReleasedListener listener = CropImageView.this.mOnCropOverlayReleasedListener;
                if (listener != null && !inProgress) {
                    listener.onCropOverlayReleased(CropImageView.this.getCropRect());
                }
                OnSetCropOverlayMovedListener movedListener = CropImageView.this.mOnSetCropOverlayMovedListener;
                if (movedListener != null && inProgress) {
                    movedListener.onCropOverlayMoved(CropImageView.this.getCropRect());
                }
            }
        });
        this.mCropOverlayView.setInitialAttributeValues(options);
        this.mProgressBar = (ProgressBar) v.findViewById(R.id.CropProgressBar);
        setProgressBarVisibility();
    }

    public ScaleType getScaleType() {
        return this.mScaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType != this.mScaleType) {
            this.mScaleType = scaleType;
            this.mZoom = 1.0f;
            this.mZoomOffsetY = 0.0f;
            this.mZoomOffsetX = 0.0f;
            this.mCropOverlayView.resetCropOverlayView();
            requestLayout();
        }
    }

    public CropShape getCropShape() {
        return this.mCropOverlayView.getCropShape();
    }

    public void setCropShape(CropShape cropShape) {
        this.mCropOverlayView.setCropShape(cropShape);
    }

    public boolean isAutoZoomEnabled() {
        return this.mAutoZoomEnabled;
    }

    public void setAutoZoomEnabled(boolean autoZoomEnabled) {
        if (this.mAutoZoomEnabled != autoZoomEnabled) {
            this.mAutoZoomEnabled = autoZoomEnabled;
            handleCropWindowChanged(false, false);
            this.mCropOverlayView.invalidate();
        }
    }

    public void setMultiTouchEnabled(boolean multiTouchEnabled) {
        if (this.mCropOverlayView.setMultiTouchEnabled(multiTouchEnabled)) {
            handleCropWindowChanged(false, false);
            this.mCropOverlayView.invalidate();
        }
    }

    public int getMaxZoom() {
        return this.mMaxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        if (this.mMaxZoom != maxZoom && maxZoom > 0) {
            this.mMaxZoom = maxZoom;
            handleCropWindowChanged(false, false);
            this.mCropOverlayView.invalidate();
        }
    }

    public void setMinCropResultSize(int minCropResultWidth, int minCropResultHeight) {
        this.mCropOverlayView.setMinCropResultSize(minCropResultWidth, minCropResultHeight);
    }

    public void setMaxCropResultSize(int maxCropResultWidth, int maxCropResultHeight) {
        this.mCropOverlayView.setMaxCropResultSize(maxCropResultWidth, maxCropResultHeight);
    }

    public int getRotatedDegrees() {
        return this.mDegreesRotated;
    }

    public void setRotatedDegrees(int degrees) {
        if (this.mDegreesRotated != degrees) {
            rotateImage(degrees - this.mDegreesRotated);
        }
    }

    public boolean isFixAspectRatio() {
        return this.mCropOverlayView.isFixAspectRatio();
    }

    public void setFixedAspectRatio(boolean fixAspectRatio) {
        this.mCropOverlayView.setFixedAspectRatio(fixAspectRatio);
    }

    public boolean isFlippedHorizontally() {
        return this.mFlipHorizontally;
    }

    public void setFlippedHorizontally(boolean flipHorizontally) {
        if (this.mFlipHorizontally != flipHorizontally) {
            this.mFlipHorizontally = flipHorizontally;
            applyImageMatrix((float) getWidth(), (float) getHeight(), true, false);
        }
    }

    public boolean isFlippedVertically() {
        return this.mFlipVertically;
    }

    public void setFlippedVertically(boolean flipVertically) {
        if (this.mFlipVertically != flipVertically) {
            this.mFlipVertically = flipVertically;
            applyImageMatrix((float) getWidth(), (float) getHeight(), true, false);
        }
    }

    public Guidelines getGuidelines() {
        return this.mCropOverlayView.getGuidelines();
    }

    public void setGuidelines(Guidelines guidelines) {
        this.mCropOverlayView.setGuidelines(guidelines);
    }

    public Pair<Integer, Integer> getAspectRatio() {
        return new Pair<>(Integer.valueOf(this.mCropOverlayView.getAspectRatioX()), Integer.valueOf(this.mCropOverlayView.getAspectRatioY()));
    }

    public void setAspectRatio(int aspectRatioX, int aspectRatioY) {
        this.mCropOverlayView.setAspectRatioX(aspectRatioX);
        this.mCropOverlayView.setAspectRatioY(aspectRatioY);
        setFixedAspectRatio(true);
    }

    public void clearAspectRatio() {
        this.mCropOverlayView.setAspectRatioX(1);
        this.mCropOverlayView.setAspectRatioY(1);
        setFixedAspectRatio(false);
    }

    public void setSnapRadius(float snapRadius) {
        if (snapRadius >= 0.0f) {
            this.mCropOverlayView.setSnapRadius(snapRadius);
        }
    }

    public boolean isShowProgressBar() {
        return this.mShowProgressBar;
    }

    public void setShowProgressBar(boolean showProgressBar) {
        if (this.mShowProgressBar != showProgressBar) {
            this.mShowProgressBar = showProgressBar;
            setProgressBarVisibility();
        }
    }

    public boolean isShowCropOverlay() {
        return this.mShowCropOverlay;
    }

    public void setShowCropOverlay(boolean showCropOverlay) {
        if (this.mShowCropOverlay != showCropOverlay) {
            this.mShowCropOverlay = showCropOverlay;
            setCropOverlayVisibility();
        }
    }

    public boolean isSaveBitmapToInstanceState() {
        return this.mSaveBitmapToInstanceState;
    }

    public void setSaveBitmapToInstanceState(boolean saveBitmapToInstanceState) {
        this.mSaveBitmapToInstanceState = saveBitmapToInstanceState;
    }

    public int getImageResource() {
        return this.mImageResource;
    }

    public Uri getImageUri() {
        return this.mLoadedImageUri;
    }

    public Rect getWholeImageRect() {
        int loadedSampleSize = this.mLoadedSampleSize;
        Bitmap bitmap = this.mBitmap;
        if (bitmap == null) {
            return null;
        }
        return new Rect(0, 0, bitmap.getWidth() * loadedSampleSize, bitmap.getHeight() * loadedSampleSize);
    }

    public Rect getCropRect() {
        int loadedSampleSize = this.mLoadedSampleSize;
        Bitmap bitmap = this.mBitmap;
        if (bitmap == null) {
            return null;
        }
        return BitmapUtils.getRectFromPoints(getCropPoints(), bitmap.getWidth() * loadedSampleSize, bitmap.getHeight() * loadedSampleSize, this.mCropOverlayView.isFixAspectRatio(), this.mCropOverlayView.getAspectRatioX(), this.mCropOverlayView.getAspectRatioY());
    }

    public RectF getCropWindowRect() {
        if (this.mCropOverlayView == null) {
            return null;
        }
        return this.mCropOverlayView.getCropWindowRect();
    }

    public float[] getCropPoints() {
        RectF cropWindowRect = this.mCropOverlayView.getCropWindowRect();
        float[] points = {cropWindowRect.left, cropWindowRect.top, cropWindowRect.right, cropWindowRect.top, cropWindowRect.right, cropWindowRect.bottom, cropWindowRect.left, cropWindowRect.bottom};
        this.mImageMatrix.invert(this.mImageInverseMatrix);
        this.mImageInverseMatrix.mapPoints(points);
        for (int i = 0; i < points.length; i++) {
            points[i] = points[i] * ((float) this.mLoadedSampleSize);
        }
        return points;
    }

    public void setCropRect(Rect rect) {
        this.mCropOverlayView.setInitialCropWindowRect(rect);
    }

    public void resetCropRect() {
        this.mZoom = 1.0f;
        this.mZoomOffsetX = 0.0f;
        this.mZoomOffsetY = 0.0f;
        this.mDegreesRotated = this.mInitialDegreesRotated;
        this.mFlipHorizontally = false;
        this.mFlipVertically = false;
        applyImageMatrix((float) getWidth(), (float) getHeight(), false, false);
        this.mCropOverlayView.resetCropWindowRect();
    }

    public Bitmap getCroppedImage() {
        return getCroppedImage(0, 0, RequestSizeOptions.NONE);
    }

    public Bitmap getCroppedImage(int reqWidth, int reqHeight) {
        return getCroppedImage(reqWidth, reqHeight, RequestSizeOptions.RESIZE_INSIDE);
    }

    public Bitmap getCroppedImage(int reqWidth, int reqHeight, RequestSizeOptions options) {
        Bitmap croppedBitmap;
        if (this.mBitmap == null) {
            return null;
        }
        this.mImageView.clearAnimation();
        if (options == RequestSizeOptions.NONE) {
            reqWidth = 0;
        }
        if (options == RequestSizeOptions.NONE) {
            reqHeight = 0;
        }
        if (this.mLoadedImageUri == null || (this.mLoadedSampleSize <= 1 && options != RequestSizeOptions.SAMPLING)) {
            croppedBitmap = BitmapUtils.cropBitmapObjectHandleOOM(this.mBitmap, getCropPoints(), this.mDegreesRotated, this.mCropOverlayView.isFixAspectRatio(), this.mCropOverlayView.getAspectRatioX(), this.mCropOverlayView.getAspectRatioY(), this.mFlipHorizontally, this.mFlipVertically).bitmap;
        } else {
            croppedBitmap = BitmapUtils.cropBitmap(getContext(), this.mLoadedImageUri, getCropPoints(), this.mDegreesRotated, this.mBitmap.getWidth() * this.mLoadedSampleSize, this.mBitmap.getHeight() * this.mLoadedSampleSize, this.mCropOverlayView.isFixAspectRatio(), this.mCropOverlayView.getAspectRatioX(), this.mCropOverlayView.getAspectRatioY(), reqWidth, reqHeight, this.mFlipHorizontally, this.mFlipVertically).bitmap;
        }
        return BitmapUtils.resizeBitmap(croppedBitmap, reqWidth, reqHeight, options);
    }

    public void getCroppedImageAsync() {
        getCroppedImageAsync(0, 0, RequestSizeOptions.NONE);
    }

    public void getCroppedImageAsync(int reqWidth, int reqHeight) {
        getCroppedImageAsync(reqWidth, reqHeight, RequestSizeOptions.RESIZE_INSIDE);
    }

    public void getCroppedImageAsync(int reqWidth, int reqHeight, RequestSizeOptions options) {
        if (this.mOnCropImageCompleteListener == null) {
            throw new IllegalArgumentException("mOnCropImageCompleteListener is not set");
        }
        startCropWorkerTask(reqWidth, reqHeight, options, (Uri) null, (Bitmap.CompressFormat) null, 0);
    }

    public void saveCroppedImageAsync(Uri saveUri) {
        saveCroppedImageAsync(saveUri, Bitmap.CompressFormat.JPEG, 90, 0, 0, RequestSizeOptions.NONE);
    }

    public void saveCroppedImageAsync(Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality) {
        saveCroppedImageAsync(saveUri, saveCompressFormat, saveCompressQuality, 0, 0, RequestSizeOptions.NONE);
    }

    public void saveCroppedImageAsync(Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality, int reqWidth, int reqHeight) {
        saveCroppedImageAsync(saveUri, saveCompressFormat, saveCompressQuality, reqWidth, reqHeight, RequestSizeOptions.RESIZE_INSIDE);
    }

    public void saveCroppedImageAsync(Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality, int reqWidth, int reqHeight, RequestSizeOptions options) {
        if (this.mOnCropImageCompleteListener == null) {
            throw new IllegalArgumentException("mOnCropImageCompleteListener is not set");
        }
        startCropWorkerTask(reqWidth, reqHeight, options, saveUri, saveCompressFormat, saveCompressQuality);
    }

    public void setOnSetCropOverlayReleasedListener(OnSetCropOverlayReleasedListener listener) {
        this.mOnCropOverlayReleasedListener = listener;
    }

    public void setOnSetCropOverlayMovedListener(OnSetCropOverlayMovedListener listener) {
        this.mOnSetCropOverlayMovedListener = listener;
    }

    public void setOnCropWindowChangedListener(OnSetCropWindowChangeListener listener) {
        this.mOnSetCropWindowChangeListener = listener;
    }

    public void setOnSetImageUriCompleteListener(OnSetImageUriCompleteListener listener) {
        this.mOnSetImageUriCompleteListener = listener;
    }

    public void setOnCropImageCompleteListener(OnCropImageCompleteListener listener) {
        this.mOnCropImageCompleteListener = listener;
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.mCropOverlayView.setInitialCropWindowRect((Rect) null);
        setBitmap(bitmap, 0, (Uri) null, 1, 0);
    }

    public void setImageBitmap(Bitmap bitmap, ExifInterface exif) {
        Bitmap setBitmap;
        int degreesRotated = 0;
        if (bitmap == null || exif == null) {
            setBitmap = bitmap;
        } else {
            BitmapUtils.RotateBitmapResult result = BitmapUtils.rotateBitmapByExif(bitmap, exif);
            setBitmap = result.bitmap;
            degreesRotated = result.degrees;
            this.mInitialDegreesRotated = result.degrees;
        }
        this.mCropOverlayView.setInitialCropWindowRect((Rect) null);
        setBitmap(setBitmap, 0, (Uri) null, 1, degreesRotated);
    }

    public void setImageResource(int resId) {
        if (resId != 0) {
            this.mCropOverlayView.setInitialCropWindowRect((Rect) null);
            setBitmap(BitmapFactory.decodeResource(getResources(), resId), resId, (Uri) null, 1, 0);
        }
    }

    public void setImageUriAsync(Uri uri) {
        if (uri != null) {
            BitmapLoadingWorkerTask currentTask = this.mBitmapLoadingWorkerTask != null ? (BitmapLoadingWorkerTask) this.mBitmapLoadingWorkerTask.get() : null;
            if (currentTask != null) {
                currentTask.cancel(true);
            }
            clearImageInt();
            this.mRestoreCropWindowRect = null;
            this.mRestoreDegreesRotated = 0;
            this.mCropOverlayView.setInitialCropWindowRect((Rect) null);
            this.mBitmapLoadingWorkerTask = new WeakReference<>(new BitmapLoadingWorkerTask(this, uri));
            ((BitmapLoadingWorkerTask) this.mBitmapLoadingWorkerTask.get()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            setProgressBarVisibility();
        }
    }

    public void clearImage() {
        clearImageInt();
        this.mCropOverlayView.setInitialCropWindowRect((Rect) null);
    }

    public void rotateImage(int degrees) {
        int degrees2;
        if (this.mBitmap != null) {
            if (degrees < 0) {
                degrees2 = (degrees % 360) + 360;
            } else {
                degrees2 = degrees % 360;
            }
            boolean flipAxes = !this.mCropOverlayView.isFixAspectRatio() && ((degrees2 > 45 && degrees2 < 135) || (degrees2 > 215 && degrees2 < 305));
            BitmapUtils.RECT.set(this.mCropOverlayView.getCropWindowRect());
            float halfWidth = (flipAxes ? BitmapUtils.RECT.height() : BitmapUtils.RECT.width()) / 2.0f;
            float halfHeight = (flipAxes ? BitmapUtils.RECT.width() : BitmapUtils.RECT.height()) / 2.0f;
            if (flipAxes) {
                boolean isFlippedHorizontally = this.mFlipHorizontally;
                this.mFlipHorizontally = this.mFlipVertically;
                this.mFlipVertically = isFlippedHorizontally;
            }
            this.mImageMatrix.invert(this.mImageInverseMatrix);
            BitmapUtils.POINTS[0] = BitmapUtils.RECT.centerX();
            BitmapUtils.POINTS[1] = BitmapUtils.RECT.centerY();
            BitmapUtils.POINTS[2] = 0.0f;
            BitmapUtils.POINTS[3] = 0.0f;
            BitmapUtils.POINTS[4] = 1.0f;
            BitmapUtils.POINTS[5] = 0.0f;
            this.mImageInverseMatrix.mapPoints(BitmapUtils.POINTS);
            this.mDegreesRotated = (this.mDegreesRotated + degrees2) % 360;
            applyImageMatrix((float) getWidth(), (float) getHeight(), true, false);
            this.mImageMatrix.mapPoints(BitmapUtils.POINTS2, BitmapUtils.POINTS);
            this.mZoom = (float) (((double) this.mZoom) / Math.sqrt(Math.pow((double) (BitmapUtils.POINTS2[4] - BitmapUtils.POINTS2[2]), 2.0d) + Math.pow((double) (BitmapUtils.POINTS2[5] - BitmapUtils.POINTS2[3]), 2.0d)));
            this.mZoom = Math.max(this.mZoom, 1.0f);
            applyImageMatrix((float) getWidth(), (float) getHeight(), true, false);
            this.mImageMatrix.mapPoints(BitmapUtils.POINTS2, BitmapUtils.POINTS);
            double change = Math.sqrt(Math.pow((double) (BitmapUtils.POINTS2[4] - BitmapUtils.POINTS2[2]), 2.0d) + Math.pow((double) (BitmapUtils.POINTS2[5] - BitmapUtils.POINTS2[3]), 2.0d));
            float halfWidth2 = (float) (((double) halfWidth) * change);
            float halfHeight2 = (float) (((double) halfHeight) * change);
            BitmapUtils.RECT.set(BitmapUtils.POINTS2[0] - halfWidth2, BitmapUtils.POINTS2[1] - halfHeight2, BitmapUtils.POINTS2[0] + halfWidth2, BitmapUtils.POINTS2[1] + halfHeight2);
            this.mCropOverlayView.resetCropOverlayView();
            this.mCropOverlayView.setCropWindowRect(BitmapUtils.RECT);
            applyImageMatrix((float) getWidth(), (float) getHeight(), true, false);
            handleCropWindowChanged(false, false);
            this.mCropOverlayView.fixCurrentCropWindowRect();
        }
    }

    public void flipImageHorizontally() {
        boolean z;
        if (!this.mFlipHorizontally) {
            z = true;
        } else {
            z = false;
        }
        this.mFlipHorizontally = z;
        applyImageMatrix((float) getWidth(), (float) getHeight(), true, false);
    }

    public void flipImageVertically() {
        boolean z;
        if (!this.mFlipVertically) {
            z = true;
        } else {
            z = false;
        }
        this.mFlipVertically = z;
        applyImageMatrix((float) getWidth(), (float) getHeight(), true, false);
    }

    /* access modifiers changed from: package-private */
    public void onSetImageUriAsyncComplete(BitmapLoadingWorkerTask.Result result) {
        this.mBitmapLoadingWorkerTask = null;
        setProgressBarVisibility();
        if (result.error == null) {
            this.mInitialDegreesRotated = result.degreesRotated;
            setBitmap(result.bitmap, 0, result.uri, result.loadSampleSize, result.degreesRotated);
        }
        OnSetImageUriCompleteListener listener = this.mOnSetImageUriCompleteListener;
        if (listener != null) {
            listener.onSetImageUriComplete(this, result.uri, result.error);
        }
    }

    /* access modifiers changed from: package-private */
    public void onImageCroppingAsyncComplete(BitmapCroppingWorkerTask.Result result) {
        this.mBitmapCroppingWorkerTask = null;
        setProgressBarVisibility();
        OnCropImageCompleteListener listener = this.mOnCropImageCompleteListener;
        if (listener != null) {
            listener.onCropImageComplete(this, new CropResult(this.mBitmap, this.mLoadedImageUri, result.bitmap, result.uri, result.error, getCropPoints(), getCropRect(), getWholeImageRect(), getRotatedDegrees(), result.sampleSize));
        }
    }

    private void setBitmap(Bitmap bitmap, int imageResource, Uri imageUri, int loadSampleSize, int degreesRotated) {
        if (this.mBitmap == null || !this.mBitmap.equals(bitmap)) {
            this.mImageView.clearAnimation();
            clearImageInt();
            this.mBitmap = bitmap;
            this.mImageView.setImageBitmap(this.mBitmap);
            this.mLoadedImageUri = imageUri;
            this.mImageResource = imageResource;
            this.mLoadedSampleSize = loadSampleSize;
            this.mDegreesRotated = degreesRotated;
            applyImageMatrix((float) getWidth(), (float) getHeight(), true, false);
            if (this.mCropOverlayView != null) {
                this.mCropOverlayView.resetCropOverlayView();
                setCropOverlayVisibility();
            }
        }
    }

    private void clearImageInt() {
        if (this.mBitmap != null && (this.mImageResource > 0 || this.mLoadedImageUri != null)) {
            this.mBitmap.recycle();
        }
        this.mBitmap = null;
        this.mImageResource = 0;
        this.mLoadedImageUri = null;
        this.mLoadedSampleSize = 1;
        this.mDegreesRotated = 0;
        this.mZoom = 1.0f;
        this.mZoomOffsetX = 0.0f;
        this.mZoomOffsetY = 0.0f;
        this.mImageMatrix.reset();
        this.mSaveInstanceStateBitmapUri = null;
        this.mImageView.setImageBitmap((Bitmap) null);
        setCropOverlayVisibility();
    }

    public void startCropWorkerTask(int reqWidth, int reqHeight, RequestSizeOptions options, Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality) {
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null) {
            this.mImageView.clearAnimation();
            BitmapCroppingWorkerTask currentTask = this.mBitmapCroppingWorkerTask != null ? (BitmapCroppingWorkerTask) this.mBitmapCroppingWorkerTask.get() : null;
            if (currentTask != null) {
                currentTask.cancel(true);
            }
            if (options == RequestSizeOptions.NONE) {
                reqWidth = 0;
            }
            if (options == RequestSizeOptions.NONE) {
                reqHeight = 0;
            }
            int orgWidth = bitmap.getWidth() * this.mLoadedSampleSize;
            int orgHeight = bitmap.getHeight() * this.mLoadedSampleSize;
            if (this.mLoadedImageUri == null || (this.mLoadedSampleSize <= 1 && options != RequestSizeOptions.SAMPLING)) {
                this.mBitmapCroppingWorkerTask = new WeakReference<>(new BitmapCroppingWorkerTask(this, bitmap, getCropPoints(), this.mDegreesRotated, this.mCropOverlayView.isFixAspectRatio(), this.mCropOverlayView.getAspectRatioX(), this.mCropOverlayView.getAspectRatioY(), reqWidth, reqHeight, this.mFlipHorizontally, this.mFlipVertically, options, saveUri, saveCompressFormat, saveCompressQuality));
            } else {
                this.mBitmapCroppingWorkerTask = new WeakReference<>(new BitmapCroppingWorkerTask(this, this.mLoadedImageUri, getCropPoints(), this.mDegreesRotated, orgWidth, orgHeight, this.mCropOverlayView.isFixAspectRatio(), this.mCropOverlayView.getAspectRatioX(), this.mCropOverlayView.getAspectRatioY(), reqWidth, reqHeight, this.mFlipHorizontally, this.mFlipVertically, options, saveUri, saveCompressFormat, saveCompressQuality));
            }
            ((BitmapCroppingWorkerTask) this.mBitmapCroppingWorkerTask.get()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            setProgressBarVisibility();
        }
    }

    public Parcelable onSaveInstanceState() {
        BitmapLoadingWorkerTask task;
        if (this.mLoadedImageUri == null && this.mBitmap == null && this.mImageResource < 1) {
            return super.onSaveInstanceState();
        }
        Bundle bundle = new Bundle();
        Uri imageUri = this.mLoadedImageUri;
        if (this.mSaveBitmapToInstanceState && imageUri == null && this.mImageResource < 1) {
            imageUri = BitmapUtils.writeTempStateStoreBitmap(getContext(), this.mBitmap, this.mSaveInstanceStateBitmapUri);
            this.mSaveInstanceStateBitmapUri = imageUri;
        }
        if (!(imageUri == null || this.mBitmap == null)) {
            String key = UUID.randomUUID().toString();
            BitmapUtils.mStateBitmap = new Pair<>(key, new WeakReference(this.mBitmap));
            bundle.putString("LOADED_IMAGE_STATE_BITMAP_KEY", key);
        }
        if (!(this.mBitmapLoadingWorkerTask == null || (task = (BitmapLoadingWorkerTask) this.mBitmapLoadingWorkerTask.get()) == null)) {
            bundle.putParcelable("LOADING_IMAGE_URI", task.getUri());
        }
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putParcelable("LOADED_IMAGE_URI", imageUri);
        bundle.putInt("LOADED_IMAGE_RESOURCE", this.mImageResource);
        bundle.putInt("LOADED_SAMPLE_SIZE", this.mLoadedSampleSize);
        bundle.putInt("DEGREES_ROTATED", this.mDegreesRotated);
        bundle.putParcelable("INITIAL_CROP_RECT", this.mCropOverlayView.getInitialCropWindowRect());
        BitmapUtils.RECT.set(this.mCropOverlayView.getCropWindowRect());
        this.mImageMatrix.invert(this.mImageInverseMatrix);
        this.mImageInverseMatrix.mapRect(BitmapUtils.RECT);
        bundle.putParcelable("CROP_WINDOW_RECT", BitmapUtils.RECT);
        bundle.putString("CROP_SHAPE", this.mCropOverlayView.getCropShape().name());
        bundle.putBoolean("CROP_AUTO_ZOOM_ENABLED", this.mAutoZoomEnabled);
        bundle.putInt("CROP_MAX_ZOOM", this.mMaxZoom);
        bundle.putBoolean("CROP_FLIP_HORIZONTALLY", this.mFlipHorizontally);
        bundle.putBoolean("CROP_FLIP_VERTICALLY", this.mFlipVertically);
        return bundle;
    }

    public void onRestoreInstanceState(Parcelable state) {
        Bitmap stateBitmap;
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            if (this.mBitmapLoadingWorkerTask == null && this.mLoadedImageUri == null && this.mBitmap == null && this.mImageResource == 0) {
                Uri uri = (Uri) bundle.getParcelable("LOADED_IMAGE_URI");
                if (uri != null) {
                    String key = bundle.getString("LOADED_IMAGE_STATE_BITMAP_KEY");
                    if (key != null) {
                        if (BitmapUtils.mStateBitmap == null || !((String) BitmapUtils.mStateBitmap.first).equals(key)) {
                            stateBitmap = null;
                        } else {
                            stateBitmap = (Bitmap) ((WeakReference) BitmapUtils.mStateBitmap.second).get();
                        }
                        BitmapUtils.mStateBitmap = null;
                        if (stateBitmap != null && !stateBitmap.isRecycled()) {
                            setBitmap(stateBitmap, 0, uri, bundle.getInt("LOADED_SAMPLE_SIZE"), 0);
                        }
                    }
                    if (this.mLoadedImageUri == null) {
                        setImageUriAsync(uri);
                    }
                } else {
                    int resId = bundle.getInt("LOADED_IMAGE_RESOURCE");
                    if (resId > 0) {
                        setImageResource(resId);
                    } else {
                        Uri uri2 = (Uri) bundle.getParcelable("LOADING_IMAGE_URI");
                        if (uri2 != null) {
                            setImageUriAsync(uri2);
                        }
                    }
                }
                int i = bundle.getInt("DEGREES_ROTATED");
                this.mRestoreDegreesRotated = i;
                this.mDegreesRotated = i;
                Rect initialCropRect = (Rect) bundle.getParcelable("INITIAL_CROP_RECT");
                if (initialCropRect != null && (initialCropRect.width() > 0 || initialCropRect.height() > 0)) {
                    this.mCropOverlayView.setInitialCropWindowRect(initialCropRect);
                }
                RectF cropWindowRect = (RectF) bundle.getParcelable("CROP_WINDOW_RECT");
                if (cropWindowRect != null && (cropWindowRect.width() > 0.0f || cropWindowRect.height() > 0.0f)) {
                    this.mRestoreCropWindowRect = cropWindowRect;
                }
                this.mCropOverlayView.setCropShape(CropShape.valueOf(bundle.getString("CROP_SHAPE")));
                this.mAutoZoomEnabled = bundle.getBoolean("CROP_AUTO_ZOOM_ENABLED");
                this.mMaxZoom = bundle.getInt("CROP_MAX_ZOOM");
                this.mFlipHorizontally = bundle.getBoolean("CROP_FLIP_HORIZONTALLY");
                this.mFlipVertically = bundle.getBoolean("CROP_FLIP_VERTICALLY");
            }
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth;
        int desiredHeight;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (this.mBitmap != null) {
            if (heightSize == 0) {
                heightSize = this.mBitmap.getHeight();
            }
            double viewToBitmapWidthRatio = Double.POSITIVE_INFINITY;
            double viewToBitmapHeightRatio = Double.POSITIVE_INFINITY;
            if (widthSize < this.mBitmap.getWidth()) {
                viewToBitmapWidthRatio = ((double) widthSize) / ((double) this.mBitmap.getWidth());
            }
            if (heightSize < this.mBitmap.getHeight()) {
                viewToBitmapHeightRatio = ((double) heightSize) / ((double) this.mBitmap.getHeight());
            }
            if (viewToBitmapWidthRatio == Double.POSITIVE_INFINITY && viewToBitmapHeightRatio == Double.POSITIVE_INFINITY) {
                desiredWidth = this.mBitmap.getWidth();
                desiredHeight = this.mBitmap.getHeight();
            } else if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                desiredWidth = widthSize;
                desiredHeight = (int) (((double) this.mBitmap.getHeight()) * viewToBitmapWidthRatio);
            } else {
                desiredHeight = heightSize;
                desiredWidth = (int) (((double) this.mBitmap.getWidth()) * viewToBitmapHeightRatio);
            }
            int width = getOnMeasureSpec(widthMode, widthSize, desiredWidth);
            int height = getOnMeasureSpec(heightMode, heightSize, desiredHeight);
            this.mLayoutWidth = width;
            this.mLayoutHeight = height;
            setMeasuredDimension(this.mLayoutWidth, this.mLayoutHeight);
            return;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mLayoutWidth <= 0 || this.mLayoutHeight <= 0) {
            updateImageBounds(true);
            return;
        }
        ViewGroup.LayoutParams origParams = getLayoutParams();
        origParams.width = this.mLayoutWidth;
        origParams.height = this.mLayoutHeight;
        setLayoutParams(origParams);
        if (this.mBitmap != null) {
            applyImageMatrix((float) (r - l), (float) (b - t), true, false);
            if (this.mRestoreCropWindowRect != null) {
                if (this.mRestoreDegreesRotated != this.mInitialDegreesRotated) {
                    this.mDegreesRotated = this.mRestoreDegreesRotated;
                    applyImageMatrix((float) (r - l), (float) (b - t), true, false);
                }
                this.mImageMatrix.mapRect(this.mRestoreCropWindowRect);
                this.mCropOverlayView.setCropWindowRect(this.mRestoreCropWindowRect);
                handleCropWindowChanged(false, false);
                this.mCropOverlayView.fixCurrentCropWindowRect();
                this.mRestoreCropWindowRect = null;
            } else if (this.mSizeChanged) {
                this.mSizeChanged = false;
                handleCropWindowChanged(false, false);
            }
        } else {
            updateImageBounds(true);
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mSizeChanged = oldw > 0 && oldh > 0;
    }

    /* access modifiers changed from: private */
    public void handleCropWindowChanged(boolean inProgress, boolean animate) {
        int width = getWidth();
        int height = getHeight();
        if (this.mBitmap != null && width > 0 && height > 0) {
            RectF cropRect = this.mCropOverlayView.getCropWindowRect();
            if (inProgress) {
                if (cropRect.left < 0.0f || cropRect.top < 0.0f || cropRect.right > ((float) width) || cropRect.bottom > ((float) height)) {
                    applyImageMatrix((float) width, (float) height, false, false);
                }
            } else if (this.mAutoZoomEnabled || this.mZoom > 1.0f) {
                float newZoom = 0.0f;
                if (this.mZoom < ((float) this.mMaxZoom) && cropRect.width() < ((float) width) * 0.5f && cropRect.height() < ((float) height) * 0.5f) {
                    newZoom = Math.min((float) this.mMaxZoom, Math.min(((float) width) / ((cropRect.width() / this.mZoom) / 0.64f), ((float) height) / ((cropRect.height() / this.mZoom) / 0.64f)));
                }
                if (this.mZoom > 1.0f && (cropRect.width() > ((float) width) * 0.65f || cropRect.height() > ((float) height) * 0.65f)) {
                    newZoom = Math.max(1.0f, Math.min(((float) width) / ((cropRect.width() / this.mZoom) / 0.51f), ((float) height) / ((cropRect.height() / this.mZoom) / 0.51f)));
                }
                if (!this.mAutoZoomEnabled) {
                    newZoom = 1.0f;
                }
                if (newZoom > 0.0f && newZoom != this.mZoom) {
                    if (animate) {
                        if (this.mAnimation == null) {
                            this.mAnimation = new CropImageAnimation(this.mImageView, this.mCropOverlayView);
                        }
                        this.mAnimation.setStartState(this.mImagePoints, this.mImageMatrix);
                    }
                    this.mZoom = newZoom;
                    applyImageMatrix((float) width, (float) height, true, animate);
                }
            }
            if (this.mOnSetCropWindowChangeListener != null && !inProgress) {
                this.mOnSetCropWindowChangeListener.onCropWindowChanged();
            }
        }
    }

    private void applyImageMatrix(float width, float height, boolean center, boolean animate) {
        float max;
        float f = 0.0f;
        if (this.mBitmap != null && width > 0.0f && height > 0.0f) {
            this.mImageMatrix.invert(this.mImageInverseMatrix);
            RectF cropRect = this.mCropOverlayView.getCropWindowRect();
            this.mImageInverseMatrix.mapRect(cropRect);
            this.mImageMatrix.reset();
            this.mImageMatrix.postTranslate((width - ((float) this.mBitmap.getWidth())) / 2.0f, (height - ((float) this.mBitmap.getHeight())) / 2.0f);
            mapImagePointsByImageMatrix();
            if (this.mDegreesRotated > 0) {
                this.mImageMatrix.postRotate((float) this.mDegreesRotated, BitmapUtils.getRectCenterX(this.mImagePoints), BitmapUtils.getRectCenterY(this.mImagePoints));
                mapImagePointsByImageMatrix();
            }
            float scale = Math.min(width / BitmapUtils.getRectWidth(this.mImagePoints), height / BitmapUtils.getRectHeight(this.mImagePoints));
            if (this.mScaleType == ScaleType.FIT_CENTER || ((this.mScaleType == ScaleType.CENTER_INSIDE && scale < 1.0f) || (scale > 1.0f && this.mAutoZoomEnabled))) {
                this.mImageMatrix.postScale(scale, scale, BitmapUtils.getRectCenterX(this.mImagePoints), BitmapUtils.getRectCenterY(this.mImagePoints));
                mapImagePointsByImageMatrix();
            }
            float scaleX = this.mFlipHorizontally ? -this.mZoom : this.mZoom;
            float scaleY = this.mFlipVertically ? -this.mZoom : this.mZoom;
            this.mImageMatrix.postScale(scaleX, scaleY, BitmapUtils.getRectCenterX(this.mImagePoints), BitmapUtils.getRectCenterY(this.mImagePoints));
            mapImagePointsByImageMatrix();
            this.mImageMatrix.mapRect(cropRect);
            if (center) {
                if (width > BitmapUtils.getRectWidth(this.mImagePoints)) {
                    max = 0.0f;
                } else {
                    max = Math.max(Math.min((width / 2.0f) - cropRect.centerX(), -BitmapUtils.getRectLeft(this.mImagePoints)), ((float) getWidth()) - BitmapUtils.getRectRight(this.mImagePoints)) / scaleX;
                }
                this.mZoomOffsetX = max;
                if (height <= BitmapUtils.getRectHeight(this.mImagePoints)) {
                    f = Math.max(Math.min((height / 2.0f) - cropRect.centerY(), -BitmapUtils.getRectTop(this.mImagePoints)), ((float) getHeight()) - BitmapUtils.getRectBottom(this.mImagePoints)) / scaleY;
                }
                this.mZoomOffsetY = f;
            } else {
                this.mZoomOffsetX = Math.min(Math.max(this.mZoomOffsetX * scaleX, -cropRect.left), (-cropRect.right) + width) / scaleX;
                this.mZoomOffsetY = Math.min(Math.max(this.mZoomOffsetY * scaleY, -cropRect.top), (-cropRect.bottom) + height) / scaleY;
            }
            this.mImageMatrix.postTranslate(this.mZoomOffsetX * scaleX, this.mZoomOffsetY * scaleY);
            cropRect.offset(this.mZoomOffsetX * scaleX, this.mZoomOffsetY * scaleY);
            this.mCropOverlayView.setCropWindowRect(cropRect);
            mapImagePointsByImageMatrix();
            this.mCropOverlayView.invalidate();
            if (animate) {
                this.mAnimation.setEndState(this.mImagePoints, this.mImageMatrix);
                this.mImageView.startAnimation(this.mAnimation);
            } else {
                this.mImageView.setImageMatrix(this.mImageMatrix);
            }
            updateImageBounds(false);
        }
    }

    private void mapImagePointsByImageMatrix() {
        this.mImagePoints[0] = 0.0f;
        this.mImagePoints[1] = 0.0f;
        this.mImagePoints[2] = (float) this.mBitmap.getWidth();
        this.mImagePoints[3] = 0.0f;
        this.mImagePoints[4] = (float) this.mBitmap.getWidth();
        this.mImagePoints[5] = (float) this.mBitmap.getHeight();
        this.mImagePoints[6] = 0.0f;
        this.mImagePoints[7] = (float) this.mBitmap.getHeight();
        this.mImageMatrix.mapPoints(this.mImagePoints);
        this.mScaleImagePoints[0] = 0.0f;
        this.mScaleImagePoints[1] = 0.0f;
        this.mScaleImagePoints[2] = 100.0f;
        this.mScaleImagePoints[3] = 0.0f;
        this.mScaleImagePoints[4] = 100.0f;
        this.mScaleImagePoints[5] = 100.0f;
        this.mScaleImagePoints[6] = 0.0f;
        this.mScaleImagePoints[7] = 100.0f;
        this.mImageMatrix.mapPoints(this.mScaleImagePoints);
    }

    private static int getOnMeasureSpec(int measureSpecMode, int measureSpecSize, int desiredSize) {
        if (measureSpecMode == 1073741824) {
            return measureSpecSize;
        }
        if (measureSpecMode == Integer.MIN_VALUE) {
            return Math.min(desiredSize, measureSpecSize);
        }
        return desiredSize;
    }

    private void setCropOverlayVisibility() {
        if (this.mCropOverlayView != null) {
            this.mCropOverlayView.setVisibility((!this.mShowCropOverlay || this.mBitmap == null) ? 4 : 0);
        }
    }

    private void setProgressBarVisibility() {
        boolean visible;
        int i = 0;
        if (!this.mShowProgressBar || ((this.mBitmap != null || this.mBitmapLoadingWorkerTask == null) && this.mBitmapCroppingWorkerTask == null)) {
            visible = false;
        } else {
            visible = true;
        }
        ProgressBar progressBar = this.mProgressBar;
        if (!visible) {
            i = 4;
        }
        progressBar.setVisibility(i);
    }

    private void updateImageBounds(boolean clear) {
        if (this.mBitmap != null && !clear) {
            this.mCropOverlayView.setCropWindowLimits((float) getWidth(), (float) getHeight(), (((float) this.mLoadedSampleSize) * 100.0f) / BitmapUtils.getRectWidth(this.mScaleImagePoints), (((float) this.mLoadedSampleSize) * 100.0f) / BitmapUtils.getRectHeight(this.mScaleImagePoints));
        }
        this.mCropOverlayView.setBounds(clear ? null : this.mImagePoints, getWidth(), getHeight());
    }

    public static class CropResult {
        private final Bitmap mBitmap;
        private final float[] mCropPoints;
        private final Rect mCropRect;
        private final Exception mError;
        private final Bitmap mOriginalBitmap;
        private final Uri mOriginalUri;
        private final int mRotation;
        private final int mSampleSize;
        private final Uri mUri;
        private final Rect mWholeImageRect;

        CropResult(Bitmap originalBitmap, Uri originalUri, Bitmap bitmap, Uri uri, Exception error, float[] cropPoints, Rect cropRect, Rect wholeImageRect, int rotation, int sampleSize) {
            this.mOriginalBitmap = originalBitmap;
            this.mOriginalUri = originalUri;
            this.mBitmap = bitmap;
            this.mUri = uri;
            this.mError = error;
            this.mCropPoints = cropPoints;
            this.mCropRect = cropRect;
            this.mWholeImageRect = wholeImageRect;
            this.mRotation = rotation;
            this.mSampleSize = sampleSize;
        }

        public Bitmap getOriginalBitmap() {
            return this.mOriginalBitmap;
        }

        public Uri getOriginalUri() {
            return this.mOriginalUri;
        }

        public boolean isSuccessful() {
            return this.mError == null;
        }

        public Bitmap getBitmap() {
            return this.mBitmap;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public Exception getError() {
            return this.mError;
        }

        public float[] getCropPoints() {
            return this.mCropPoints;
        }

        public Rect getCropRect() {
            return this.mCropRect;
        }

        public Rect getWholeImageRect() {
            return this.mWholeImageRect;
        }

        public int getRotation() {
            return this.mRotation;
        }

        public int getSampleSize() {
            return this.mSampleSize;
        }
    }
}
