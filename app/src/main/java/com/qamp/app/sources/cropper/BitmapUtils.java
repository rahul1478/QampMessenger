package com.qamp.app.sources.cropper;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import androidx.exifinterface.media.ExifInterface;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

final class BitmapUtils {
    static final Rect EMPTY_RECT = new Rect();
    static final RectF EMPTY_RECT_F = new RectF();
    static final float[] POINTS = new float[6];
    static final float[] POINTS2 = new float[6];
    static final RectF RECT = new RectF();
    private static int mMaxTextureSize;
    static Pair<String, WeakReference<Bitmap>> mStateBitmap;

    BitmapUtils() {
    }

    static RotateBitmapResult rotateBitmapByExif(Bitmap bitmap, Context context, Uri uri) {
        ExifInterface ei = null;
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is != null) {
                ExifInterface ei2 = new ExifInterface(is);
                try {
                    is.close();
                    ei = ei2;
                } catch (Exception e) {
                    ei = ei2;
                }
            }
        } catch (Exception e2) {
        }
        return ei != null ? rotateBitmapByExif(bitmap, ei) : new RotateBitmapResult(bitmap, 0);
    }

    static RotateBitmapResult rotateBitmapByExif(Bitmap bitmap, ExifInterface exif) {
        int degrees;
        switch (exif.getAttributeInt("Orientation", 1)) {
            case 3:
                degrees = 180;
                break;
            case 6:
                degrees = 90;
                break;
            case 8:
                degrees = 270;
                break;
            default:
                degrees = 0;
                break;
        }
        return new RotateBitmapResult(bitmap, degrees);
    }

    static BitmapSampled decodeSampledBitmap(Context context, Uri uri, int reqWidth, int reqHeight) {
        try {
            ContentResolver resolver = context.getContentResolver();
            BitmapFactory.Options options = decodeImageForOption(resolver, uri);
            options.inSampleSize = Math.max(calculateInSampleSizeByReqestedSize(options.outWidth, options.outHeight, reqWidth, reqHeight), calculateInSampleSizeByMaxTextureSize(options.outWidth, options.outHeight));
            return new BitmapSampled(decodeImage(resolver, uri, options), options.inSampleSize);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sampled bitmap: " + uri + "\r\n" + e.getMessage(), e);
        }
    }

    static BitmapSampled cropBitmapObjectHandleOOM(Bitmap bitmap, float[] points, int degreesRotated, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY, boolean flipHorizontally, boolean flipVertically) {
        int scale = 1;
        do {
            try {
                return new BitmapSampled(cropBitmapObjectWithScale(bitmap, points, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY, 1.0f / ((float) scale), flipHorizontally, flipVertically), scale);
            } catch (OutOfMemoryError e) {
                scale *= 2;
                if (scale > 8) {
                    throw e;
                }
            }
        } while (scale > 8);

        return new BitmapSampled(cropBitmapObjectWithScale(bitmap, points, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY, 1.0f / ((float) scale), flipHorizontally, flipVertically), scale);

    }

    private static Bitmap cropBitmapObjectWithScale(Bitmap bitmap, float[] points, int degreesRotated, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY, float scale, boolean flipHorizontally, boolean flipVertically) {
        float f;
        Rect rect = getRectFromPoints(points, bitmap.getWidth(), bitmap.getHeight(), fixAspectRatio, aspectRatioX, aspectRatioY);
        Matrix matrix = new Matrix();
        matrix.setRotate((float) degreesRotated, (float) (bitmap.getWidth() / 2), (float) (bitmap.getHeight() / 2));
        if (flipHorizontally) {
            f = -scale;
        } else {
            f = scale;
        }
        if (flipVertically) {
            scale = -scale;
        }
        matrix.postScale(f, scale);
        Bitmap result = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), matrix, true);
        if (result == bitmap) {
            result = bitmap.copy(bitmap.getConfig(), false);
        }
        if (degreesRotated % 90 != 0) {
            return cropForRotatedImage(result, points, rect, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY);
        }
        return result;
    }

    static BitmapSampled cropBitmap(Context context, Uri loadedImageUri, float[] points, int degreesRotated, int orgWidth, int orgHeight, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY, int reqWidth, int reqHeight, boolean flipHorizontally, boolean flipVertically) {
        int sampleMulti = 1;
        do {
            try {
                return cropBitmap(context, loadedImageUri, points, degreesRotated, orgWidth, orgHeight, fixAspectRatio, aspectRatioX, aspectRatioY, reqWidth, reqHeight, flipHorizontally, flipVertically, sampleMulti);
            } catch (OutOfMemoryError e) {
                sampleMulti *= 2;
                if (sampleMulti > 16) {
                    throw new RuntimeException("Failed to handle OOM by sampling (" + sampleMulti + "): " + loadedImageUri + "\r\n" + e.getMessage(), e);
                }
            }
        } while (sampleMulti > 16);
        throw new RuntimeException("Failed to handle OOM by sampling (" + sampleMulti + "): " + loadedImageUri + "\r\n");
    }

    static float getRectLeft(float[] points) {
        return Math.min(Math.min(Math.min(points[0], points[2]), points[4]), points[6]);
    }

    static float getRectTop(float[] points) {
        return Math.min(Math.min(Math.min(points[1], points[3]), points[5]), points[7]);
    }

    static float getRectRight(float[] points) {
        return Math.max(Math.max(Math.max(points[0], points[2]), points[4]), points[6]);
    }

    static float getRectBottom(float[] points) {
        return Math.max(Math.max(Math.max(points[1], points[3]), points[5]), points[7]);
    }

    static float getRectWidth(float[] points) {
        return getRectRight(points) - getRectLeft(points);
    }

    static float getRectHeight(float[] points) {
        return getRectBottom(points) - getRectTop(points);
    }

    static float getRectCenterX(float[] points) {
        return (getRectRight(points) + getRectLeft(points)) / 2.0f;
    }

    static float getRectCenterY(float[] points) {
        return (getRectBottom(points) + getRectTop(points)) / 2.0f;
    }

    static Rect getRectFromPoints(float[] points, int imageWidth, int imageHeight, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY) {
        Rect rect = new Rect(Math.round(Math.max(0.0f, getRectLeft(points))), Math.round(Math.max(0.0f, getRectTop(points))), Math.round(Math.min((float) imageWidth, getRectRight(points))), Math.round(Math.min((float) imageHeight, getRectBottom(points))));
        if (fixAspectRatio) {
            fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY);
        }
        return rect;
    }

    private static void fixRectForAspectRatio(Rect rect, int aspectRatioX, int aspectRatioY) {
        if (aspectRatioX == aspectRatioY && rect.width() != rect.height()) {
            if (rect.height() > rect.width()) {
                rect.bottom -= rect.height() - rect.width();
            } else {
                rect.right -= rect.width() - rect.height();
            }
        }
    }

    static Uri writeTempStateStoreBitmap(Context context, Bitmap bitmap, Uri uri) {
        Uri uri2 = null;
        boolean needSave = true;
        if (uri == null) {
            try {
                uri2 = Uri.fromFile(File.createTempFile("aic_state_store_temp", ".jpg", context.getCacheDir()));
            } catch (Exception e) {
                e = e;
            }
        } else if (new File(uri.getPath()).exists()) {
            needSave = false;
            uri2 = uri;
        } else {
            uri2 = uri;
        }
        if (needSave) {
            try {
                writeBitmapToUri(context, bitmap, uri2, Bitmap.CompressFormat.JPEG, 95);
            } catch (Exception e2) {
                Exception e = e2;

                Log.w("AIC", "Failed to write bitmap to temp file for image-cropper save instance state", e);
                return null;
            }
        }

        return uri2;
    }

    static void writeBitmapToUri(Context context, Bitmap bitmap, Uri uri, Bitmap.CompressFormat compressFormat, int compressQuality) throws FileNotFoundException {
        OutputStream outputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
            bitmap.compress(compressFormat, compressQuality, outputStream);
        } finally {
            closeSafe(outputStream);
        }
    }

    static Bitmap resizeBitmap(Bitmap bitmap, int reqWidth, int reqHeight, CropImageView.RequestSizeOptions options) {
        if (reqWidth > 0 && reqHeight > 0) {
            try {
                if (options == CropImageView.RequestSizeOptions.RESIZE_FIT || options == CropImageView.RequestSizeOptions.RESIZE_INSIDE || options == CropImageView.RequestSizeOptions.RESIZE_EXACT) {
                    Bitmap resized = null;
                    if (options == CropImageView.RequestSizeOptions.RESIZE_EXACT) {
                        resized = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
                    } else {
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        float scale = Math.max(((float) width) / ((float) reqWidth), ((float) height) / ((float) reqHeight));
                        if (scale > 1.0f || options == CropImageView.RequestSizeOptions.RESIZE_FIT) {
                            resized = Bitmap.createScaledBitmap(bitmap, (int) (((float) width) / scale), (int) (((float) height) / scale), false);
                        }
                    }
                    if (resized != null) {
                        if (resized == bitmap) {
                            return resized;
                        }
                        bitmap.recycle();
                        return resized;
                    }
                }
            } catch (Exception e) {
                Log.w("AIC", "Failed to resize cropped image, return bitmap before resize", e);
            }
        }
        return bitmap;
    }

    private static BitmapSampled cropBitmap(Context context, Uri loadedImageUri, float[] points, int degreesRotated, int orgWidth, int orgHeight, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY, int reqWidth, int reqHeight, boolean flipHorizontally, boolean flipVertically, int sampleMulti) {
        Bitmap result;
        Rect rect = getRectFromPoints(points, orgWidth, orgHeight, fixAspectRatio, aspectRatioX, aspectRatioY);
        int width = reqWidth > 0 ? reqWidth : rect.width();
        int height = reqHeight > 0 ? reqHeight : rect.height();
        int sampleSize = 1;
        try {
            BitmapSampled bitmapSampled = decodeSampledBitmapRegion(context, loadedImageUri, rect, width, height, sampleMulti);
            result = bitmapSampled.bitmap;
            try {
                sampleSize = bitmapSampled.sampleSize;
            } catch (Exception e) {
            }
        } catch (Exception e2) {
            result = null;
        }
        if (result == null) {
            return cropBitmap(context, loadedImageUri, points, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY, sampleMulti, rect, width, height, flipHorizontally, flipVertically);
        }
        try {
            Bitmap result2 = rotateAndFlipBitmapInt(result, degreesRotated, flipHorizontally, flipVertically);
            if (degreesRotated % 90 != 0) {
                result2 = cropForRotatedImage(result2, points, rect, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY);
            }
            return new BitmapSampled(result2, sampleSize);
        } catch (OutOfMemoryError e3) {
            if (result != null) {
                result.recycle();
            }
            throw e3;
        }
    }

    private static BitmapSampled cropBitmap(Context context, Uri loadedImageUri, float[] points, int degreesRotated, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY, int sampleMulti, Rect rect, int width, int height, boolean flipHorizontally, boolean flipVertically) {
        Bitmap fullBitmap = null;
        Bitmap result = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            int sampleSize = sampleMulti * calculateInSampleSizeByReqestedSize(rect.width(), rect.height(), width, height);
            options.inSampleSize = sampleSize;
            fullBitmap = decodeImage(context.getContentResolver(), loadedImageUri, options);
            if (fullBitmap != null) {
                float[] points2 = new float[points.length];
                System.arraycopy(points, 0, points2, 0, points.length);
                for (int i = 0; i < points2.length; i++) {
                    points2[i] = points2[i] / ((float) options.inSampleSize);
                }
                result = cropBitmapObjectWithScale(fullBitmap, points2, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY, 1.0f, flipHorizontally, flipVertically);
                if (result != fullBitmap) {
                    fullBitmap.recycle();
                }
            }
            return new BitmapSampled(result, sampleSize);
        } catch (OutOfMemoryError e) {
            if (result != null) {
                result.recycle();
            }
            throw e;
        } catch (Exception e2) {
            throw new RuntimeException("Failed to load sampled bitmap: " + loadedImageUri + "\r\n" + e2.getMessage(), e2);
        } catch (Throwable th) {
            if (result != fullBitmap) {
                fullBitmap.recycle();
            }
            throw th;
        }
    }

    private static BitmapFactory.Options decodeImageForOption(ContentResolver resolver, Uri uri) throws FileNotFoundException {
        InputStream stream = null;
        try {
            stream = resolver.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, EMPTY_RECT, options);
            options.inJustDecodeBounds = false;
            return options;
        } finally {
            closeSafe(stream);
        }
    }

    /* JADX INFO: finally extract failed */
    private static Bitmap decodeImage(ContentResolver resolver, Uri uri, BitmapFactory.Options options) throws FileNotFoundException {
        do {
            InputStream stream = null;
            try {
                stream = resolver.openInputStream(uri);
                Bitmap decodeStream = BitmapFactory.decodeStream(stream, EMPTY_RECT, options);
                closeSafe(stream);
                return decodeStream;
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2;
                closeSafe(stream);
                if (options.inSampleSize > 512) {
                    throw new RuntimeException("Failed to decode image: " + uri);
                }
            } catch (Throwable th) {
                closeSafe(stream);
                throw th;
            }
        } while (options.inSampleSize > 512);
        throw new RuntimeException("Failed to decode image: " + uri);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0049  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static BitmapSampled decodeSampledBitmapRegion(Context r7, Uri r8, Rect r9, int r10, int r11, int r12) {
        /*
            r3 = 0
            r0 = 0
            android.graphics.BitmapFactory$Options r2 = new android.graphics.BitmapFactory$Options     // Catch:{ Exception -> 0x0054 }
            r2.<init>()     // Catch:{ Exception -> 0x0054 }
            int r4 = r9.width()     // Catch:{ Exception -> 0x0054 }
            int r5 = r9.height()     // Catch:{ Exception -> 0x0054 }
            int r4 = calculateInSampleSizeByReqestedSize(r4, r5, r10, r11)     // Catch:{ Exception -> 0x0054 }
            int r4 = r4 * r12
            r2.inSampleSize = r4     // Catch:{ Exception -> 0x0054 }
            android.content.ContentResolver r4 = r7.getContentResolver()     // Catch:{ Exception -> 0x0054 }
            java.io.InputStream r3 = r4.openInputStream(r8)     // Catch:{ Exception -> 0x0054 }
            r4 = 0
            android.graphics.BitmapRegionDecoder r0 = android.graphics.BitmapRegionDecoder.newInstance(r3, r4)     // Catch:{ Exception -> 0x0054 }
        L_0x0023:
            com.mesibo.mediapicker.cropper.BitmapUtils$BitmapSampled r4 = new com.mesibo.mediapicker.cropper.BitmapUtils$BitmapSampled     // Catch:{ OutOfMemoryError -> 0x0037 }
            android.graphics.Bitmap r5 = r0.decodeRegion(r9, r2)     // Catch:{ OutOfMemoryError -> 0x0037 }
            int r6 = r2.inSampleSize     // Catch:{ OutOfMemoryError -> 0x0037 }
            r4.<init>(r5, r6)     // Catch:{ OutOfMemoryError -> 0x0037 }
            closeSafe(r3)
            if (r0 == 0) goto L_0x0036
            r0.recycle()
        L_0x0036:
            return r4
        L_0x0037:
            r1 = move-exception
            int r4 = r2.inSampleSize     // Catch:{ Exception -> 0x0054 }
            int r4 = r4 * 2
            r2.inSampleSize = r4     // Catch:{ Exception -> 0x0054 }
            int r4 = r2.inSampleSize     // Catch:{ Exception -> 0x0054 }
            r5 = 512(0x200, float:7.175E-43)
            if (r4 <= r5) goto L_0x0023
            closeSafe(r3)
            if (r0 == 0) goto L_0x004c
            r0.recycle()
        L_0x004c:
            com.mesibo.mediapicker.cropper.BitmapUtils$BitmapSampled r4 = new com.mesibo.mediapicker.cropper.BitmapUtils$BitmapSampled
            r5 = 0
            r6 = 1
            r4.<init>(r5, r6)
            goto L_0x0036
        L_0x0054:
            r1 = move-exception
            java.lang.RuntimeException r4 = new java.lang.RuntimeException     // Catch:{ all -> 0x007c }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x007c }
            r5.<init>()     // Catch:{ all -> 0x007c }
            java.lang.String r6 = "Failed to load sampled bitmap: "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x007c }
            java.lang.StringBuilder r5 = r5.append(r8)     // Catch:{ all -> 0x007c }
            java.lang.String r6 = "\r\n"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x007c }
            java.lang.String r6 = r1.getMessage()     // Catch:{ all -> 0x007c }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x007c }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x007c }
            r4.<init>(r5, r1)     // Catch:{ all -> 0x007c }
            throw r4     // Catch:{ all -> 0x007c }
        L_0x007c:
            r4 = move-exception
            closeSafe(r3)
            if (r0 == 0) goto L_0x0085
            r0.recycle()
        L_0x0085:
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mesibo.mediapicker.cropper.BitmapUtils.decodeSampledBitmapRegion(android.content.Context, android.net.Uri, android.graphics.Rect, int, int, int):com.mesibo.mediapicker.cropper.BitmapUtils$BitmapSampled");
    }

    private static Bitmap cropForRotatedImage(Bitmap bitmap, float[] points, Rect rect, int degreesRotated, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY) {
        int compareTo;
        if (degreesRotated % 90 != 0) {
            int adjLeft = 0;
            int adjTop = 0;
            int width = 0;
            int height = 0;
            double rads = Math.toRadians((double) degreesRotated);
            if (degreesRotated < 90 || (degreesRotated > 180 && degreesRotated < 270)) {
                compareTo = rect.left;
            } else {
                compareTo = rect.right;
            }
            int i = 0;
            while (true) {
                if (i < points.length) {
                    if (points[i] >= ((float) (compareTo - 1)) && points[i] <= ((float) (compareTo + 1))) {
                        adjLeft = (int) Math.abs(Math.sin(rads) * ((double) (((float) rect.bottom) - points[i + 1])));
                        adjTop = (int) Math.abs(Math.cos(rads) * ((double) (points[i + 1] - ((float) rect.top))));
                        width = (int) Math.abs(((double) (points[i + 1] - ((float) rect.top))) / Math.sin(rads));
                        height = (int) Math.abs(((double) (((float) rect.bottom) - points[i + 1])) / Math.cos(rads));
                        break;
                    }
                    i += 2;
                } else {
                    break;
                }
            }
            rect.set(adjLeft, adjTop, adjLeft + width, adjTop + height);
            if (fixAspectRatio) {
                fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY);
            }
            Bitmap bitmapTmp = bitmap;
            bitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
            if (bitmapTmp != bitmap) {
                bitmapTmp.recycle();
            }
        }
        return bitmap;
    }

    private static int calculateInSampleSizeByReqestedSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            while ((height / 2) / inSampleSize > reqHeight && (width / 2) / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static int calculateInSampleSizeByMaxTextureSize(int width, int height) {
        int inSampleSize = 1;
        if (mMaxTextureSize == 0) {
            mMaxTextureSize = getMaxTextureSize();
        }
        if (mMaxTextureSize > 0) {
            while (true) {
                if (height / inSampleSize <= mMaxTextureSize && width / inSampleSize <= mMaxTextureSize) {
                    break;
                }
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateAndFlipBitmapInt(Bitmap bitmap, int degrees, boolean flipHorizontally, boolean flipVertically) {
        float f;
        float f2 = -1.0f;
        if (degrees <= 0 && !flipHorizontally && !flipVertically) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate((float) degrees);
        if (flipHorizontally) {
            f = -1.0f;
        } else {
            f = 1.0f;
        }
        if (!flipVertically) {
            f2 = 1.0f;
        }
        matrix.postScale(f, f2);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        if (newBitmap != bitmap) {
            bitmap.recycle();
        }
        return newBitmap;
    }

    private static int getMaxTextureSize() {
        try {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            egl.eglInitialize(display, new int[2]);
            int[] totalConfigurations = new int[1];
            egl.eglGetConfigs(display, (EGLConfig[]) null, 0, totalConfigurations);
            EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
            egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);
            int[] textureSize = new int[1];
            int maximumTextureSize = 0;
            for (int i = 0; i < totalConfigurations[0]; i++) {
                egl.eglGetConfigAttrib(display, configurationsList[i], 12332, textureSize);
                if (maximumTextureSize < textureSize[0]) {
                    maximumTextureSize = textureSize[0];
                }
            }
            egl.eglTerminate(display);
            return Math.max(maximumTextureSize, 2048);
        } catch (Exception e) {
            return 2048;
        }
    }

    private static void closeSafe(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    static final class BitmapSampled {
        public final Bitmap bitmap;
        final int sampleSize;

        BitmapSampled(Bitmap bitmap2, int sampleSize2) {
            this.bitmap = bitmap2;
            this.sampleSize = sampleSize2;
        }
    }

    static final class RotateBitmapResult {
        public final Bitmap bitmap;
        final int degrees;

        RotateBitmapResult(Bitmap bitmap2, int degrees2) {
            this.bitmap = bitmap2;
            this.degrees = degrees2;
        }
    }
}
