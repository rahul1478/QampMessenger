package com.qamp.app.sources;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Comparator;

public class FileUtils {
    private static final boolean DEBUG = false;
    public static final String HIDDEN_PREFIX = ".";
    public static final String MIME_TYPE_APP = "application/*";
    public static final String MIME_TYPE_AUDIO = "audio/*";
    public static final String MIME_TYPE_IMAGE = "image/*";
    public static final String MIME_TYPE_TEXT = "text/*";
    public static final String MIME_TYPE_VIDEO = "video/*";
    static final String TAG = "FileUtils";
    public static Comparator<File> sComparator = new Comparator<File>() {
        public int compare(File f1, File f2) {
            return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
        }
    };
    public static FileFilter sDirFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory() && !file.getName().startsWith(FileUtils.HIDDEN_PREFIX);
        }
    };
    public static FileFilter sFileFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isFile() && !file.getName().startsWith(FileUtils.HIDDEN_PREFIX);
        }
    };

    private FileUtils() {
    }

    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }
        int dot = uri.lastIndexOf(HIDDEN_PREFIX);
        if (dot >= 0) {
            return uri.substring(dot);
        }
        return "";
    }

    public static boolean isLocal(String url) {
        if (url == null || url.startsWith("http://") || url.startsWith("https://")) {
            return false;
        }
        return true;
    }

    public static boolean isMediaUri(Uri uri) {
        return "media".equalsIgnoreCase(uri.getAuthority());
    }

    public static Uri getUri(File file) {
        if (file != null) {
            return Uri.fromFile(file);
        }
        return null;
    }

    public static File getPathWithoutFilename(File file) {
        if (file == null) {
            return null;
        }
        if (file.isDirectory()) {
            return file;
        }
        String filename = file.getName();
        String filepath = file.getAbsolutePath();
        String pathwithoutname = filepath.substring(0, filepath.length() - filename.length());
        if (pathwithoutname.endsWith("/")) {
            pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
        }
        return new File(pathwithoutname);
    }

    public static String getMimeType(File file) {
        String extension = getExtension(file.getName());
        if (extension.length() > 0) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
        }
        return "application/octet-stream";
    }

    public static String getMimeType(Context context, Uri uri) {
        return getMimeType(new File(getPath(context, uri)));
    }

    public static boolean isLocalStorageDocument(Uri uri) {
        return false;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            Cursor cursor2 = context.getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, (String) null);
            if (cursor2 == null || !cursor2.moveToFirst()) {
                if (cursor2 != null) {
                    cursor2.close();
                }
                return null;
            }
            String string = cursor2.getString(cursor2.getColumnIndexOrThrow("_data"));
            if (cursor2 == null) {
                return string;
            }
            cursor2.close();
            return string;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        return null;
    }

    private static String getPathInternal(Context context, Uri uri) {
        boolean isKitKat = Build.VERSION.SDK_INT >= 19;
        String authority = uri.getAuthority();
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                return getExternalStorageFilePath(uri);
            }
            if (isDownloadsDocument(uri)) {
                return getDownloadDocumentFilePath(context, uri);
            }
            if (isMediaDocument(uri)) {
                return getMediaDocumentFilePath(context, uri);
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, (String) null, (String[]) null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        } else {
            return null;
        }
    }

    public static String getPath(Context context, Uri uri) {
        if (ImagePicker.getInstance().getApp() != null) {
            File f = UriUtils.uri2File(context, uri);
            if (f == null) {
                return null;
            }
            String p = f.getAbsolutePath();
            String path = f.getPath();
            return p;
        }
        String path2 = getPathInternal(context, uri);
        if (path2 == null && Build.VERSION.SDK_INT >= 29) {
            path2 = copyFileToInternalStorage(context, uri, "temp");
        }
        return path2;
    }

    public static String getImagePathFromURI(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
        String path = null;
        if (cursor != null) {
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            String document_id2 = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();
            Cursor cursor2 = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, (String[]) null, "_id = ? ", new String[]{document_id2}, (String) null);
            if (cursor2 != null && cursor2.getCount() > 0) {
                cursor2.moveToFirst();
                int count = cursor2.getColumnCount();
                int index = cursor2.getColumnIndex("_data");
                if (index >= 0 && count > index) {
                    path = cursor2.getString(index);
                }
                cursor2.close();
            }
        }
        Log.d(TAG, "getImagePathFromURI " + path);
        return path;
    }

    private static String getDownloadDocumentFilePath(Context context, Uri uri) {
        String id = DocumentsContract.getDocumentId(uri);
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        if (id.startsWith("raw:")) {
            return id.replaceFirst("raw:", "");
        }
        if (id.startsWith("msf:")) {
            return getImagePathFromURI(context, uri);
        }
        return getDataColumn(context, ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id).longValue()), (String) null, (String[]) null);
    }

    private static String getExternalStorageFilePath(Uri uri) {
        String[] split = DocumentsContract.getDocumentId(uri).split(":");
        if ("primary".equalsIgnoreCase(split[0])) {
            return Environment.getExternalStorageDirectory() + "/" + split[1];
        }
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + split[1];
    }

    private static String getMediaDocumentFilePath(Context context, Uri uri) {
        String[] split = DocumentsContract.getDocumentId(uri).split(":");
        String type = split[0];
        Uri contentUri = null;
        char c = 65535;
        switch (type.hashCode()) {
            case 93166550:
                if (type.equals("audio")) {
                    c = 2;
                    break;
                }
                break;
            case 100313435:
                if (type.equals("image")) {
                    c = 0;
                    break;
                }
                break;
            case 112202875:
                if (type.equals("video")) {
                    c = 1;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case 1:
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case 2:
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
        }
        return getDataColumn(context, contentUri, "_id=?", new String[]{split[1]});
    }

    public static Intent createGetContentIntent() {
        return createGetContentIntent("*/*");
    }

    public static Intent createGetContentIntent(String mimeType) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        if (mimeType == null) {
            mimeType = "*/*";
        }
        intent.setType(mimeType);
        intent.addCategory("android.intent.category.OPENABLE");
        intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
        return intent;
    }

    public static File getFile(Context context, Uri uri) {
        String path;
        if (uri == null || (path = getPath(context, uri)) == null || !isLocal(path)) {
            return null;
        }
        return new File(path);
    }

    public static String getReadableFileSize(int size) {
        DecimalFormat dec = new DecimalFormat("###.#");
        float fileSize = 0.0f;
        String suffix = " KB";
        if (size > 1024) {
            fileSize = (float) (size / 1024);
            if (fileSize > 1024.0f) {
                fileSize /= 1024.0f;
                if (fileSize > 1024.0f) {
                    fileSize /= 1024.0f;
                    suffix = " GB";
                } else {
                    suffix = " MB";
                }
            }
        }
        return String.valueOf(dec.format((double) fileSize) + suffix);
    }

    public static Bitmap getThumbnail(Context context, File file) {
        return getThumbnail(context, getUri(file), getMimeType(file));
    }

    public static Bitmap getThumbnail(Context context, Uri uri) {
        return getThumbnail(context, uri, getMimeType(context, uri));
    }

    public static Bitmap getThumbnail(Context context, Uri uri, String mimeType) {
        Bitmap bm = null;
        if (!isMediaUri(uri)) {
            Log.e(TAG, "You can only retrieve thumbnails for images and videos.");
        } else {
            bm = null;
            if (uri != null) {
                ContentResolver resolver = context.getContentResolver();
                Cursor cursor = null;
                try {
                    cursor = resolver.query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
                    if (cursor.moveToFirst()) {
                        int id = cursor.getInt(0);
                        if (mimeType.contains("video")) {
                            bm = MediaStore.Video.Thumbnails.getThumbnail(resolver, (long) id, 1, (BitmapFactory.Options) null);
                        } else if (mimeType.contains(MIME_TYPE_IMAGE)) {
                            bm = MediaStore.Images.Thumbnails.getThumbnail(resolver, (long) id, 1, (BitmapFactory.Options) null);
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        }
        return bm;
    }

    private static String copyFileToInternalStorage(Context context, Uri uri, String newDirName) {
        File output;
        Cursor returnCursor = context.getContentResolver().query(uri, new String[]{"_display_name", "_size"}, (String) null, (String[]) null, (String) null);
        int nameIndex = returnCursor.getColumnIndex("_display_name");
        int sizeIndex = returnCursor.getColumnIndex("_size");
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        String l = Long.toString(returnCursor.getLong(sizeIndex));
        if (!newDirName.equals("")) {
            File dir = new File(context.getFilesDir() + "/" + newDirName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            output = new File(context.getFilesDir() + "/" + newDirName + "/" + name);
        } else {
            output = new File(context.getFilesDir() + "/" + name);
        }
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream fileOutputStream = new FileOutputStream(output);
            byte[] buffers = new byte[1024];
            while (true) {
                int read = inputStream.read(buffers);
                if (read == -1) {
                    break;
                }
                fileOutputStream.write(buffers, 0, read);
            }
            inputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return output.getPath();
    }
}
