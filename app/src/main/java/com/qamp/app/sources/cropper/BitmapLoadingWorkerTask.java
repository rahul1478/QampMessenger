package com.qamp.app.sources.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.DisplayMetrics;

import java.lang.ref.WeakReference;

final class BitmapLoadingWorkerTask extends AsyncTask<Void, Void, BitmapLoadingWorkerTask.Result> {
    private final Context mContext;
    private final WeakReference<CropImageView> mCropImageViewReference;
    private final int mHeight;
    private final Uri mUri;
    private final int mWidth;

    public BitmapLoadingWorkerTask(CropImageView cropImageView, Uri uri) {
        this.mUri = uri;
        this.mCropImageViewReference = new WeakReference<>(cropImageView);
        this.mContext = cropImageView.getContext();
        DisplayMetrics metrics = cropImageView.getResources().getDisplayMetrics();
        double densityAdj = metrics.density > 1.0f ? (double) (1.0f / metrics.density) : 1.0d;
        this.mWidth = (int) (((double) metrics.widthPixels) * densityAdj);
        this.mHeight = (int) (((double) metrics.heightPixels) * densityAdj);
    }

    public Uri getUri() {
        return this.mUri;
    }

    /* access modifiers changed from: protected */
    public Result doInBackground(Void... params) {
        try {
            if (!isCancelled()) {
                BitmapUtils.BitmapSampled decodeResult = BitmapUtils.decodeSampledBitmap(this.mContext, this.mUri, this.mWidth, this.mHeight);
                if (!isCancelled()) {
                    BitmapUtils.RotateBitmapResult rotateResult = BitmapUtils.rotateBitmapByExif(decodeResult.bitmap, this.mContext, this.mUri);
                    return new Result(this.mUri, rotateResult.bitmap, decodeResult.sampleSize, rotateResult.degrees);
                }
            }
            return null;
        } catch (Exception e) {
            return new Result(this.mUri, e);
        }
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Result result) {
        CropImageView cropImageView;
        if (result != null) {
            boolean completeCalled = false;
            if (!isCancelled() && (cropImageView = (CropImageView) this.mCropImageViewReference.get()) != null) {
                completeCalled = true;
                cropImageView.onSetImageUriAsyncComplete(result);
            }
            if (!completeCalled && result.bitmap != null) {
                result.bitmap.recycle();
            }
        }
    }

    public static final class Result {
        public final Bitmap bitmap;
        public final int degreesRotated;
        public final Exception error;
        public final int loadSampleSize;
        public final Uri uri;

        Result(Uri uri2, Bitmap bitmap2, int loadSampleSize2, int degreesRotated2) {
            this.uri = uri2;
            this.bitmap = bitmap2;
            this.loadSampleSize = loadSampleSize2;
            this.degreesRotated = degreesRotated2;
            this.error = null;
        }

        Result(Uri uri2, Exception error2) {
            this.uri = uri2;
            this.bitmap = null;
            this.loadSampleSize = 0;
            this.degreesRotated = 0;
            this.error = error2;
        }
    }
}
