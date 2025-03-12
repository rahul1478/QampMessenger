package com.qamp.app.sources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Date;
import java.util.UUID;

public final class SocialUtilities {
    public static String createImageFromBitmap(Context ctx, Bitmap bitmap) {
        File f = new File(Environment.getExternalStorageDirectory(), "temp" + new Date().getTime() + ".jpg");
        try {
            FileOutputStream fo = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fo);
            fo.flush();
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f.getAbsolutePath();
    }

    public static Bitmap getMyProfilePictureBitmap(Context ctx) {
        try {
            return BitmapFactory.decodeStream(ctx.openFileInput("myImage"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String saveBitmpToFile(Context ctx, Bitmap bitmap, String fileName) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = ctx.openFileOutput(fileName, 0);
            fo.write(bytes.toByteArray());
            fo.close();
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapFromfile(Context ctx, String filename) {
        try {
            return BitmapFactory.decodeStream(ctx.openFileInput(filename));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapFromImagefile(String filename) {
        try {
            return BitmapFactory.decodeFile(filename);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap createSqrCropppedProfile(Bitmap bitmap) {
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            return Bitmap.createBitmap(bitmap, (bitmap.getWidth() / 2) - (bitmap.getHeight() / 2), 0, bitmap.getHeight(), bitmap.getHeight());
        }
        return Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() / 2) - (bitmap.getWidth() / 2), bitmap.getWidth(), bitmap.getWidth());
    }

    public static String bitmapToFilepath(Context mContext, Bitmap icon) {
        File dir = new File(MediaPicker.getPath() + "/profiles");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, UUID.randomUUID().toString() + ".png");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        icon.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static Bitmap createThumbnailAtTime(String filePath, int timeInSeconds) {
        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        try {
            mMMR.setDataSource(filePath);
            return mMMR.getFrameAtTime((long) (1000000 * timeInSeconds), 2);
        } catch (Exception e) {
            return null;
        }
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, new ByteArrayOutputStream());
        return Uri.parse(MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", (String) null));
    }

    public static int getExifRotation(String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int rotation = exif.getAttributeInt("Orientation", 1);
            int rotationInDegrees = 0;
            if (rotation == 6) {
                rotationInDegrees = 90;
            } else if (rotation == 3) {
                rotationInDegrees = 180;
            } else if (rotation == 8) {
                rotationInDegrees = 270;
            }
            ExifInterface exifInterface = exif;
            return rotationInDegrees;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static ExifInterface getExif(String filePath) {
        try {
            return new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getDeviceRotation(Context context) {
        int deviceRotation = ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRotation();
        if (deviceRotation == 0) {
            return 0;
        }
        if (1 == deviceRotation) {
            return 90;
        }
        if (2 == deviceRotation) {
            return 180;
        }
        return 3 == deviceRotation ? 270 : 0;
    }
}
