package com.qamp.app.sources;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboProfile;
import com.qamp.app.R;
import com.qamp.app.sources.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Random;


public class ImageEditor extends AppCompatActivity {
    public static final int CANCEL = -1;
    public static final int SUCCESS = 0;
    EditText mCaptionEditText = null;
    RelativeLayout mCaptionView = null;

    private static final int MAX_IMAGE_SIZE = 1024; // Maximum size in kilobytes
    boolean mCropMode = false;
    private Rect mCropRect = new Rect();
    int mDisplayHeght = 0;
    int mDisplayWidth = 0;
    int mExifRotation = 0;
    String mFilePath = null;
    String peer = null;
    Long gruopId = null;
    int mFileType = -1;
    boolean mForceShowCropOverlay = false;
    Bitmap mImage = null;
    CropImageView mImageView = null;
    /* access modifiers changed from: private */
    public MediaPicker.ImageEditorListener mListener = null;
    int mRotation = 0;
    ScalingUtilities.Result mScaleResult = new ScalingUtilities.Result();
    ImageButton mSendBtn = null;
    boolean mShowEditMenu = false;
    boolean mShowTitle = false;
    boolean mShowToolbar = true;
    boolean mSquareCrop = false;

    String userID = null;
    String userRole = null;
    int maxDimension = 1280;

