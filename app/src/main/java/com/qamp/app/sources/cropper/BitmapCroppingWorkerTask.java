package com.qamp.app.sources.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

final class BitmapCroppingWorkerTask extends AsyncTask<Void, Void, BitmapCroppingWorkerTask.Result> {
    private final int mAspectRatioX;
    private final int mAspectRatioY;
    private final Bitmap mBitmap;
    private final Context mContext;
    private final WeakReference<CropImageView> mCropImageViewReference;
    private final float[] mCropPoints;
    private final int mDegreesRotated;
    private final boolean mFixAspectRatio;
    private final boolean mFlipHorizontally;
    private final boolean mFlipVertically;
    private final int mOrgHeight;
    private final int mOrgWidth;
    private final int mReqHeight;
    private final CropImageView.RequestSizeOptions mReqSizeOptions;
    private final int mReqWidth;
    private final Bitmap.CompressFormat mSaveCompressFormat;
    private final int mSaveCompressQuality;
    private final Uri mSaveUri;
    private final Uri mUri;

    BitmapCroppingWorkerTask(CropImageView cropImageView, Bitmap bitmap, float[] cropPoints, int degreesRotated, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY, int reqWidth, int reqHeight, boolean flipHorizontally, boolean flipVertically, CropImageView.RequestSizeOptions options, Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality) {
        this.mCropImageViewReference = new WeakReference<>(cropImageView);
        this.mContext = cropImageView.getContext();
        this.mBitmap = bitmap;
        this.mCropPoints = cropPoints;
        this.mUri = null;
        this.mDegreesRotated = degreesRotated;
        this.mFixAspectRatio = fixAspectRatio;
        this.mAspectRatioX = aspectRatioX;
        this.mAspectRatioY = aspectRatioY;
        this.mReqWidth = reqWidth;
        this.mReqHeight = reqHeight;
        this.mFlipHorizontally = flipHorizontally;
        this.mFlipVertically = flipVertically;
        this.mReqSizeOptions = options;
        this.mSaveUri = saveUri;
        this.mSaveCompressFormat = saveCompressFormat;
        this.mSaveCompressQuality = saveCompressQuality;
        this.mOrgWidth = 0;
        this.mOrgHeight = 0;
    }

    BitmapCroppingWorkerTask(CropImageView cropImageView, Uri uri, float[] cropPoints, int degreesRotated, int orgWidth, int orgHeight, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY, int reqWidth, int reqHeight, boolean flipHorizontally, boolean flipVertically, CropImageView.RequestSizeOptions options, Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality) {
        this.mCropImageViewReference = new WeakReference<>(cropImageView);
        this.mContext = cropImageView.getContext();
        this.mUri = uri;
        this.mCropPoints = cropPoints;
        this.mDegreesRotated = degreesRotated;
        this.mFixAspectRatio = fixAspectRatio;
        this.mAspectRatioX = aspectRatioX;
        this.mAspectRatioY = aspectRatioY;
        this.mOrgWidth = orgWidth;
        this.mOrgHeight = orgHeight;
        this.mReqWidth = reqWidth;
        this.mReqHeight = reqHeight;
        this.mFlipHorizontally = flipHorizontally;
        this.mFlipVertically = flipVertically;
        this.mReqSizeOptions = options;
        this.mSaveUri = saveUri;
        this.mSaveCompressFormat = saveCompressFormat;
        this.mSaveCompressQuality = saveCompressQuality;
        this.mBitmap = null;
    }

    public Uri getUri() {
        return this.mUri;
    }

    /* access modifiers changed from: protected */
    public Result doInBackground(Void... params) {
        BitmapUtils.BitmapSampled bitmapSampled;
        try {
            if (isCancelled()) {
                return null;
            }
            if (this.mUri != null) {
                bitmapSampled = BitmapUtils.cropBitmap(this.mContext, this.mUri, this.mCropPoints, this.mDegreesRotated, this.mOrgWidth, this.mOrgHeight, this.mFixAspectRatio, this.mAspectRatioX, this.mAspectRatioY, this.mReqWidth, this.mReqHeight, this.mFlipHorizontally, this.mFlipVertically);
            } else if (this.mBitmap == null) {
                return new Result((Bitmap) null, 1);
            } else {
                bitmapSampled = BitmapUtils.cropBitmapObjectHandleOOM(this.mBitmap, this.mCropPoints, this.mDegreesRotated, this.mFixAspectRatio, this.mAspectRatioX, this.mAspectRatioY, this.mFlipHorizontally, this.mFlipVertically);
            }
            Bitmap bitmap = BitmapUtils.resizeBitmap(bitmapSampled.bitmap, this.mReqWidth, this.mReqHeight, this.mReqSizeOptions);
            if (this.mSaveUri == null) {
                return new Result(bitmap, bitmapSampled.sampleSize);
            }
            BitmapUtils.writeBitmapToUri(this.mContext, bitmap, this.mSaveUri, this.mSaveCompressFormat, this.mSaveCompressQuality);
            if (bitmap != null) {
                bitmap.recycle();
            }
            return new Result(this.mSaveUri, bitmapSampled.sampleSize);
        } catch (Exception e) {
            return new Result(e, this.mSaveUri != null);
        }
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Result result) {
        CropImageView cropImageView;
        if (result != null) {
            boolean completeCalled = false;
            if (!isCancelled() && (cropImageView = (CropImageView) this.mCropImageViewReference.get()) != null) {
                completeCalled = true;
                cropImageView.onImageCroppingAsyncComplete(result);
            }
            if (!completeCalled && result.bitmap != null) {
                result.bitmap.recycle();
            }
        }
    }

    static final class Result {
        public final Bitmap bitmap;
        final Exception error;
        final boolean isSave;
        final int sampleSize;
        public final Uri uri;

        Result(Bitmap bitmap2, int sampleSize2) {
            this.bitmap = bitmap2;
            this.uri = null;
            this.error = null;
            this.isSave = false;
            this.sampleSize = sampleSize2;
        }

        Result(Uri uri2, int sampleSize2) {
            this.bitmap = null;
            this.uri = uri2;
            this.error = null;
            this.isSave = true;
            this.sampleSize = sampleSize2;
        }

        Result(Exception error2, boolean isSave2) {
            this.bitmap = null;
            this.uri = null;
            this.error = error2;
            this.isSave = isSave2;
            this.sampleSize = 1;
        }
    }
}
