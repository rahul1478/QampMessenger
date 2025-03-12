package com.qamp.app.sources;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.List;

public class ImagePicker {
    private static ImagePicker instance = null;
    private static List<AlbumListData> mAlbumList = null;
    /* access modifiers changed from: private */
    public static Activity parentActivity = null;
    Intent FirstIntent;
    String TAG = "FacebookLogin";
    File cameraFile;
    SharedPreferences.Editor editor;

    /* renamed from: im */
    ImageView f0im;
    Boolean login = false;
    Application mApp = null;
    TextView mUserNameTextView;
    SharedPreferences sharedPreferences;

    private ImagePicker() {
    }

    public static ImagePicker getInstance(Activity mContext) {
        if (instance == null) {
            instance = new ImagePicker();
        }
        parentActivity = mContext;
        return instance;
    }

    public static ImagePicker getInstance() {
        if (instance == null) {
            instance = new ImagePicker();
        }
        return instance;
    }

    private void LoadImageOptions(ImagePicker im) {
        final CharSequence[] items = {"Take Photo", "From Gallery", "From Facebook", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setTitle("Change Profile Picture?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    ImagePicker.this.imageCapture(ImagePicker.parentActivity, (String) null);
                } else if (items[item].equals("From Gallery")) {
                    ImagePicker.this.imageFromGallery(ImagePicker.parentActivity);
                } else if (!items[item].equals("From Facebook") && items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /* access modifiers changed from: private */
    public void imageCapture(Activity activity, String tempPath) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (tempPath == null) {
            tempPath = MediaPicker.getPath();
        }
        this.cameraFile = new File(tempPath + File.separator + "camera-" + new Date().getTime() + ".jpg");
        if (Build.VERSION.SDK_INT < 24) {
            intent.putExtra("output", Uri.fromFile(this.cameraFile));
        } else {
            intent.putExtra("output", MediaPicker.getUri(activity.getApplicationContext(), this.cameraFile));
        }
        intent.addFlags(1);
        intent.addFlags(2);
        activity.startActivityForResult(intent, MediaPicker.TYPE_CAMERAIMAGE);
    }

    private void videoCapture(Activity activity, String tempPath) {
        Intent intent = new Intent("android.media.action.VIDEO_CAPTURE");
        if (tempPath == null) {
            tempPath = MediaPicker.getPath();
        }
        this.cameraFile = new File(tempPath + File.separator + "video-" + new Date().getTime() + ".mp4");
        if (Build.VERSION.SDK_INT < 24) {
            intent.putExtra("output", Uri.fromFile(this.cameraFile));
        } else {
            intent.putExtra("output", MediaPicker.getUri(activity.getApplicationContext(), this.cameraFile));
        }
        intent.addFlags(1);
        intent.addFlags(2);
        intent.putExtra("android.intent.extra.videoQuality", 1);
        activity.startActivityForResult(intent, MediaPicker.TYPE_CAMERAVIDEO);
    }

    /* access modifiers changed from: private */
    private void videoFromGallery(Activity activity) {
        Intent intent = new Intent("android.intent.action.PICK", MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType(FileUtils.MIME_TYPE_VIDEO);
        activity.startActivityForResult(intent, MediaPicker.TYPE_FILEVIDEO);
    }

    public void imageFromGallery(Activity activity) {
        Intent intent = new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType(FileUtils.MIME_TYPE_IMAGE);
        activity.startActivityForResult(Intent.createChooser(intent, "Select an Image"), MediaPicker.TYPE_FILEIMAGE);
    }

    public void selectFiles(Activity activity, int fileType, String filter) {
        Intent intent_upload = new Intent();
        intent_upload.setType(filter);
        intent_upload.addCategory("android.intent.category.OPENABLE");
        intent_upload.setAction("android.intent.action.GET_CONTENT");
        activity.startActivityForResult(intent_upload, fileType);
    }

    public void pick(Activity activity, int fileType, String tempPath, String filter) {
        if (MediaPicker.TYPE_CAMERAIMAGE == fileType) {
            imageCapture(activity, tempPath);
        } else if (MediaPicker.TYPE_FILEIMAGE == fileType) {
            imageFromGallery(activity);
        } else if (MediaPicker.TYPE_CAMERAVIDEO == fileType) {
            videoCapture(activity, tempPath);
        } else if (MediaPicker.TYPE_FILEVIDEO == fileType) {
            videoFromGallery(activity);
        } else if (MediaPicker.TYPE_AUDIO == fileType) {
            selectFiles(activity, MediaPicker.TYPE_AUDIO, FileUtils.MIME_TYPE_AUDIO);
        } else if (MediaPicker.TYPE_FILE == fileType) {
            selectFiles(activity, MediaPicker.TYPE_FILE, "*/*");
        } else if (MediaPicker.TYPE_CUSTOM == fileType && filter != null) {
            selectFiles(activity, MediaPicker.TYPE_FILE, filter);
        }
    }

    public void pick(Activity activity, int fileType, String tempPath) {
        pick(activity, fileType, tempPath, (String) null);
    }

    public void pick(Activity activity, int fileType) {
        pick(activity, fileType, (String) null);
    }

    public String getAbsolutePath(Activity activity, Uri uri) {
        Cursor cursor = activity.managedQuery(uri, new String[]{"_data"}, (String) null, (String[]) null, (String) null);
        if (cursor == null) {
            return null;
        }
        int column_index = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public Bitmap decodeFile(String path) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            int scale = 1;
            while ((o.outWidth / scale) / 2 >= 200 && (o.outHeight / scale) / 2 >= 200) {
                scale *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeFile(path, o2);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public String processOnActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (resultCode != -1) {
            return null;
        }
        String actualFilePAth = null;
        if (requestCode == MediaPicker.TYPE_FILEIMAGE) {
            Uri imgUri = data.getData();
            Cursor cursor = null;
            if (cursor == null) {
                actualFilePAth = FileUtils.getPath(activity, imgUri);
            } else {
                cursor.moveToFirst();
                actualFilePAth = cursor.getString(0);
            }
        } else if (requestCode == MediaPicker.TYPE_CAMERAIMAGE) {
            actualFilePAth = this.cameraFile.getPath();
        } else if (requestCode == MediaPicker.TYPE_FACEBOOK) {
            actualFilePAth = data.getStringExtra("PATH");
        } else if (requestCode == MediaPicker.TYPE_CAMERAVIDEO) {
            if (data == null) {
                actualFilePAth = this.cameraFile.getPath();
            } else {
                actualFilePAth = FileUtils.getPath(activity, data.getData());
            }
        } else if (requestCode == MediaPicker.TYPE_FILEVIDEO) {
            actualFilePAth = FileUtils.getPath(activity, data.getData());
        } else if (requestCode == MediaPicker.TYPE_FILE || requestCode == MediaPicker.TYPE_AUDIO || requestCode == MediaPicker.TYPE_CUSTOM) {
            Uri uri = data.getData();
            String authority = uri.getAuthority();
            actualFilePAth = FileUtils.getPath(activity, uri);
        }
        if (actualFilePAth != null) {
            return actualFilePAth;
        }
        Toast.makeText(activity, "Unable to open file", 0).show();
        return actualFilePAth;
    }

    public String getVideoPath(Activity activity, Uri uri) {
        Cursor cursor = activity.getContentResolver().query(uri, new String[]{"_data"}, (String) null, (String[]) null, (String) null);
        if (cursor == null) {
            return null;
        }
        int column_index = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void setApp(Application app) {
        this.mApp = app;
    }

    public Application getApp() {
        return this.mApp;
    }
}
