package com.qamp.app.sources;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaPicker {
    public static int BASE_TYPE_VALUE = 10000;
    public static int TYPE_AUDIO = 10006;
    public static int TYPE_CAMERAIMAGE = 10001;
    public static int TYPE_CAMERAVIDEO = 10003;
    public static int TYPE_CAPTION = 10015;
    public static int TYPE_CUSTOM = 10007;
    public static int TYPE_FACEBOOK = 10004;
    public static int TYPE_FILE = 10005;
    public static int TYPE_FILEIMAGE = 10000;
    public static int TYPE_FILEVIDEO = 10002;
    private static List<AlbumListData> mAlbumList = null;
    private static String mAuthority = null;
    private static ImageEditorListener mImageEditorListener = null;
    private static String mTempPath = null;
    private static int mToolbarColor = -16742773;

    public interface ImageEditorListener {
        void onImageEdit(int i, String str, String str2, Bitmap bitmap, int i2);
    }

    public static void setBaseTypeValue(int val) {
        TYPE_FILEIMAGE = (TYPE_FILEIMAGE - BASE_TYPE_VALUE) + val;
        TYPE_CAMERAIMAGE = (TYPE_CAMERAIMAGE - BASE_TYPE_VALUE) + val;
        TYPE_FILEVIDEO = (TYPE_FILEVIDEO - BASE_TYPE_VALUE) + val;
        TYPE_CAMERAVIDEO = (TYPE_CAMERAVIDEO - BASE_TYPE_VALUE) + val;
        TYPE_FACEBOOK = (TYPE_FACEBOOK - BASE_TYPE_VALUE) + val;
        TYPE_AUDIO = (TYPE_AUDIO - BASE_TYPE_VALUE) + val;
        TYPE_CUSTOM = (TYPE_CUSTOM - BASE_TYPE_VALUE) + val;
        TYPE_CAPTION = (TYPE_CAPTION - BASE_TYPE_VALUE) + val;
        BASE_TYPE_VALUE = val;
    }

    public static int getBaseTypeValue() {
        return BASE_TYPE_VALUE;
    }

    public static int getMaxTypeValue() {
        return TYPE_CAPTION;
    }

    public static void setToolbarColor(int color) {
        mToolbarColor = color;
    }

    public static int getToolbarColor() {
        return mToolbarColor;
    }

    protected static ImageEditorListener getImageEditorListener() {
        return mImageEditorListener;
    }

    public static void launchEditor(Activity context, int type, int drawableid, String title, String filePath, boolean showEditControls, boolean showTitle, boolean showCropOverlay, boolean squareCrop, int maxDimension, ImageEditorListener listener) {
        Intent in = new Intent(context, ImageEditor.class);
        in.putExtra("title", title);
        in.putExtra("filepath", filePath);
        in.putExtra("showEditControls", showEditControls);
        in.putExtra("showTitle", showTitle);
        in.putExtra("showCrop", showCropOverlay);
        in.putExtra("squareCrop", squareCrop);
        in.putExtra("type", type);
        in.putExtra("drawableid", drawableid);
        mImageEditorListener = listener;
        if (maxDimension > 0) {
            in.putExtra("maxDimension", maxDimension);
        }
        if (listener == null) {
            context.startActivityForResult(in, TYPE_CAPTION);
        } else {
            context.startActivity(in);
        }
    }

    public static void launchImageViewer(Activity context, String filePath) {
        Intent intent = new Intent(context, zoomVuPictureActivity.class);
        intent.putExtra("filePath", filePath);
        context.startActivity(intent);
    }

    public static void launchImageViewer(Activity context, ArrayList<String> files, int firstIndex) {
        Intent intent = new Intent(context, zoomVuPictureActivity.class);
        intent.putExtra("position", firstIndex);
        intent.putStringArrayListExtra("stringImageArray", files);
        context.startActivity(intent);
    }

    public static void launchAlbum(Activity context, List<AlbumListData> albumList) {
        mAlbumList = albumList;
        context.startActivity(new Intent(context, AlbumStartActivity.class));
    }

    public static List<AlbumListData> getAlbumList() {
        return mAlbumList;
    }

    public static void launchPicker(Activity activity, int fileType, String path) {
        ImagePicker.getInstance().pick(activity, fileType, path);
    }

    public static void launchPicker(Activity activity, int fileType) {
        ImagePicker.getInstance().pick(activity, fileType);
    }

    public boolean isActivityStarted(int code) {
        return code >= getBaseTypeValue() || code <= getMaxTypeValue();
    }

    public static String processOnActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        return ImagePicker.getInstance().processOnActivityResult(activity, requestCode, resultCode, data);
    }

    public static void setPath(String path, String authority) {
        mTempPath = path;
        mAuthority = authority;
    }

    public static void setPath(String path) {
        mTempPath = path;
    }

    public static void setAuthority(String authority) {
        mAuthority = authority;
    }

    public static String getPath() {
        if (TextUtils.isEmpty(mTempPath)) {
            return Environment.getExternalStorageDirectory().getPath();
        }
        return mTempPath;
    }

    public static String getAuthority(Context context) {
        if (TextUtils.isEmpty(mAuthority)) {
            return context.getPackageName() ;
        }
        return mAuthority;
    }

    public static Uri getUri(Context context, File file) {
        return FileProvider.getUriForFile(context, getAuthority(context), file);
    }

    protected static String getTempPath(Context context, String name, String ext, boolean video) {
        try {
            return File.createTempFile(name, ext, context.getExternalFilesDir(video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES)).getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
}
