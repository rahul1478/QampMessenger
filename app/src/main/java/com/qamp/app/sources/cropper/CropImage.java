package com.qamp.app.sources.cropper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.InputStream;

public final class CropImage {
    public static final int CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE = 2011;
    public static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203;
    public static final int CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE = 204;
    public static final String CROP_IMAGE_EXTRA_BUNDLE = "CROP_IMAGE_EXTRA_BUNDLE";
    public static final String CROP_IMAGE_EXTRA_OPTIONS = "CROP_IMAGE_EXTRA_OPTIONS";
    public static final String CROP_IMAGE_EXTRA_RESULT = "CROP_IMAGE_EXTRA_RESULT";
    public static final String CROP_IMAGE_EXTRA_SOURCE = "CROP_IMAGE_EXTRA_SOURCE";
    public static final int PICK_IMAGE_CHOOSER_REQUEST_CODE = 200;
    public static final int PICK_IMAGE_PERMISSIONS_REQUEST_CODE = 201;

    private CropImage() {
    }

    public static Bitmap toOvalBitmap(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-12434878);
        canvas.drawOval(new RectF(0.0f, 0.0f, (float) width, (float) height), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        bitmap.recycle();
        return output;
    }

    public static boolean isExplicitCameraPermissionRequired(@NonNull Context context) {
        return Build.VERSION.SDK_INT >= 23 && hasPermissionInManifest(context, "android.permission.CAMERA") && context.checkSelfPermission("android.permission.CAMERA") != 0;
    }

    public static boolean hasPermissionInManifest(@NonNull Context context, @NonNull String permissionName) {
        try {
            String[] declaredPermisisons = context.getPackageManager().getPackageInfo(context.getPackageName(), 4096).requestedPermissions;
            if (declaredPermisisons == null || declaredPermisisons.length <= 0) {
                return false;
            }
            for (String p : declaredPermisisons) {
                if (p.equalsIgnoreCase(permissionName)) {
                    return true;
                }
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static Uri getCaptureImageOutputUri(@NonNull Context context) {
        File getImage = context.getExternalCacheDir();
        if (getImage != null) {
            return Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return null;
    }

    public static Uri getPickImageResultUri(@NonNull Context context, @Nullable Intent data) {
        boolean isCamera = true;
        if (!(data == null || data.getData() == null)) {
            String action = data.getAction();
            isCamera = action != null && action.equals("android.media.action.IMAGE_CAPTURE");
        }
        return (isCamera || data.getData() == null) ? getCaptureImageOutputUri(context) : data.getData();
    }

    public static boolean isReadExternalStoragePermissionsRequired(@NonNull Context context, @NonNull Uri uri) {
        return Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0 && isUriRequiresPermissions(context, uri);
    }

    public static boolean isUriRequiresPermissions(@NonNull Context context, @NonNull Uri uri) {
        try {
            InputStream stream = context.getContentResolver().openInputStream(uri);
            if (stream != null) {
                stream.close();
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
