package com.qamp.app.sources.cropper;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.Arrays;

public class CropOverlayView extends View {
    private boolean initializedCropWindow;
    private int mAspectRatioX;
    private int mAspectRatioY;
    private Paint mBackgroundPaint;
    private float mBorderCornerLength;
    private float mBorderCornerOffset;
    private Paint mBorderCornerPaint;
    private Paint mBorderPaint;
    private final float[] mBoundsPoints;
    private final RectF mCalcBounds;
    private CropImageView.CropShape mCropShape;
    private CropWindowChangeListener mCropWindowChangeListener;
    /* access modifiers changed from: private */
    public final CropWindowHandler mCropWindowHandler;
    private final RectF mDrawRect;
    private boolean mFixAspectRatio;
    private Paint mGuidelinePaint;
    private CropImageView.Guidelines mGuidelines;
    private float mInitialCropWindowPaddingRatio;
    private final Rect mInitialCropWindowRect;
    private CropWindowMoveHandler mMoveHandler;
    private boolean mMultiTouchEnabled;
    private Integer mOriginalLayerType;
    private Path mPath;
    private ScaleGestureDetector mScaleDetector;
    private float mSnapRadius;
    private float mTargetAspectRatio;
    private float mTouchRadius;
    private int mViewHeight;
    private int mViewWidth;

    public interface CropWindowChangeListener {
        void onCropWindowChanged(boolean z);
    }