    /* JADX WARNING: type inference failed for: r14v0, types: [android.content.Context, com.mesibo.mediapicker.ImageEditor, androidx.appcompat.app.AppCompatActivity] */
    /* access modifiers changed from: protected */
    @SuppressLint("ResourceAsColor")
    public void onCreate(Bundle savedInstanceState) {
        ImageEditor.super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_caption);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(R.color.transparent_color));
        Intent intent = getIntent();
        this.mFilePath = intent.getStringExtra("filepath");
        this.maxDimension = intent.getIntExtra("maxDimension", 1280);
        this.mShowEditMenu = intent.getBooleanExtra("showEditControls", false);
        this.mShowTitle = intent.getBooleanExtra("showTitle", true);
        this.mForceShowCropOverlay = intent.getBooleanExtra("showCrop", false);
        this.mSquareCrop = intent.getBooleanExtra("squareCrop", false);
        this.peer = intent.getStringExtra("peer");
        this.gruopId = intent.getLongExtra("groupId",0);
        String title = intent.getStringExtra("title");
        if (!TextUtils.isEmpty(title)) {
            actionBar.setTitle(title);
        }
        if (!this.mShowEditMenu) {
            actionBar.hide();
        }
        this.mFileType = intent.getIntExtra("type", -1);
        this.mListener = MediaPicker.getImageEditorListener();
        this.mSendBtn = (ImageButton) findViewById(R.id.caption_send);
        this.mCaptionEditText = (EditText) findViewById(R.id.caption_edit);
        this.mCaptionView = (RelativeLayout) findViewById(R.id.caption_view);
        if (!this.mShowTitle) {
            this.mCaptionEditText.setVisibility(4);
        }
        this.mImageView = (CropImageView) findViewById(R.id.caption_image);
        this.mExifRotation = SocialUtilities.getExifRotation(this.mFilePath);
        int deviceRotation = SocialUtilities.getDeviceRotation(this);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int i = displaymetrics.heightPixels;
        int i2 = displaymetrics.widthPixels;
        new Matrix();
        if (this.mExifRotation != deviceRotation) {
        }

        if (MediaPicker.TYPE_CAMERAIMAGE != this.mFileType && MediaPicker.TYPE_FILEIMAGE != this.mFileType) {
            if (MediaPicker.TYPE_CAMERAVIDEO != this.mFileType && MediaPicker.TYPE_FILEVIDEO != this.mFileType) {
                int drawableid = intent.getIntExtra("drawableid", -1);
                if (drawableid >= 0) {
                    this.mImage = BitmapFactory.decodeResource(this.getApplicationContext().getResources(), drawableid);
                }
                Log.e("check","100");
                this.mShowEditMenu = false;
                this.mForceShowCropOverlay = false;
            } else {
                Log.e("check","200");
                this.mImage = ThumbnailUtils.createVideoThumbnail(this.mFilePath, 1);
                this.mShowEditMenu = false;
                this.mForceShowCropOverlay = false;
            }
        } else {
            Log.e("check","300");
            this.loadImage();
        }

        if (this.mImage == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Not an image or unable to show preview", 0);
            toast.setMargin(50.0f, 50.0f);
            toast.show();
            finish();
            return;
        }else {
            setImage(this.mImage);
            if (this.mSquareCrop) {
                this.mImageView.setAspectRatio(1, 1);
            } else {
                this.mImageView.clearAspectRatio();
            }
            this.mImageView.setShowCropOverlay(this.mForceShowCropOverlay);
            if (this.mSendBtn != null) {
                this.mSendBtn.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {



                        if (ImageEditor.this.mImageView.isShowCropOverlay()) {
                            ImageEditor.this.updateCropRect(ImageEditor.this.mImageView.getCropRect());
                           ImageEditor.this.loadImage();
                            Log.e("intent","11");
                        }

                        String caption = ImageEditor.this.mCaptionEditText.getText().toString();
                        if (null != ImageEditor.this.mListener) {
                            ImageEditor.this.mListener.onImageEdit(ImageEditor.this.mFileType, caption, ImageEditor.this.mFilePath, ImageEditor.this.mImage, 0);
                            ImageEditor.this.finish();
                            Log.e("intent","22");
                        } else {
                            if (null != ImageEditor.this.mImage) {
                                mImage.recycle();
                                Log.e("intent","33");
                            }
                            Log.e("intent","44");
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("filepath", ImageEditor.this.mFilePath);
                            returnIntent.putExtra("message", caption);
                            ImageEditor.this.setResult(-1, returnIntent);
                            ImageEditor.this.finish();
                        }
                    }
                });
            }
            hideKeyboard();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if (!this.mShowEditMenu) {
            return true;
        }
        menuInflater.inflate(com.mesibo.messaging.R.menu.add_image_caption_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == com.mesibo.messaging.R.id.action_crop) {
            TextView txt_crop = findViewById(R.id.txt_crop_done);
            txt_crop.setVisibility(View.VISIBLE);
            txt_crop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    txt_crop.setVisibility(View.GONE);
                    cropBitmap(true);
                }
            });
            cropBitmap(true);
        } else if (item.getItemId() == com.mesibo.messaging.R.id.action_rotate) {
            this.mImage = rotateBitmap(this.mImage, 90);
            setImage(this.mImage);
            this.mRotation += 90;
            if (this.mRotation != 360) {
                return true;
            }
            this.mRotation = 0;
            return true;
        } else if (item.getItemId() == 16908332) {
            onBackPressed();
            return true;
        }
        return ImageEditor.super.onOptionsItemSelected(item);
    }

    public int adjustAngle(int angle) {
        while (angle < 0) {
            angle += 360;
        }
        while (angle >= 360) {
            angle -= 360;
        }
        if (angle == 0 || angle == 90 || angle == 270) {
            return angle;
        }
        return angle - (angle % 90);
    }

    public Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void loadImage() {
        this.mScaleResult = new ScalingUtilities.Result();
        this.mImage = ScalingUtilities.scale(this.mFilePath, this.mCropRect, this.maxDimension, ScalingUtilities.ScalingLogic.FIT, this.mScaleResult);
        if (this.mImage != null) {
            if (this.mCropRect.isEmpty()) {
                this.mCropRect.left = 0;
                this.mCropRect.top = 0;
                this.mCropRect.bottom = this.mScaleResult.origHeight;
                this.mCropRect.right = this.mScaleResult.origWidth;
            }
            int angle = adjustAngle(this.mExifRotation + this.mRotation);
            if (angle > 0) {
                this.mImage = rotateBitmap(this.mImage, angle);
            }
            this.mDisplayWidth = this.mImage.getWidth();
            this.mDisplayHeght = this.mImage.getHeight();


        }

    }




    public Uri getImageUri(Context inContext, Bitmap inImage) {
        String uniqueTitle = "image_" + System.currentTimeMillis(); // Generate a unique title
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, uniqueTitle, null);
        return Uri.parse(path);
    }

    public static Bitmap compressAndCropImage(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        try {
            // Calculate the dimensions of the final image while maintaining aspect ratio
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();

            if (width > maxWidth || height > maxHeight) {
                float aspectRatio = (float) width / (float) height;
                if (width > height) {
                    width = maxWidth;
                    height = (int) (width / aspectRatio);
                } else {
                    height = maxHeight;
                    width = (int) (height * aspectRatio);
                }
            }

            // Crop the image to the calculated dimensions
            Bitmap croppedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);

            // Compress the cropped image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int quality = 60; // Adjust the quality as needed
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

            // Create a new Bitmap from the compressed data
            byte[] compressedData = outputStream.toByteArray();
            return BitmapFactory.decodeByteArray(compressedData, 0, compressedData.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }





    public static String getRandomString(int length) {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(allowedChars.length());
            char randomChar = allowedChars.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }



    public void updateCropRect(Rect r) {
        int angle = adjustAngle(this.mExifRotation + this.mRotation);
        if (angle > 0) {
            rotateRectangle(this.mDisplayWidth, this.mDisplayHeght, r, 0 - angle);
        }
        Rect rect = this.mCropRect;
        rect.left = (int) (((float) rect.left) + (((float) r.left) * this.mScaleResult.scale));
        Rect rect2 = this.mCropRect;
        rect2.top = (int) (((float) rect2.top) + (((float) r.top) * this.mScaleResult.scale));
        this.mCropRect.right = this.mCropRect.left + ((int) (((float) (r.right - r.left)) * this.mScaleResult.scale));
        this.mCropRect.bottom = this.mCropRect.top + ((int) (((float) (r.bottom - r.top)) * this.mScaleResult.scale));
    }

    public void rotateRectangle(int x, int y, Rect rect, int angle) {
        int t;
        int l;
        int r;
        int b;
        int angle2 = adjustAngle(angle);
        if (angle2 != 0) {
            int w = rect.right - rect.left;
            int h = rect.bottom - rect.top;
            if (90 == angle2) {
                t = rect.left;
                r = y - rect.top;
                l = r - h;
                b = t + w;
            } else if (180 == angle2) {
                t = y - rect.bottom;
                r = x - rect.left;
                l = r - w;
                b = t + h;
            } else {
                t = x - rect.right;
                l = rect.top;
                r = l + h;
                b = t + w;
            }
            rect.right = r;
            rect.left = l;
            rect.top = t;
            rect.bottom = b;
        }
    }

    public void setImage(Bitmap bmp) {
        this.mImage = bmp;
        this.mImageView.setImageBitmap(bmp);
        this.mDisplayWidth = this.mImage.getWidth();
        this.mDisplayHeght = this.mImage.getHeight();
    }

    public void hideKeyboard() {
        ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(this.mCaptionEditText.getWindowToken(), 0);
    }

    public void cropBitmap(boolean crop) {
        int i = 8;
        this.mCropMode = false;
        if (this.mImageView.isShowCropOverlay()) {
            this.mCropMode = true;
        }
        if (this.mShowTitle) {
            this.mCaptionEditText.setVisibility(!this.mCropMode ? 8 : 0);
        }
        RelativeLayout relativeLayout = this.mCaptionView;
        if (this.mCropMode) {
            i = 0;
        }
        relativeLayout.setVisibility(i);
        if (this.mCropMode) {
            if (crop) {
                updateCropRect(this.mImageView.getCropRect());
                loadImage();
                setImage(this.mImage);
            }
            this.mCropMode = false;
        } else {
            this.mCropMode = true;
            hideKeyboard();
        }
        this.mImageView.setShowCropOverlay(this.mCropMode);
    }

    public void onBackPressed() {
        if (this.mForceShowCropOverlay || !this.mImageView.isShowCropOverlay()) {
            if (this.mImage != null) {
                this.mImage.recycle();
            }
            if (this.mListener != null) {
                this.mListener.onImageEdit(this.mFileType, (String) null, this.mFilePath, (Bitmap) null, -1);
                finish();
                return;
            }
            ImageEditor.super.onBackPressed();
            return;
        }
        cropBitmap(false);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        ImageEditor.super.onResume();
    }
}
