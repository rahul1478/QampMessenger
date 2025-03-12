package com.qamp.app.sources;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class UriUtils {
    private static int sBufferSize = 524288;

    private UriUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static String getMimeType(Context context, Uri uri) {
        if (!uri.getScheme().equals("content")) {
            return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        if (mime != null) {
            return mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        }
        return "";
    }

    public static File uri2File(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        File file = uri2FileReal(context, uri);
        return file == null ? copyUri2Cache(uri, getMimeType(context, uri)) : file;
    }

    /* JADX WARNING: Removed duplicated region for block: B:120:0x0366 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x036c A[Catch:{ Exception -> 0x03f6 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static File uri2FileReal(Context r41, Uri r42) {
        /*
            java.lang.String r37 = "UriUtils"
            java.lang.String r38 = r42.toString()
            android.util.Log.d(r37, r38)
            java.lang.String r4 = r42.getAuthority()
            java.lang.String r29 = r42.getScheme()
            java.lang.String r27 = r42.getPath()
            int r37 = android.os.Build.VERSION.SDK_INT
            r38 = 24
            r0 = r37
            r1 = r38
            if (r0 < r1) goto L_0x01dc
            if (r27 == 0) goto L_0x01dc
            r37 = 2
            r0 = r37
            java.lang.String[] r14 = new java.lang.String[r0]
            r37 = 0
            java.lang.String r38 = "/external/"
            r14[r37] = r38
            r37 = 1
            java.lang.String r38 = "/external_path/"
            r14[r37] = r38
            r15 = 0
            int r0 = r14.length
            r38 = r0
            r37 = 0
        L_0x0039:
            r0 = r37
            r1 = r38
            if (r0 >= r1) goto L_0x009f
            r13 = r14[r37]
            r0 = r27
            boolean r39 = r0.startsWith(r13)
            if (r39 == 0) goto L_0x009c
            java.io.File r15 = new java.io.File
            java.lang.StringBuilder r39 = new java.lang.StringBuilder
            r39.<init>()
            java.io.File r40 = android.os.Environment.getExternalStorageDirectory()
            java.lang.String r40 = r40.getAbsolutePath()
            java.lang.StringBuilder r39 = r39.append(r40)
            java.lang.String r40 = "/"
            r0 = r27
            r1 = r40
            java.lang.String r40 = r0.replace(r13, r1)
            java.lang.StringBuilder r39 = r39.append(r40)
            java.lang.String r39 = r39.toString()
            r0 = r39
            r15.<init>(r0)
            boolean r39 = r15.exists()
            if (r39 == 0) goto L_0x009c
            java.lang.String r37 = "UriUtils"
            java.lang.StringBuilder r38 = new java.lang.StringBuilder
            r38.<init>()
            java.lang.String r39 = r42.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " -> "
            java.lang.StringBuilder r38 = r38.append(r39)
            r0 = r38
            java.lang.StringBuilder r38 = r0.append(r13)
            java.lang.String r38 = r38.toString()
            android.util.Log.d(r37, r38)
        L_0x009b:
            return r15
        L_0x009c:
            int r37 = r37 + 1
            goto L_0x0039
        L_0x009f:
            r15 = 0
            java.lang.String r37 = "/files_path/"
            r0 = r27
            r1 = r37
            boolean r37 = r0.startsWith(r1)
            if (r37 == 0) goto L_0x010f
            java.io.File r15 = new java.io.File
            java.lang.StringBuilder r37 = new java.lang.StringBuilder
            r37.<init>()
            com.mesibo.mediapicker.ImagePicker r38 = com.mesibo.mediapicker.ImagePicker.getInstance()
            android.app.Application r38 = r38.getApp()
            java.io.File r38 = r38.getFilesDir()
            java.lang.String r38 = r38.getAbsolutePath()
            java.lang.StringBuilder r37 = r37.append(r38)
            java.lang.String r38 = "/files_path/"
            java.lang.String r39 = "/"
            r0 = r27
            r1 = r38
            r2 = r39
            java.lang.String r38 = r0.replace(r1, r2)
            java.lang.StringBuilder r37 = r37.append(r38)
            java.lang.String r37 = r37.toString()
            r0 = r37
            r15.<init>(r0)
        L_0x00e2:
            if (r15 == 0) goto L_0x01dc
            boolean r37 = r15.exists()
            if (r37 == 0) goto L_0x01dc
            java.lang.String r37 = "UriUtils"
            java.lang.StringBuilder r38 = new java.lang.StringBuilder
            r38.<init>()
            java.lang.String r39 = r42.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " -> "
            java.lang.StringBuilder r38 = r38.append(r39)
            r0 = r38
            r1 = r27
            java.lang.StringBuilder r38 = r0.append(r1)
            java.lang.String r38 = r38.toString()
            android.util.Log.d(r37, r38)
            goto L_0x009b
        L_0x010f:
            java.lang.String r37 = "/cache_path/"
            r0 = r27
            r1 = r37
            boolean r37 = r0.startsWith(r1)
            if (r37 == 0) goto L_0x0152
            java.io.File r15 = new java.io.File
            java.lang.StringBuilder r37 = new java.lang.StringBuilder
            r37.<init>()
            com.mesibo.mediapicker.ImagePicker r38 = com.mesibo.mediapicker.ImagePicker.getInstance()
            android.app.Application r38 = r38.getApp()
            java.io.File r38 = r38.getCacheDir()
            java.lang.String r38 = r38.getAbsolutePath()
            java.lang.StringBuilder r37 = r37.append(r38)
            java.lang.String r38 = "/cache_path/"
            java.lang.String r39 = "/"
            r0 = r27
            r1 = r38
            r2 = r39
            java.lang.String r38 = r0.replace(r1, r2)
            java.lang.StringBuilder r37 = r37.append(r38)
            java.lang.String r37 = r37.toString()
            r0 = r37
            r15.<init>(r0)
            goto L_0x00e2
        L_0x0152:
            java.lang.String r37 = "/external_files_path/"
            r0 = r27
            r1 = r37
            boolean r37 = r0.startsWith(r1)
            if (r37 == 0) goto L_0x0198
            java.io.File r15 = new java.io.File
            java.lang.StringBuilder r37 = new java.lang.StringBuilder
            r37.<init>()
            com.mesibo.mediapicker.ImagePicker r38 = com.mesibo.mediapicker.ImagePicker.getInstance()
            android.app.Application r38 = r38.getApp()
            r39 = 0
            java.io.File r38 = r38.getExternalFilesDir(r39)
            java.lang.String r38 = r38.getAbsolutePath()
            java.lang.StringBuilder r37 = r37.append(r38)
            java.lang.String r38 = "/external_files_path/"
            java.lang.String r39 = "/"
            r0 = r27
            r1 = r38
            r2 = r39
            java.lang.String r38 = r0.replace(r1, r2)
            java.lang.StringBuilder r37 = r37.append(r38)
            java.lang.String r37 = r37.toString()
            r0 = r37
            r15.<init>(r0)
            goto L_0x00e2
        L_0x0198:
            java.lang.String r37 = "/external_cache_path/"
            r0 = r27
            r1 = r37
            boolean r37 = r0.startsWith(r1)
            if (r37 == 0) goto L_0x00e2
            java.io.File r15 = new java.io.File
            java.lang.StringBuilder r37 = new java.lang.StringBuilder
            r37.<init>()
            com.mesibo.mediapicker.ImagePicker r38 = com.mesibo.mediapicker.ImagePicker.getInstance()
            android.app.Application r38 = r38.getApp()
            java.io.File r38 = r38.getExternalCacheDir()
            java.lang.String r38 = r38.getAbsolutePath()
            java.lang.StringBuilder r37 = r37.append(r38)
            java.lang.String r38 = "/external_cache_path/"
            java.lang.String r39 = "/"
            r0 = r27
            r1 = r38
            r2 = r39
            java.lang.String r38 = r0.replace(r1, r2)
            java.lang.StringBuilder r37 = r37.append(r38)
            java.lang.String r37 = r37.toString()
            r0 = r37
            r15.<init>(r0)
            goto L_0x00e2
        L_0x01dc:
            java.lang.String r37 = "file"
            r0 = r37
            r1 = r29
            boolean r37 = r0.equals(r1)
            if (r37 == 0) goto L_0x0212
            if (r27 == 0) goto L_0x01f3
            java.io.File r15 = new java.io.File
            r0 = r27
            r15.<init>(r0)
            goto L_0x009b
        L_0x01f3:
            java.lang.String r37 = "UriUtils"
            java.lang.StringBuilder r38 = new java.lang.StringBuilder
            r38.<init>()
            java.lang.String r39 = r42.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " parse failed. -> 0"
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r38 = r38.toString()
            android.util.Log.d(r37, r38)
            r15 = 0
            goto L_0x009b
        L_0x0212:
            int r37 = android.os.Build.VERSION.SDK_INT
            r38 = 19
            r0 = r37
            r1 = r38
            if (r0 < r1) goto L_0x05cf
            com.mesibo.mediapicker.ImagePicker r37 = com.mesibo.mediapicker.ImagePicker.getInstance()
            android.app.Application r37 = r37.getApp()
            r0 = r37
            r1 = r42
            boolean r37 = android.provider.DocumentsContract.isDocumentUri(r0, r1)
            if (r37 == 0) goto L_0x05cf
            java.lang.String r37 = "com.android.externalstorage.documents"
            r0 = r37
            boolean r37 = r0.equals(r4)
            if (r37 == 0) goto L_0x0440
            java.lang.String r10 = android.provider.DocumentsContract.getDocumentId(r42)
            java.lang.String r37 = ":"
            r0 = r37
            java.lang.String[] r32 = r10.split(r0)
            r37 = 0
            r35 = r32[r37]
            java.lang.String r37 = "primary"
            r0 = r37
            r1 = r35
            boolean r37 = r0.equalsIgnoreCase(r1)
            if (r37 == 0) goto L_0x027c
            java.io.File r15 = new java.io.File
            java.lang.StringBuilder r37 = new java.lang.StringBuilder
            r37.<init>()
            java.io.File r38 = android.os.Environment.getExternalStorageDirectory()
            java.lang.StringBuilder r37 = r37.append(r38)
            java.lang.String r38 = "/"
            java.lang.StringBuilder r37 = r37.append(r38)
            r38 = 1
            r38 = r32[r38]
            java.lang.StringBuilder r37 = r37.append(r38)
            java.lang.String r37 = r37.toString()
            r0 = r37
            r15.<init>(r0)
            goto L_0x009b
        L_0x027c:
            com.mesibo.mediapicker.ImagePicker r37 = com.mesibo.mediapicker.ImagePicker.getInstance()
            android.app.Application r37 = r37.getApp()
            java.lang.String r38 = "storage"
            java.lang.Object r25 = r37.getSystemService(r38)
            android.os.storage.StorageManager r25 = (android.os.storage.StorageManager) r25
            java.lang.String r37 = "android.os.storage.StorageVolume"
            java.lang.Class r33 = java.lang.Class.forName(r37)     // Catch:{ Exception -> 0x03f6 }
            java.lang.Class r37 = r25.getClass()     // Catch:{ Exception -> 0x03f6 }
            java.lang.String r38 = "getVolumeList"
            r39 = 0
            r0 = r39
            java.lang.Class[] r0 = new java.lang.Class[r0]     // Catch:{ Exception -> 0x03f6 }
            r39 = r0
            java.lang.reflect.Method r19 = r37.getMethod(r38, r39)     // Catch:{ Exception -> 0x03f6 }
            java.lang.String r37 = "getUuid"
            r38 = 0
            r0 = r38
            java.lang.Class[] r0 = new java.lang.Class[r0]     // Catch:{ Exception -> 0x03f6 }
            r38 = r0
            r0 = r33
            r1 = r37
            r2 = r38
            java.lang.reflect.Method r18 = r0.getMethod(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            java.lang.String r37 = "getState"
            r38 = 0
            r0 = r38
            java.lang.Class[] r0 = new java.lang.Class[r0]     // Catch:{ Exception -> 0x03f6 }
            r38 = r0
            r0 = r33
            r1 = r37
            r2 = r38
            java.lang.reflect.Method r17 = r0.getMethod(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            java.lang.String r37 = "getPath"
            r38 = 0
            r0 = r38
            java.lang.Class[] r0 = new java.lang.Class[r0]     // Catch:{ Exception -> 0x03f6 }
            r38 = r0
            r0 = r33
            r1 = r37
            r2 = r38
            java.lang.reflect.Method r16 = r0.getMethod(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            java.lang.String r37 = "isPrimary"
            r38 = 0
            r0 = r38
            java.lang.Class[] r0 = new java.lang.Class[r0]     // Catch:{ Exception -> 0x03f6 }
            r38 = r0
            r0 = r33
            r1 = r37
            r2 = r38
            java.lang.reflect.Method r23 = r0.getMethod(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            java.lang.String r37 = "isEmulated"
            r38 = 0
            r0 = r38
            java.lang.Class[] r0 = new java.lang.Class[r0]     // Catch:{ Exception -> 0x03f6 }
            r38 = r0
            r0 = r33
            r1 = r37
            r2 = r38
            java.lang.reflect.Method r22 = r0.getMethod(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            r37 = 0
            r0 = r37
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ Exception -> 0x03f6 }
            r37 = r0
            r0 = r19
            r1 = r25
            r2 = r37
            java.lang.Object r28 = r0.invoke(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            int r24 = java.lang.reflect.Array.getLength(r28)     // Catch:{ Exception -> 0x03f6 }
            r20 = 0
        L_0x0320:
            r0 = r20
            r1 = r24
            if (r0 >= r1) goto L_0x0421
            r0 = r28
            r1 = r20
            java.lang.Object r34 = java.lang.reflect.Array.get(r0, r1)     // Catch:{ Exception -> 0x03f6 }
            java.lang.String r37 = "mounted"
            r38 = 0
            r0 = r38
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ Exception -> 0x03f6 }
            r38 = r0
            r0 = r17
            r1 = r34
            r2 = r38
            java.lang.Object r38 = r0.invoke(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            boolean r37 = r37.equals(r38)     // Catch:{ Exception -> 0x03f6 }
            if (r37 != 0) goto L_0x0362
            java.lang.String r37 = "mounted_ro"
            r38 = 0
            r0 = r38
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ Exception -> 0x03f6 }
            r38 = r0
            r0 = r17
            r1 = r34
            r2 = r38
            java.lang.Object r38 = r0.invoke(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            boolean r37 = r37.equals(r38)     // Catch:{ Exception -> 0x03f6 }
            if (r37 == 0) goto L_0x0369
        L_0x0362:
            r26 = 1
        L_0x0364:
            if (r26 != 0) goto L_0x036c
        L_0x0366:
            int r20 = r20 + 1
            goto L_0x0320
        L_0x0369:
            r26 = 0
            goto L_0x0364
        L_0x036c:
            r37 = 0
            r0 = r37
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ Exception -> 0x03f6 }
            r37 = r0
            r0 = r23
            r1 = r34
            r2 = r37
            java.lang.Object r37 = r0.invoke(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            java.lang.Boolean r37 = (java.lang.Boolean) r37     // Catch:{ Exception -> 0x03f6 }
            boolean r37 = r37.booleanValue()     // Catch:{ Exception -> 0x03f6 }
            if (r37 == 0) goto L_0x03a0
            r37 = 0
            r0 = r37
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ Exception -> 0x03f6 }
            r37 = r0
            r0 = r22
            r1 = r34
            r2 = r37
            java.lang.Object r37 = r0.invoke(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            java.lang.Boolean r37 = (java.lang.Boolean) r37     // Catch:{ Exception -> 0x03f6 }
            boolean r37 = r37.booleanValue()     // Catch:{ Exception -> 0x03f6 }
            if (r37 != 0) goto L_0x0366
        L_0x03a0:
            r37 = 0
            r0 = r37
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ Exception -> 0x03f6 }
            r37 = r0
            r0 = r18
            r1 = r34
            r2 = r37
            java.lang.Object r36 = r0.invoke(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            java.lang.String r36 = (java.lang.String) r36     // Catch:{ Exception -> 0x03f6 }
            if (r36 == 0) goto L_0x0366
            r0 = r36
            r1 = r35
            boolean r37 = r0.equals(r1)     // Catch:{ Exception -> 0x03f6 }
            if (r37 == 0) goto L_0x0366
            java.io.File r15 = new java.io.File     // Catch:{ Exception -> 0x03f6 }
            java.lang.StringBuilder r37 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x03f6 }
            r37.<init>()     // Catch:{ Exception -> 0x03f6 }
            r38 = 0
            r0 = r38
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ Exception -> 0x03f6 }
            r38 = r0
            r0 = r16
            r1 = r34
            r2 = r38
            java.lang.Object r38 = r0.invoke(r1, r2)     // Catch:{ Exception -> 0x03f6 }
            java.lang.StringBuilder r37 = r37.append(r38)     // Catch:{ Exception -> 0x03f6 }
            java.lang.String r38 = "/"
            java.lang.StringBuilder r37 = r37.append(r38)     // Catch:{ Exception -> 0x03f6 }
            r38 = 1
            r38 = r32[r38]     // Catch:{ Exception -> 0x03f6 }
            java.lang.StringBuilder r37 = r37.append(r38)     // Catch:{ Exception -> 0x03f6 }
            java.lang.String r37 = r37.toString()     // Catch:{ Exception -> 0x03f6 }
            r0 = r37
            r15.<init>(r0)     // Catch:{ Exception -> 0x03f6 }
            goto L_0x009b
        L_0x03f6:
            r12 = move-exception
            java.lang.String r37 = "UriUtils"
            java.lang.StringBuilder r38 = new java.lang.StringBuilder
            r38.<init>()
            java.lang.String r39 = r42.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " parse failed. "
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = r12.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " -> 1_0"
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r38 = r38.toString()
            android.util.Log.d(r37, r38)
        L_0x0421:
            java.lang.String r37 = "UriUtils"
            java.lang.StringBuilder r38 = new java.lang.StringBuilder
            r38.<init>()
            java.lang.String r39 = r42.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " parse failed. -> 1_0"
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r38 = r38.toString()
            android.util.Log.d(r37, r38)
            r15 = 0
            goto L_0x009b
        L_0x0440:
            java.lang.String r37 = "com.android.providers.downloads.documents"
            r0 = r37
            boolean r37 = r0.equals(r4)
            if (r37 == 0) goto L_0x0511
            java.lang.String r21 = android.provider.DocumentsContract.getDocumentId(r42)
            boolean r37 = android.text.TextUtils.isEmpty(r21)
            if (r37 == 0) goto L_0x0473
            java.lang.String r37 = "UriUtils"
            java.lang.StringBuilder r38 = new java.lang.StringBuilder
            r38.<init>()
            java.lang.String r39 = r42.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " parse failed(id is null). -> 1_1"
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r38 = r38.toString()
            android.util.Log.d(r37, r38)
            r15 = 0
            goto L_0x009b
        L_0x0473:
            java.lang.String r37 = "raw:"
            r0 = r21
            r1 = r37
            boolean r37 = r0.startsWith(r1)
            if (r37 == 0) goto L_0x0492
            java.io.File r15 = new java.io.File
            r37 = 4
            r0 = r21
            r1 = r37
            java.lang.String r37 = r0.substring(r1)
            r0 = r37
            r15.<init>(r0)
            goto L_0x009b
        L_0x0492:
            java.lang.String r37 = "msf:"
            r0 = r21
            r1 = r37
            boolean r37 = r0.startsWith(r1)
            if (r37 == 0) goto L_0x04ac
            java.lang.String r37 = ":"
            r0 = r21
            r1 = r37
            java.lang.String[] r37 = r0.split(r1)
            r38 = 1
            r21 = r37[r38]
        L_0x04ac:
            r6 = 0
            long r6 = java.lang.Long.parseLong(r21)     // Catch:{ Exception -> 0x04ee }
            r37 = 3
            r0 = r37
            java.lang.String[] r9 = new java.lang.String[r0]
            r37 = 0
            java.lang.String r38 = "content://downloads/public_downloads"
            r9[r37] = r38
            r37 = 1
            java.lang.String r38 = "content://downloads/all_downloads"
            r9[r37] = r38
            r37 = 2
            java.lang.String r38 = "content://downloads/my_downloads"
            r9[r37] = r38
            int r0 = r9.length
            r38 = r0
            r37 = 0
        L_0x04cf:
            r0 = r37
            r1 = r38
            if (r0 >= r1) goto L_0x04f2
            r8 = r9[r37]
            android.net.Uri r39 = android.net.Uri.parse(r8)
            r0 = r39
            android.net.Uri r5 = android.content.ContentUris.withAppendedId(r0, r6)
            java.lang.String r39 = "1_1"
            r0 = r39
            java.io.File r15 = getFileFromUri(r5, r0)     // Catch:{ Exception -> 0x0606 }
            if (r15 != 0) goto L_0x009b
        L_0x04eb:
            int r37 = r37 + 1
            goto L_0x04cf
        L_0x04ee:
            r11 = move-exception
            r15 = 0
            goto L_0x009b
        L_0x04f2:
            java.lang.String r37 = "UriUtils"
            java.lang.StringBuilder r38 = new java.lang.StringBuilder
            r38.<init>()
            java.lang.String r39 = r42.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " parse failed. -> 1_1"
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r38 = r38.toString()
            android.util.Log.d(r37, r38)
            r15 = 0
            goto L_0x009b
        L_0x0511:
            java.lang.String r37 = "com.android.providers.media.documents"
            r0 = r37
            boolean r37 = r0.equals(r4)
            if (r37 == 0) goto L_0x0598
            java.lang.String r10 = android.provider.DocumentsContract.getDocumentId(r42)
            java.lang.String r37 = ":"
            r0 = r37
            java.lang.String[] r32 = r10.split(r0)
            r37 = 0
            r35 = r32[r37]
            java.lang.String r37 = "image"
            r0 = r37
            r1 = r35
            boolean r37 = r0.equals(r1)
            if (r37 == 0) goto L_0x055b
            android.net.Uri r5 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        L_0x0539:
            java.lang.String r30 = "_id=?"
            r37 = 1
            r0 = r37
            java.lang.String[] r0 = new java.lang.String[r0]
            r31 = r0
            r37 = 0
            r38 = 1
            r38 = r32[r38]
            r31[r37] = r38
            java.lang.String r37 = "_id=?"
            java.lang.String r38 = "1_2"
            r0 = r37
            r1 = r31
            r2 = r38
            java.io.File r15 = getFileFromUri(r5, r0, r1, r2)
            goto L_0x009b
        L_0x055b:
            java.lang.String r37 = "video"
            r0 = r37
            r1 = r35
            boolean r37 = r0.equals(r1)
            if (r37 == 0) goto L_0x056a
            android.net.Uri r5 = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            goto L_0x0539
        L_0x056a:
            java.lang.String r37 = "audio"
            r0 = r37
            r1 = r35
            boolean r37 = r0.equals(r1)
            if (r37 == 0) goto L_0x0579
            android.net.Uri r5 = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            goto L_0x0539
        L_0x0579:
            java.lang.String r37 = "UriUtils"
            java.lang.StringBuilder r38 = new java.lang.StringBuilder
            r38.<init>()
            java.lang.String r39 = r42.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " parse failed. -> 1_2"
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r38 = r38.toString()
            android.util.Log.d(r37, r38)
            r15 = 0
            goto L_0x009b
        L_0x0598:
            java.lang.String r37 = "content"
            r0 = r37
            r1 = r29
            boolean r37 = r0.equals(r1)
            if (r37 == 0) goto L_0x05b0
            java.lang.String r37 = "1_3"
            r0 = r42
            r1 = r37
            java.io.File r15 = getFileFromUri(r0, r1)
            goto L_0x009b
        L_0x05b0:
            java.lang.String r37 = "UriUtils"
            java.lang.StringBuilder r38 = new java.lang.StringBuilder
            r38.<init>()
            java.lang.String r39 = r42.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " parse failed. -> 1_4"
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r38 = r38.toString()
            android.util.Log.d(r37, r38)
            r15 = 0
            goto L_0x009b
        L_0x05cf:
            java.lang.String r37 = "content"
            r0 = r37
            r1 = r29
            boolean r37 = r0.equals(r1)
            if (r37 == 0) goto L_0x05e7
            java.lang.String r37 = "2"
            r0 = r42
            r1 = r37
            java.io.File r15 = getFileFromUri(r0, r1)
            goto L_0x009b
        L_0x05e7:
            java.lang.String r37 = "UriUtils"
            java.lang.StringBuilder r38 = new java.lang.StringBuilder
            r38.<init>()
            java.lang.String r39 = r42.toString()
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r39 = " parse failed. -> 3"
            java.lang.StringBuilder r38 = r38.append(r39)
            java.lang.String r38 = r38.toString()
            android.util.Log.d(r37, r38)
            r15 = 0
            goto L_0x009b
        L_0x0606:
            r39 = move-exception
            goto L_0x04eb
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mesibo.mediapicker.UriUtils.uri2FileReal(android.content.Context, android.net.Uri):java.io.File");
    }

    private static File getFileFromUri(Uri uri, String code) {
        return getFileFromUri(uri, (String) null, (String[]) null, code);
    }

    private static File getFileFromUri(Uri uri, String selection, String[] selectionArgs, String code) {
        if ("com.google.android.apps.photos.content".equals(uri.getAuthority())) {
            if (!TextUtils.isEmpty(uri.getLastPathSegment())) {
                return new File(uri.getLastPathSegment());
            }
        } else if ("com.tencent.mtt.fileprovider".equals(uri.getAuthority())) {
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                return new File(Environment.getExternalStorageDirectory(), path.substring("/QQBrowser".length(), path.length()));
            }
        } else if ("com.huawei.hidisk.fileprovider".equals(uri.getAuthority())) {
            String path2 = uri.getPath();
            if (!TextUtils.isEmpty(path2)) {
                return new File(path2.replace("/root", ""));
            }
        }
        Cursor cursor = ImagePicker.getInstance().getApp().getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, (String) null);
        if (cursor == null) {
            Log.d("UriUtils", uri.toString() + " parse failed(cursor is null). -> " + code);
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("_data");
                if (columnIndex > -1) {
                    File file = new File(cursor.getString(columnIndex));
                    cursor.close();
                    return file;
                }
                Log.d("UriUtils", uri.toString() + " parse failed(columnIndex: " + columnIndex + " is wrong). -> " + code);
                return null;
            }
            Log.d("UriUtils", uri.toString() + " parse failed(moveToFirst return false). -> " + code);
            cursor.close();
            return null;
        } catch (Exception e) {
            Log.d("UriUtils", uri.toString() + " parse failed. -> " + code);
            return null;
        } finally {
            cursor.close();
        }
    }

    private static File copyUri2Cache(Uri uri, String ext) {
        File file;
        String ext2;
        Log.d("UriUtils", "copyUri2Cache() called");
        InputStream is = null;
        try {
            is = ImagePicker.getInstance().getApp().getContentResolver().openInputStream(uri);
            if (TextUtils.isEmpty(ext)) {
                ext2 = "";
            } else {
                ext2 = FileUtils.HIDDEN_PREFIX + ext;
            }
            file = new File(ImagePicker.getInstance().getApp().getCacheDir(), "" + System.currentTimeMillis() + ext2);
            writeFileFromIS(new File(file.getAbsolutePath()), is, false);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            file = null;
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
        return file;
    }

    public static boolean createOrExistsDir(File file) {
        return file != null && (!file.exists() ? file.mkdirs() : file.isDirectory());
    }

    public static boolean createOrExistsFile(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return file.isFile();
        }
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004f A[SYNTHETIC, Splitter:B:21:0x004f] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0077 A[SYNTHETIC, Splitter:B:41:0x0077] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean writeFileFromIS(File r9, InputStream r10, boolean r11) {
        /*
            r5 = 0
            if (r10 == 0) goto L_0x0009
            boolean r6 = createOrExistsFile(r9)
            if (r6 != 0) goto L_0x0028
        L_0x0009:
            java.lang.String r6 = "FileIOUtils"
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "create file <"
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.StringBuilder r7 = r7.append(r9)
            java.lang.String r8 = "> failed."
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            android.util.Log.e(r6, r7)
        L_0x0027:
            return r5
        L_0x0028:
            r3 = 0
            java.io.BufferedOutputStream r4 = new java.io.BufferedOutputStream     // Catch:{ IOException -> 0x0088 }
            java.io.FileOutputStream r6 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x0088 }
            r6.<init>(r9, r11)     // Catch:{ IOException -> 0x0088 }
            int r7 = sBufferSize     // Catch:{ IOException -> 0x0088 }
            r4.<init>(r6, r7)     // Catch:{ IOException -> 0x0088 }
            int r6 = sBufferSize     // Catch:{ IOException -> 0x0045, all -> 0x0085 }
            byte[] r0 = new byte[r6]     // Catch:{ IOException -> 0x0045, all -> 0x0085 }
        L_0x0039:
            int r2 = r10.read(r0)     // Catch:{ IOException -> 0x0045, all -> 0x0085 }
            r6 = -1
            if (r2 == r6) goto L_0x0058
            r6 = 0
            r4.write(r0, r6, r2)     // Catch:{ IOException -> 0x0045, all -> 0x0085 }
            goto L_0x0039
        L_0x0045:
            r1 = move-exception
            r3 = r4
        L_0x0047:
            r1.printStackTrace()     // Catch:{ all -> 0x0071 }
            r10.close()     // Catch:{ IOException -> 0x006c }
        L_0x004d:
            if (r3 == 0) goto L_0x0027
            r3.close()     // Catch:{ IOException -> 0x0053 }
            goto L_0x0027
        L_0x0053:
            r1 = move-exception
            r1.printStackTrace()
            goto L_0x0027
        L_0x0058:
            r5 = 1
            r10.close()     // Catch:{ IOException -> 0x0067 }
        L_0x005c:
            if (r4 == 0) goto L_0x0027
            r4.close()     // Catch:{ IOException -> 0x0062 }
            goto L_0x0027
        L_0x0062:
            r1 = move-exception
            r1.printStackTrace()
            goto L_0x0027
        L_0x0067:
            r1 = move-exception
            r1.printStackTrace()
            goto L_0x005c
        L_0x006c:
            r1 = move-exception
            r1.printStackTrace()
            goto L_0x004d
        L_0x0071:
            r5 = move-exception
        L_0x0072:
            r10.close()     // Catch:{ IOException -> 0x007b }
        L_0x0075:
            if (r3 == 0) goto L_0x007a
            r3.close()     // Catch:{ IOException -> 0x0080 }
        L_0x007a:
            throw r5
        L_0x007b:
            r1 = move-exception
            r1.printStackTrace()
            goto L_0x0075
        L_0x0080:
            r1 = move-exception
            r1.printStackTrace()
            goto L_0x007a
        L_0x0085:
            r5 = move-exception
            r3 = r4
            goto L_0x0072
        L_0x0088:
            r1 = move-exception
            goto L_0x0047
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mesibo.mediapicker.UriUtils.writeFileFromIS(java.io.File, java.io.InputStream, boolean):boolean");
    }
}
