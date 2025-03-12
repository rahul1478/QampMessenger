package com.qamp.app.sources;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;

public class ScalingUtilities {

    public static class Result {
        public int height = 0;
        public String mimeType = null;
        public int origHeight = 0;
        public int origWidth = 0;
        public float scale = 1.0f;
        public boolean scaled = false;
        public int width = 0;
    }

    public enum ScalingLogic {
        CROP,
        FIT
    }

    public static Bitmap scale(String pathname, Rect srcRegion, int dstWidth, int dstHeight, ScalingLogic scalingLogic, Result result) {
        return decodeFile(pathname, srcRegion, dstWidth, dstHeight, 0, scalingLogic, result);
    }

    public static Bitmap scale(String pathname, Rect srcRegion, int maxSide, ScalingLogic scalingLogic, Result result) {
        return decodeFile(pathname, srcRegion, 0, 0, maxSide, scalingLogic, result);
    }

    public static Bitmap scale(Bitmap bmp, int maxSide, ScalingLogic scalingLogic, Result result) {
        int dstHeight = bmp.getHeight();
        int dstWidth = bmp.getWidth();
        int max = dstWidth;
        if (dstHeight > max) {
            max = dstHeight;
        }
        if (result != null) {
            result.height = dstHeight;
            result.width = dstWidth;
            result.mimeType = null;
            result.scaled = false;
        }
        float multiplier = ((float) maxSide) / ((float) max);
        if (((double) multiplier) < 0.999d) {
            dstHeight = (int) (((float) dstHeight) * multiplier);
            dstWidth = (int) (((float) dstWidth) * multiplier);
        } else if (scalingLogic == ScalingLogic.FIT) {
            return bmp;
        }
        return createScaledBitmap(bmp, dstWidth, dstHeight, scalingLogic, result);
    }

    public static Bitmap decodeResource(Resources res, int resId, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeResource(res, resId, options);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth, dstHeight, scalingLogic);
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeFile(String filename, Rect srcRegion, int dstWidth, int dstHeight, int maxDstSide, ScalingLogic scalingLogic, Result result) {
        Bitmap unscaledBitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapRegionDecoder regionDecoder = null;
        if (srcRegion != null && !srcRegion.isEmpty() && Build.VERSION.SDK_INT >= 10) {
            try {
                regionDecoder = BitmapRegionDecoder.newInstance(filename, false);
            } catch (Exception e) {
            }
        }
        if (regionDecoder != null) {
            regionDecoder.decodeRegion(srcRegion, options);
        } else {
            BitmapFactory.decodeFile(filename, options);
        }
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return null;
        }
        if (result != null) {
            result.height = options.outHeight;
            result.width = options.outWidth;
            result.origHeight = options.outHeight;
            result.origWidth = options.outWidth;
            result.mimeType = options.outMimeType;
            result.scaled = false;
        }
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPreferQualityOverSpeed = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        boolean returnUnscaled = false;
        float multiplier = 1.0f;
        if (maxDstSide > 0) {
            int max = options.outWidth;
            if (options.outHeight > max) {
                max = options.outHeight;
            }
            multiplier = ((float) maxDstSide) / ((float) max);
            dstHeight = options.outHeight;
            dstWidth = options.outWidth;
            if (((double) multiplier) < 0.999d) {
                dstHeight = (int) (((float) options.outHeight) * multiplier);
                dstWidth = (int) (((float) options.outWidth) * multiplier);
            } else if (scalingLogic == ScalingLogic.FIT) {
                returnUnscaled = true;
            }
        }
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth, dstHeight, scalingLogic);
        if (regionDecoder != null) {
            unscaledBitmap = regionDecoder.decodeRegion(srcRegion, options);
        } else {
            unscaledBitmap = BitmapFactory.decodeFile(filename, options);
        }
        if (returnUnscaled) {
            return unscaledBitmap;
        }
        if (result != null) {
            result.scale = 1.0f / multiplier;
        }
        return createScaledBitmap(unscaledBitmap, dstWidth, dstHeight, scalingLogic, result);
    }

    public static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight, ScalingLogic scalingLogic, Result result) {
        if (unscaledBitmap == null) {
            return null;
        }
        if (result != null) {
            result.height = dstHeight;
            result.width = dstWidth;
            result.scaled = true;
        }
        Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
        Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(), Bitmap.Config.ARGB_8888);
        new Canvas(scaledBitmap).drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(2));
        return scaledBitmap;
    }

    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
        float srcAspect = ((float) srcWidth) / ((float) srcHeight);
        float dstAspect = ((float) dstWidth) / ((float) dstHeight);
        if (scalingLogic == ScalingLogic.FIT) {
            if (srcAspect > dstAspect) {
                return srcWidth / dstWidth;
            }
            return srcHeight / dstHeight;
        } else if (srcAspect > dstAspect) {
            return srcHeight / dstHeight;
        } else {
            return srcWidth / dstWidth;
        }
    }

    public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
        if (scalingLogic != ScalingLogic.CROP) {
            return new Rect(0, 0, srcWidth, srcHeight);
        }
        float dstAspect = ((float) dstWidth) / ((float) dstHeight);
        if (((float) srcWidth) / ((float) srcHeight) > dstAspect) {
            int srcRectWidth = (int) (((float) srcHeight) * dstAspect);
            int srcRectLeft = (srcWidth - srcRectWidth) / 2;
            return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
        }
        int srcRectHeight = (int) (((float) srcWidth) / dstAspect);
        int scrRectTop = (srcHeight - srcRectHeight) / 2;
        return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
    }

    public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
        if (scalingLogic != ScalingLogic.FIT) {
            return new Rect(0, 0, dstWidth, dstHeight);
        }
        float srcAspect = ((float) srcWidth) / ((float) srcHeight);
        if (srcAspect > ((float) dstWidth) / ((float) dstHeight)) {
            return new Rect(0, 0, dstWidth, (int) (((float) dstWidth) / srcAspect));
        }
        return new Rect(0, 0, (int) (((float) dstHeight) * srcAspect), dstHeight);
    }
}