    public CropOverlayView(Context context) {
        this(context, (AttributeSet) null);
    }

    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCropWindowHandler = new CropWindowHandler();
        this.mDrawRect = new RectF();
        this.mPath = new Path();
        this.mBoundsPoints = new float[8];
        this.mCalcBounds = new RectF();
        this.mTargetAspectRatio = ((float) this.mAspectRatioX) / ((float) this.mAspectRatioY);
        this.mInitialCropWindowRect = new Rect();
    }

    public void setCropWindowChangeListener(CropWindowChangeListener listener) {
        this.mCropWindowChangeListener = listener;
    }

    public RectF getCropWindowRect() {
        return this.mCropWindowHandler.getRect();
    }

    public void setCropWindowRect(RectF rect) {
        this.mCropWindowHandler.setRect(rect);
    }

    public void fixCurrentCropWindowRect() {
        RectF rect = getCropWindowRect();
        fixCropWindowRectByRules(rect);
        this.mCropWindowHandler.setRect(rect);
    }

    public void setBounds(float[] boundsPoints, int viewWidth, int viewHeight) {
        if (boundsPoints == null || !Arrays.equals(this.mBoundsPoints, boundsPoints)) {
            if (boundsPoints == null) {
                Arrays.fill(this.mBoundsPoints, 0.0f);
            } else {
                System.arraycopy(boundsPoints, 0, this.mBoundsPoints, 0, boundsPoints.length);
            }
            this.mViewWidth = viewWidth;
            this.mViewHeight = viewHeight;
            RectF cropRect = this.mCropWindowHandler.getRect();
            if (cropRect.width() == 0.0f || cropRect.height() == 0.0f) {
                initCropWindow();
            }
        }
    }

    public void resetCropOverlayView() {
        if (this.initializedCropWindow) {
            setCropWindowRect(BitmapUtils.EMPTY_RECT_F);
            initCropWindow();
            invalidate();
        }
    }

    public CropImageView.CropShape getCropShape() {
        return this.mCropShape;
    }

    public void setCropShape(CropImageView.CropShape cropShape) {
        if (this.mCropShape != cropShape) {
            this.mCropShape = cropShape;
            if (Build.VERSION.SDK_INT <= 17) {
                if (this.mCropShape == CropImageView.CropShape.OVAL) {
                    this.mOriginalLayerType = Integer.valueOf(getLayerType());
                    if (this.mOriginalLayerType.intValue() != 1) {
                        setLayerType(1, (Paint) null);
                    } else {
                        this.mOriginalLayerType = null;
                    }
                } else if (this.mOriginalLayerType != null) {
                    setLayerType(this.mOriginalLayerType.intValue(), (Paint) null);
                    this.mOriginalLayerType = null;
                }
            }
            invalidate();
        }
    }

    public CropImageView.Guidelines getGuidelines() {
        return this.mGuidelines;
    }

    public void setGuidelines(CropImageView.Guidelines guidelines) {
        if (this.mGuidelines != guidelines) {
            this.mGuidelines = guidelines;
            if (this.initializedCropWindow) {
                invalidate();
            }
        }
    }

    public boolean isFixAspectRatio() {
        return this.mFixAspectRatio;
    }

    public void setFixedAspectRatio(boolean fixAspectRatio) {
        if (this.mFixAspectRatio != fixAspectRatio) {
            this.mFixAspectRatio = fixAspectRatio;
            if (this.initializedCropWindow) {
                initCropWindow();
                invalidate();
            }
        }
    }

    public int getAspectRatioX() {
        return this.mAspectRatioX;
    }

    public void setAspectRatioX(int aspectRatioX) {
        if (aspectRatioX <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        } else if (this.mAspectRatioX != aspectRatioX) {
            this.mAspectRatioX = aspectRatioX;
            this.mTargetAspectRatio = ((float) this.mAspectRatioX) / ((float) this.mAspectRatioY);
            if (this.initializedCropWindow) {
                initCropWindow();
                invalidate();
            }
        }
    }

    public int getAspectRatioY() {
        return this.mAspectRatioY;
    }

    public void setAspectRatioY(int aspectRatioY) {
        if (aspectRatioY <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        } else if (this.mAspectRatioY != aspectRatioY) {
            this.mAspectRatioY = aspectRatioY;
            this.mTargetAspectRatio = ((float) this.mAspectRatioX) / ((float) this.mAspectRatioY);
            if (this.initializedCropWindow) {
                initCropWindow();
                invalidate();
            }
        }
    }

    public void setSnapRadius(float snapRadius) {
        this.mSnapRadius = snapRadius;
    }

    public boolean setMultiTouchEnabled(boolean multiTouchEnabled) {
        if (this.mMultiTouchEnabled == multiTouchEnabled) {
            return false;
        }
        this.mMultiTouchEnabled = multiTouchEnabled;
        if (this.mMultiTouchEnabled && this.mScaleDetector == null) {
            this.mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        }
        return true;
    }

    public void setMinCropResultSize(int minCropResultWidth, int minCropResultHeight) {
        this.mCropWindowHandler.setMinCropResultSize(minCropResultWidth, minCropResultHeight);
    }

    public void setMaxCropResultSize(int maxCropResultWidth, int maxCropResultHeight) {
        this.mCropWindowHandler.setMaxCropResultSize(maxCropResultWidth, maxCropResultHeight);
    }

    public void setCropWindowLimits(float maxWidth, float maxHeight, float scaleFactorWidth, float scaleFactorHeight) {
        this.mCropWindowHandler.setCropWindowLimits(maxWidth, maxHeight, scaleFactorWidth, scaleFactorHeight);
    }

    public Rect getInitialCropWindowRect() {
        return this.mInitialCropWindowRect;
    }

    public void setInitialCropWindowRect(Rect rect) {
        Rect rect2 = this.mInitialCropWindowRect;
        if (rect == null) {
            rect = BitmapUtils.EMPTY_RECT;
        }
        rect2.set(rect);
        if (this.initializedCropWindow) {
            initCropWindow();
            invalidate();
            callOnCropWindowChanged(false);
        }
    }

    public void resetCropWindowRect() {
        if (this.initializedCropWindow) {
            initCropWindow();
            invalidate();
            callOnCropWindowChanged(false);
        }
    }

    public void setInitialAttributeValues(CropImageOptions options) {
        this.mCropWindowHandler.setInitialAttributeValues(options);
        setCropShape(options.cropShape);
        setSnapRadius(options.snapRadius);
        setGuidelines(options.guidelines);
        setFixedAspectRatio(options.fixAspectRatio);
        setAspectRatioX(options.aspectRatioX);
        setAspectRatioY(options.aspectRatioY);
        setMultiTouchEnabled(options.multiTouchEnabled);
        this.mTouchRadius = options.touchRadius;
        this.mInitialCropWindowPaddingRatio = options.initialCropWindowPaddingRatio;
        this.mBorderPaint = getNewPaintOrNull(options.borderLineThickness, options.borderLineColor);
        this.mBorderCornerOffset = options.borderCornerOffset;
        this.mBorderCornerLength = options.borderCornerLength;
        this.mBorderCornerPaint = getNewPaintOrNull(options.borderCornerThickness, options.borderCornerColor);
        this.mGuidelinePaint = getNewPaintOrNull(options.guidelinesThickness, options.guidelinesColor);
        this.mBackgroundPaint = getNewPaint(options.backgroundColor);
    }

    private void initCropWindow() {
        float leftLimit = Math.max(BitmapUtils.getRectLeft(this.mBoundsPoints), 0.0f);
        float topLimit = Math.max(BitmapUtils.getRectTop(this.mBoundsPoints), 0.0f);
        float rightLimit = Math.min(BitmapUtils.getRectRight(this.mBoundsPoints), (float) getWidth());
        float bottomLimit = Math.min(BitmapUtils.getRectBottom(this.mBoundsPoints), (float) getHeight());
        if (rightLimit > leftLimit && bottomLimit > topLimit) {
            RectF rect = new RectF();
            this.initializedCropWindow = true;
            float horizontalPadding = this.mInitialCropWindowPaddingRatio * (rightLimit - leftLimit);
            float verticalPadding = this.mInitialCropWindowPaddingRatio * (bottomLimit - topLimit);
            if (this.mInitialCropWindowRect.width() > 0 && this.mInitialCropWindowRect.height() > 0) {
                rect.left = (((float) this.mInitialCropWindowRect.left) / this.mCropWindowHandler.getScaleFactorWidth()) + leftLimit;
                rect.top = (((float) this.mInitialCropWindowRect.top) / this.mCropWindowHandler.getScaleFactorHeight()) + topLimit;
                rect.right = rect.left + (((float) this.mInitialCropWindowRect.width()) / this.mCropWindowHandler.getScaleFactorWidth());
                rect.bottom = rect.top + (((float) this.mInitialCropWindowRect.height()) / this.mCropWindowHandler.getScaleFactorHeight());
                rect.left = Math.max(leftLimit, rect.left);
                rect.top = Math.max(topLimit, rect.top);
                rect.right = Math.min(rightLimit, rect.right);
                rect.bottom = Math.min(bottomLimit, rect.bottom);
            } else if (!this.mFixAspectRatio || rightLimit <= leftLimit || bottomLimit <= topLimit) {
                rect.left = leftLimit + horizontalPadding;
                rect.top = topLimit + verticalPadding;
                rect.right = rightLimit - horizontalPadding;
                rect.bottom = bottomLimit - verticalPadding;
            } else if ((rightLimit - leftLimit) / (bottomLimit - topLimit) > this.mTargetAspectRatio) {
                rect.top = topLimit + verticalPadding;
                rect.bottom = bottomLimit - verticalPadding;
                float centerX = ((float) getWidth()) / 2.0f;
                this.mTargetAspectRatio = ((float) this.mAspectRatioX) / ((float) this.mAspectRatioY);
                float halfCropWidth = Math.max(this.mCropWindowHandler.getMinCropWidth(), rect.height() * this.mTargetAspectRatio) / 2.0f;
                rect.left = centerX - halfCropWidth;
                rect.right = centerX + halfCropWidth;
            } else {
                rect.left = leftLimit + horizontalPadding;
                rect.right = rightLimit - horizontalPadding;
                float centerY = ((float) getHeight()) / 2.0f;
                float halfCropHeight = Math.max(this.mCropWindowHandler.getMinCropHeight(), rect.width() / this.mTargetAspectRatio) / 2.0f;
                rect.top = centerY - halfCropHeight;
                rect.bottom = centerY + halfCropHeight;
            }
            fixCropWindowRectByRules(rect);
            this.mCropWindowHandler.setRect(rect);
        }
    }

    private void fixCropWindowRectByRules(RectF rect) {
        if (rect.width() < this.mCropWindowHandler.getMinCropWidth()) {
            float adj = (this.mCropWindowHandler.getMinCropWidth() - rect.width()) / 2.0f;
            rect.left -= adj;
            rect.right += adj;
        }
        if (rect.height() < this.mCropWindowHandler.getMinCropHeight()) {
            float adj2 = (this.mCropWindowHandler.getMinCropHeight() - rect.height()) / 2.0f;
            rect.top -= adj2;
            rect.bottom += adj2;
        }
        if (rect.width() > this.mCropWindowHandler.getMaxCropWidth()) {
            float adj3 = (rect.width() - this.mCropWindowHandler.getMaxCropWidth()) / 2.0f;
            rect.left += adj3;
            rect.right -= adj3;
        }
        if (rect.height() > this.mCropWindowHandler.getMaxCropHeight()) {
            float adj4 = (rect.height() - this.mCropWindowHandler.getMaxCropHeight()) / 2.0f;
            rect.top += adj4;
            rect.bottom -= adj4;
        }
        calculateBounds(rect);
        if (this.mCalcBounds.width() > 0.0f && this.mCalcBounds.height() > 0.0f) {
            float leftLimit = Math.max(this.mCalcBounds.left, 0.0f);
            float topLimit = Math.max(this.mCalcBounds.top, 0.0f);
            float rightLimit = Math.min(this.mCalcBounds.right, (float) getWidth());
            float bottomLimit = Math.min(this.mCalcBounds.bottom, (float) getHeight());
            if (rect.left < leftLimit) {
                rect.left = leftLimit;
            }
            if (rect.top < topLimit) {
                rect.top = topLimit;
            }
            if (rect.right > rightLimit) {
                rect.right = rightLimit;
            }
            if (rect.bottom > bottomLimit) {
                rect.bottom = bottomLimit;
            }
        }
        if (this.mFixAspectRatio && ((double) Math.abs(rect.width() - (rect.height() * this.mTargetAspectRatio))) > 0.1d) {
            if (rect.width() > rect.height() * this.mTargetAspectRatio) {
                float adj5 = Math.abs((rect.height() * this.mTargetAspectRatio) - rect.width()) / 2.0f;
                rect.left += adj5;
                rect.right -= adj5;
                return;
            }
            float adj6 = Math.abs((rect.width() / this.mTargetAspectRatio) - rect.height()) / 2.0f;
            rect.top += adj6;
            rect.bottom -= adj6;
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        if (this.mCropWindowHandler.showGuidelines()) {
            if (this.mGuidelines == CropImageView.Guidelines.ON) {
                drawGuidelines(canvas);
            } else if (this.mGuidelines == CropImageView.Guidelines.ON_TOUCH && this.mMoveHandler != null) {
                drawGuidelines(canvas);
            }
        }
        drawBorders(canvas);
        drawCorners(canvas);
    }

    private void drawBackground(Canvas canvas) {
        RectF rect = this.mCropWindowHandler.getRect();
        float left = Math.max(BitmapUtils.getRectLeft(this.mBoundsPoints), 0.0f);
        float top = Math.max(BitmapUtils.getRectTop(this.mBoundsPoints), 0.0f);
        float right = Math.min(BitmapUtils.getRectRight(this.mBoundsPoints), (float) getWidth());
        float bottom = Math.min(BitmapUtils.getRectBottom(this.mBoundsPoints), (float) getHeight());
        if (this.mCropShape != CropImageView.CropShape.RECTANGLE) {
            this.mPath.reset();
            if (Build.VERSION.SDK_INT > 17 || this.mCropShape != CropImageView.CropShape.OVAL) {
                this.mDrawRect.set(rect.left, rect.top, rect.right, rect.bottom);
            } else {
                this.mDrawRect.set(rect.left + 2.0f, rect.top + 2.0f, rect.right - 2.0f, rect.bottom - 2.0f);
            }
            this.mPath.addOval(this.mDrawRect, Path.Direction.CW);
            canvas.save();
            canvas.clipPath(this.mPath, Region.Op.XOR);
            canvas.drawRect(left, top, right, bottom, this.mBackgroundPaint);
            canvas.restore();
        } else if (!isNonStraightAngleRotated() || Build.VERSION.SDK_INT <= 17) {
            canvas.drawRect(left, top, right, rect.top, this.mBackgroundPaint);
            canvas.drawRect(left, rect.bottom, right, bottom, this.mBackgroundPaint);
            canvas.drawRect(left, rect.top, rect.left, rect.bottom, this.mBackgroundPaint);
            canvas.drawRect(rect.right, rect.top, right, rect.bottom, this.mBackgroundPaint);
        } else {
            this.mPath.reset();
            this.mPath.moveTo(this.mBoundsPoints[0], this.mBoundsPoints[1]);
            this.mPath.lineTo(this.mBoundsPoints[2], this.mBoundsPoints[3]);
            this.mPath.lineTo(this.mBoundsPoints[4], this.mBoundsPoints[5]);
            this.mPath.lineTo(this.mBoundsPoints[6], this.mBoundsPoints[7]);
            this.mPath.close();
            canvas.save();
            canvas.clipPath(this.mPath, Region.Op.INTERSECT);
            canvas.clipRect(rect, Region.Op.XOR);
            canvas.drawRect(left, top, right, bottom, this.mBackgroundPaint);
            canvas.restore();
        }
    }

    private void drawGuidelines(Canvas canvas) {
        if (this.mGuidelinePaint != null) {
            float sw = this.mBorderPaint != null ? this.mBorderPaint.getStrokeWidth() : 0.0f;
            RectF rect = this.mCropWindowHandler.getRect();
            rect.inset(sw, sw);
            float oneThirdCropWidth = rect.width() / 3.0f;
            float oneThirdCropHeight = rect.height() / 3.0f;
            if (this.mCropShape == CropImageView.CropShape.OVAL) {
                float w = (rect.width() / 2.0f) - sw;
                float h = (rect.height() / 2.0f) - sw;
                float x1 = rect.left + oneThirdCropWidth;
                float x2 = rect.right - oneThirdCropWidth;
                float yv = (float) (((double) h) * Math.sin(Math.acos((double) ((w - oneThirdCropWidth) / w))));
                canvas.drawLine(x1, (rect.top + h) - yv, x1, (rect.bottom - h) + yv, this.mGuidelinePaint);
                canvas.drawLine(x2, (rect.top + h) - yv, x2, (rect.bottom - h) + yv, this.mGuidelinePaint);
                float y1 = rect.top + oneThirdCropHeight;
                float y2 = rect.bottom - oneThirdCropHeight;
                float xv = (float) (((double) w) * Math.cos(Math.asin((double) ((h - oneThirdCropHeight) / h))));
                canvas.drawLine((rect.left + w) - xv, y1, (rect.right - w) + xv, y1, this.mGuidelinePaint);
                canvas.drawLine((rect.left + w) - xv, y2, (rect.right - w) + xv, y2, this.mGuidelinePaint);
                return;
            }
            float x12 = rect.left + oneThirdCropWidth;
            float x22 = rect.right - oneThirdCropWidth;
            canvas.drawLine(x12, rect.top, x12, rect.bottom, this.mGuidelinePaint);
            canvas.drawLine(x22, rect.top, x22, rect.bottom, this.mGuidelinePaint);
            float y12 = rect.top + oneThirdCropHeight;
            float y22 = rect.bottom - oneThirdCropHeight;
            canvas.drawLine(rect.left, y12, rect.right, y12, this.mGuidelinePaint);
            canvas.drawLine(rect.left, y22, rect.right, y22, this.mGuidelinePaint);
        }
    }

    private void drawBorders(Canvas canvas) {
        if (this.mBorderPaint != null) {
            float w = this.mBorderPaint.getStrokeWidth();
            RectF rect = this.mCropWindowHandler.getRect();
            rect.inset(w / 2.0f, w / 2.0f);
            if (this.mCropShape == CropImageView.CropShape.RECTANGLE) {
                canvas.drawRect(rect, this.mBorderPaint);
            } else {
                canvas.drawOval(rect, this.mBorderPaint);
            }
        }
    }

    private void drawCorners(Canvas canvas) {
        float f = 0.0f;
        if (this.mBorderCornerPaint != null) {
            float lineWidth = this.mBorderPaint != null ? this.mBorderPaint.getStrokeWidth() : 0.0f;
            float cornerWidth = this.mBorderCornerPaint.getStrokeWidth();
            float f2 = cornerWidth / 2.0f;
            if (this.mCropShape == CropImageView.CropShape.RECTANGLE) {
                f = this.mBorderCornerOffset;
            }
            float w = f2 + f;
            RectF rect = this.mCropWindowHandler.getRect();
            rect.inset(w, w);
            float cornerOffset = (cornerWidth - lineWidth) / 2.0f;
            float cornerExtension = (cornerWidth / 2.0f) + cornerOffset;
            canvas.drawLine(rect.left - cornerOffset, rect.top - cornerExtension, rect.left - cornerOffset, this.mBorderCornerLength + rect.top, this.mBorderCornerPaint);
            canvas.drawLine(rect.left - cornerExtension, rect.top - cornerOffset, this.mBorderCornerLength + rect.left, rect.top - cornerOffset, this.mBorderCornerPaint);
            canvas.drawLine(rect.right + cornerOffset, rect.top - cornerExtension, rect.right + cornerOffset, this.mBorderCornerLength + rect.top, this.mBorderCornerPaint);
            canvas.drawLine(rect.right + cornerExtension, rect.top - cornerOffset, rect.right - this.mBorderCornerLength, rect.top - cornerOffset, this.mBorderCornerPaint);
            canvas.drawLine(rect.left - cornerOffset, rect.bottom + cornerExtension, rect.left - cornerOffset, rect.bottom - this.mBorderCornerLength, this.mBorderCornerPaint);
            canvas.drawLine(rect.left - cornerExtension, rect.bottom + cornerOffset, this.mBorderCornerLength + rect.left, rect.bottom + cornerOffset, this.mBorderCornerPaint);
            canvas.drawLine(rect.right + cornerOffset, rect.bottom + cornerExtension, rect.right + cornerOffset, rect.bottom - this.mBorderCornerLength, this.mBorderCornerPaint);
            canvas.drawLine(rect.right + cornerExtension, rect.bottom + cornerOffset, rect.right - this.mBorderCornerLength, rect.bottom + cornerOffset, this.mBorderCornerPaint);
        }
    }

    private static Paint getNewPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        return paint;
    }

    private static Paint getNewPaintOrNull(float thickness, int color) {
        if (thickness <= 0.0f) {
            return null;
        }
        Paint borderPaint = new Paint();
        borderPaint.setColor(color);
        borderPaint.setStrokeWidth(thickness);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        return borderPaint;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        if (this.mMultiTouchEnabled) {
            this.mScaleDetector.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case 0:
                onActionDown(event.getX(), event.getY());
                return true;
            case 1:
            case 3:
                getParent().requestDisallowInterceptTouchEvent(false);
                onActionUp();
                return true;
            case 2:
                onActionMove(event.getX(), event.getY());
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            default:
                return false;
        }
    }

    private void onActionDown(float x, float y) {
        this.mMoveHandler = this.mCropWindowHandler.getMoveHandler(x, y, this.mTouchRadius, this.mCropShape);
        if (this.mMoveHandler != null) {
            invalidate();
        }
    }

    private void onActionUp() {
        if (this.mMoveHandler != null) {
            this.mMoveHandler = null;
            callOnCropWindowChanged(false);
            invalidate();
        }
    }

    private void onActionMove(float x, float y) {
        if (this.mMoveHandler != null) {
            float snapRadius = this.mSnapRadius;
            RectF rect = this.mCropWindowHandler.getRect();
            if (calculateBounds(rect)) {
                snapRadius = 0.0f;
            }
            this.mMoveHandler.move(rect, x, y, this.mCalcBounds, this.mViewWidth, this.mViewHeight, snapRadius, this.mFixAspectRatio, this.mTargetAspectRatio);
            this.mCropWindowHandler.setRect(rect);
            callOnCropWindowChanged(true);
            invalidate();
        }
    }

    private boolean calculateBounds(RectF rect) {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        float left = BitmapUtils.getRectLeft(this.mBoundsPoints);
        float top = BitmapUtils.getRectTop(this.mBoundsPoints);
        float right = BitmapUtils.getRectRight(this.mBoundsPoints);
        float bottom = BitmapUtils.getRectBottom(this.mBoundsPoints);
        if (!isNonStraightAngleRotated()) {
            this.mCalcBounds.set(left, top, right, bottom);
            return false;
        }
        float x0 = this.mBoundsPoints[0];
        float y0 = this.mBoundsPoints[1];
        float x2 = this.mBoundsPoints[4];
        float y2 = this.mBoundsPoints[5];
        float x3 = this.mBoundsPoints[6];
        float y3 = this.mBoundsPoints[7];
        if (this.mBoundsPoints[7] < this.mBoundsPoints[1]) {
            if (this.mBoundsPoints[1] < this.mBoundsPoints[3]) {
                x0 = this.mBoundsPoints[6];
                y0 = this.mBoundsPoints[7];
                x2 = this.mBoundsPoints[2];
                y2 = this.mBoundsPoints[3];
                x3 = this.mBoundsPoints[4];
                y3 = this.mBoundsPoints[5];
            } else {
                x0 = this.mBoundsPoints[4];
                y0 = this.mBoundsPoints[5];
                x2 = this.mBoundsPoints[0];
                y2 = this.mBoundsPoints[1];
                x3 = this.mBoundsPoints[2];
                y3 = this.mBoundsPoints[3];
            }
        } else if (this.mBoundsPoints[1] > this.mBoundsPoints[3]) {
            x0 = this.mBoundsPoints[2];
            y0 = this.mBoundsPoints[3];
            x2 = this.mBoundsPoints[6];
            y2 = this.mBoundsPoints[7];
            x3 = this.mBoundsPoints[0];
            y3 = this.mBoundsPoints[1];
        }
        float a0 = (y3 - y0) / (x3 - x0);
        float a1 = -1.0f / a0;
        float b0 = y0 - (a0 * x0);
        float b1 = y0 - (a1 * x0);
        float b2 = y2 - (a0 * x2);
        float b3 = y2 - (a1 * x2);
        float c0 = (rect.centerY() - rect.top) / (rect.centerX() - rect.left);
        float c1 = -c0;
        float d0 = rect.top - (rect.left * c0);
        float d1 = rect.top - (rect.right * c1);
        if ((d0 - b0) / (a0 - c0) < rect.right) {
            f = (d0 - b0) / (a0 - c0);
        } else {
            f = left;
        }
        float left2 = Math.max(left, f);
        if ((d0 - b1) / (a1 - c0) < rect.right) {
            f2 = (d0 - b1) / (a1 - c0);
        } else {
            f2 = left2;
        }
        float left3 = Math.max(left2, f2);
        if ((d1 - b3) / (a1 - c1) < rect.right) {
            f3 = (d1 - b3) / (a1 - c1);
        } else {
            f3 = left3;
        }
        float left4 = Math.max(left3, f3);
        if ((d1 - b1) / (a1 - c1) > rect.left) {
            f4 = (d1 - b1) / (a1 - c1);
        } else {
            f4 = right;
        }
        float right2 = Math.min(right, f4);
        if ((d1 - b2) / (a0 - c1) > rect.left) {
            f5 = (d1 - b2) / (a0 - c1);
        } else {
            f5 = right2;
        }
        float right3 = Math.min(right2, f5);
        if ((d0 - b2) / (a0 - c0) > rect.left) {
            f6 = (d0 - b2) / (a0 - c0);
        } else {
            f6 = right3;
        }
        float right4 = Math.min(right3, f6);
        float top2 = Math.max(top, Math.max((a0 * left4) + b0, (a1 * right4) + b1));
        float bottom2 = Math.min(bottom, Math.min((a1 * left4) + b3, (a0 * right4) + b2));
        this.mCalcBounds.left = left4;
        this.mCalcBounds.top = top2;
        this.mCalcBounds.right = right4;
        this.mCalcBounds.bottom = bottom2;
        return true;
    }

    private boolean isNonStraightAngleRotated() {
        return (this.mBoundsPoints[0] == this.mBoundsPoints[6] || this.mBoundsPoints[1] == this.mBoundsPoints[7]) ? false : true;
    }

    private void callOnCropWindowChanged(boolean inProgress) {
        try {
            if (this.mCropWindowChangeListener != null) {
                this.mCropWindowChangeListener.onCropWindowChanged(inProgress);
            }
        } catch (Exception e) {
            Log.e("AIC", "Exception in crop window changed", e);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        @TargetApi(11)
        public boolean onScale(ScaleGestureDetector detector) {
            RectF rect = CropOverlayView.this.mCropWindowHandler.getRect();
            float x = detector.getFocusX();
            float y = detector.getFocusY();
            float dY = detector.getCurrentSpanY() / 2.0f;
            float dX = detector.getCurrentSpanX() / 2.0f;
            float newTop = y - dY;
            float newLeft = x - dX;
            float newRight = x + dX;
            float newBottom = y + dY;
            if (newLeft >= newRight || newTop > newBottom || newLeft < 0.0f || newRight > CropOverlayView.this.mCropWindowHandler.getMaxCropWidth() || newTop < 0.0f || newBottom > CropOverlayView.this.mCropWindowHandler.getMaxCropHeight()) {
                return true;
            }
            rect.set(newLeft, newTop, newRight, newBottom);
            CropOverlayView.this.mCropWindowHandler.setRect(rect);
            CropOverlayView.this.invalidate();
            return true;
        }
    }
}
