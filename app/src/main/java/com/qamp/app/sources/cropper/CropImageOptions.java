package com.qamp.app.sources.cropper;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;


public class CropImageOptions implements Parcelable {
    public static final Creator<CropImageOptions> CREATOR = new Creator<CropImageOptions>() {
        public CropImageOptions createFromParcel(Parcel in) {
            return new CropImageOptions(in);
        }

        public CropImageOptions[] newArray(int size) {
            return new CropImageOptions[size];
        }
    };
    public int activityMenuIconColor;
    public CharSequence activityTitle;
    public boolean allowCounterRotation;
    public boolean allowFlipping;
    public boolean allowRotation;
    public int aspectRatioX;
    public int aspectRatioY;
    public boolean autoZoomEnabled;
    public int backgroundColor;
    public int borderCornerColor;
    public float borderCornerLength;
    public float borderCornerOffset;
    public float borderCornerThickness;
    public int borderLineColor;
    public float borderLineThickness;
    public int cropMenuCropButtonIcon;
    public CharSequence cropMenuCropButtonTitle;
    public CropImageView.CropShape cropShape;
    public boolean fixAspectRatio;
    public boolean flipHorizontally;
    public boolean flipVertically;
    public CropImageView.Guidelines guidelines;
    public int guidelinesColor;
    public float guidelinesThickness;
    public float initialCropWindowPaddingRatio;
    public Rect initialCropWindowRectangle;
    public int initialRotation;
    public int maxCropResultHeight;
    public int maxCropResultWidth;
    public int maxZoom;
    public int minCropResultHeight;
    public int minCropResultWidth;
    public int minCropWindowHeight;
    public int minCropWindowWidth;
    public boolean multiTouchEnabled;
    public boolean noOutputImage;
    public Bitmap.CompressFormat outputCompressFormat;
    public int outputCompressQuality;
    public int outputRequestHeight;
    public CropImageView.RequestSizeOptions outputRequestSizeOptions;
    public int outputRequestWidth;
    public Uri outputUri;
    public int rotationDegrees;
    public CropImageView.ScaleType scaleType;
    public boolean showCropOverlay;
    public boolean showProgressBar;
    public float snapRadius;
    public float touchRadius;

    public CropImageOptions() {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        this.cropShape = CropImageView.CropShape.RECTANGLE;
        this.snapRadius = TypedValue.applyDimension(1, 3.0f, dm);
        this.touchRadius = TypedValue.applyDimension(1, 24.0f, dm);
        this.guidelines = CropImageView.Guidelines.ON_TOUCH;
        this.scaleType = CropImageView.ScaleType.FIT_CENTER;
        this.showCropOverlay = true;
        this.showProgressBar = true;
        this.autoZoomEnabled = true;
        this.multiTouchEnabled = false;
        this.maxZoom = 4;
        this.initialCropWindowPaddingRatio = 0.1f;
        this.fixAspectRatio = false;
        this.aspectRatioX = 1;
        this.aspectRatioY = 1;
        this.borderLineThickness = TypedValue.applyDimension(1, 3.0f, dm);
        this.borderLineColor = Color.argb(170, 255, 255, 255);
        this.borderCornerThickness = TypedValue.applyDimension(1, 2.0f, dm);
        this.borderCornerOffset = TypedValue.applyDimension(1, 5.0f, dm);
        this.borderCornerLength = TypedValue.applyDimension(1, 14.0f, dm);
        this.borderCornerColor = -1;
        this.guidelinesThickness = TypedValue.applyDimension(1, 1.0f, dm);
        this.guidelinesColor = Color.argb(170, 255, 255, 255);
        this.backgroundColor = Color.argb(119, 0, 0, 0);
        this.minCropWindowWidth = (int) TypedValue.applyDimension(1, 42.0f, dm);
        this.minCropWindowHeight = (int) TypedValue.applyDimension(1, 42.0f, dm);
        this.minCropResultWidth = 40;
        this.minCropResultHeight = 40;
        this.maxCropResultWidth = 99999;
        this.maxCropResultHeight = 99999;
        this.activityTitle = "";
        this.activityMenuIconColor = 0;
        this.outputUri = Uri.EMPTY;
        this.outputCompressFormat = Bitmap.CompressFormat.JPEG;
        this.outputCompressQuality = 90;
        this.outputRequestWidth = 0;
        this.outputRequestHeight = 0;
        this.outputRequestSizeOptions = CropImageView.RequestSizeOptions.NONE;
        this.noOutputImage = false;
        this.initialCropWindowRectangle = null;
        this.initialRotation = -1;
        this.allowRotation = true;
        this.allowFlipping = true;
        this.allowCounterRotation = false;
        this.rotationDegrees = 90;
        this.flipHorizontally = false;
        this.flipVertically = false;
        this.cropMenuCropButtonTitle = null;
        this.cropMenuCropButtonIcon = 0;
    }

    protected CropImageOptions(Parcel in) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        boolean z5;
        boolean z6;
        boolean z7;
        boolean z8;
        boolean z9 = true;
        this.cropShape = CropImageView.CropShape.values()[in.readInt()];
        this.snapRadius = in.readFloat();
        this.touchRadius = in.readFloat();
        this.guidelines = CropImageView.Guidelines.values()[in.readInt()];
        this.scaleType = CropImageView.ScaleType.values()[in.readInt()];
        this.showCropOverlay = in.readByte() != 0;
        if (in.readByte() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.showProgressBar = z;
        if (in.readByte() != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.autoZoomEnabled = z2;
        if (in.readByte() != 0) {
            z3 = true;
        } else {
            z3 = false;
        }
        this.multiTouchEnabled = z3;
        this.maxZoom = in.readInt();
        this.initialCropWindowPaddingRatio = in.readFloat();
        if (in.readByte() != 0) {
            z4 = true;
        } else {
            z4 = false;
        }
        this.fixAspectRatio = z4;
        this.aspectRatioX = in.readInt();
        this.aspectRatioY = in.readInt();
        this.borderLineThickness = in.readFloat();
        this.borderLineColor = in.readInt();
        this.borderCornerThickness = in.readFloat();
        this.borderCornerOffset = in.readFloat();
        this.borderCornerLength = in.readFloat();
        this.borderCornerColor = in.readInt();
        this.guidelinesThickness = in.readFloat();
        this.guidelinesColor = in.readInt();
        this.backgroundColor = in.readInt();
        this.minCropWindowWidth = in.readInt();
        this.minCropWindowHeight = in.readInt();
        this.minCropResultWidth = in.readInt();
        this.minCropResultHeight = in.readInt();
        this.maxCropResultWidth = in.readInt();
        this.maxCropResultHeight = in.readInt();
        this.activityTitle = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.activityMenuIconColor = in.readInt();
        this.outputUri = (Uri) in.readParcelable(Uri.class.getClassLoader());
        this.outputCompressFormat = Bitmap.CompressFormat.valueOf(in.readString());
        this.outputCompressQuality = in.readInt();
        this.outputRequestWidth = in.readInt();
        this.outputRequestHeight = in.readInt();
        this.outputRequestSizeOptions = CropImageView.RequestSizeOptions.values()[in.readInt()];
        this.noOutputImage = in.readByte() != 0;
        this.initialCropWindowRectangle = (Rect) in.readParcelable(Rect.class.getClassLoader());
        this.initialRotation = in.readInt();
        if (in.readByte() != 0) {
            z5 = true;
        } else {
            z5 = false;
        }
        this.allowRotation = z5;
        if (in.readByte() != 0) {
            z6 = true;
        } else {
            z6 = false;
        }
        this.allowFlipping = z6;
        if (in.readByte() != 0) {
            z7 = true;
        } else {
            z7 = false;
        }
        this.allowCounterRotation = z7;
        this.rotationDegrees = in.readInt();
        if (in.readByte() != 0) {
            z8 = true;
        } else {
            z8 = false;
        }
        this.flipHorizontally = z8;
        this.flipVertically = in.readByte() == 0 ? false : z9;
        this.cropMenuCropButtonTitle = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.cropMenuCropButtonIcon = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9 = 1;
        dest.writeInt(this.cropShape.ordinal());
        dest.writeFloat(this.snapRadius);
        dest.writeFloat(this.touchRadius);
        dest.writeInt(this.guidelines.ordinal());
        dest.writeInt(this.scaleType.ordinal());
        dest.writeByte((byte) (this.showCropOverlay ? 1 : 0));
        if (this.showProgressBar) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeByte((byte) i);
        if (this.autoZoomEnabled) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeByte((byte) i2);
        if (this.multiTouchEnabled) {
            i3 = 1;
        } else {
            i3 = 0;
        }
        dest.writeByte((byte) i3);
        dest.writeInt(this.maxZoom);
        dest.writeFloat(this.initialCropWindowPaddingRatio);
        if (this.fixAspectRatio) {
            i4 = 1;
        } else {
            i4 = 0;
        }
        dest.writeByte((byte) i4);
        dest.writeInt(this.aspectRatioX);
        dest.writeInt(this.aspectRatioY);
        dest.writeFloat(this.borderLineThickness);
        dest.writeInt(this.borderLineColor);
        dest.writeFloat(this.borderCornerThickness);
        dest.writeFloat(this.borderCornerOffset);
        dest.writeFloat(this.borderCornerLength);
        dest.writeInt(this.borderCornerColor);
        dest.writeFloat(this.guidelinesThickness);
        dest.writeInt(this.guidelinesColor);
        dest.writeInt(this.backgroundColor);
        dest.writeInt(this.minCropWindowWidth);
        dest.writeInt(this.minCropWindowHeight);
        dest.writeInt(this.minCropResultWidth);
        dest.writeInt(this.minCropResultHeight);
        dest.writeInt(this.maxCropResultWidth);
        dest.writeInt(this.maxCropResultHeight);
        TextUtils.writeToParcel(this.activityTitle, dest, flags);
        dest.writeInt(this.activityMenuIconColor);
        dest.writeParcelable(this.outputUri, flags);
        dest.writeString(this.outputCompressFormat.name());
        dest.writeInt(this.outputCompressQuality);
        dest.writeInt(this.outputRequestWidth);
        dest.writeInt(this.outputRequestHeight);
        dest.writeInt(this.outputRequestSizeOptions.ordinal());
        dest.writeInt(this.noOutputImage ? 1 : 0);
        dest.writeParcelable(this.initialCropWindowRectangle, flags);
        dest.writeInt(this.initialRotation);
        if (this.allowRotation) {
            i5 = 1;
        } else {
            i5 = 0;
        }
        dest.writeByte((byte) i5);
        if (this.allowFlipping) {
            i6 = 1;
        } else {
            i6 = 0;
        }
        dest.writeByte((byte) i6);
        if (this.allowCounterRotation) {
            i7 = 1;
        } else {
            i7 = 0;
        }
        dest.writeByte((byte) i7);
        dest.writeInt(this.rotationDegrees);
        if (this.flipHorizontally) {
            i8 = 1;
        } else {
            i8 = 0;
        }
        dest.writeByte((byte) i8);
        if (!this.flipVertically) {
            i9 = 0;
        }
        dest.writeByte((byte) i9);
        TextUtils.writeToParcel(this.cropMenuCropButtonTitle, dest, flags);
        dest.writeInt(this.cropMenuCropButtonIcon);
    }

    public int describeContents() {
        return 0;
    }

    public void validate() {
        if (this.maxZoom < 0) {
            throw new IllegalArgumentException("Cannot set max zoom to a number < 1");
        } else if (this.touchRadius < 0.0f) {
            throw new IllegalArgumentException("Cannot set touch radius value to a number <= 0 ");
        } else if (this.initialCropWindowPaddingRatio < 0.0f || ((double) this.initialCropWindowPaddingRatio) >= 0.5d) {
            throw new IllegalArgumentException("Cannot set initial crop window padding value to a number < 0 or >= 0.5");
        } else if (this.aspectRatioX <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        } else if (this.aspectRatioY <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        } else if (this.borderLineThickness < 0.0f) {
            throw new IllegalArgumentException("Cannot set line thickness value to a number less than 0.");
        } else if (this.borderCornerThickness < 0.0f) {
            throw new IllegalArgumentException("Cannot set corner thickness value to a number less than 0.");
        } else if (this.guidelinesThickness < 0.0f) {
            throw new IllegalArgumentException("Cannot set guidelines thickness value to a number less than 0.");
        } else if (this.minCropWindowHeight < 0) {
            throw new IllegalArgumentException("Cannot set min crop window height value to a number < 0 ");
        } else if (this.minCropResultWidth < 0) {
            throw new IllegalArgumentException("Cannot set min crop result width value to a number < 0 ");
        } else if (this.minCropResultHeight < 0) {
            throw new IllegalArgumentException("Cannot set min crop result height value to a number < 0 ");
        } else if (this.maxCropResultWidth < this.minCropResultWidth) {
            throw new IllegalArgumentException("Cannot set max crop result width to smaller value than min crop result width");
        } else if (this.maxCropResultHeight < this.minCropResultHeight) {
            throw new IllegalArgumentException("Cannot set max crop result height to smaller value than min crop result height");
        } else if (this.outputRequestWidth < 0) {
            throw new IllegalArgumentException("Cannot set request width value to a number < 0 ");
        } else if (this.outputRequestHeight < 0) {
            throw new IllegalArgumentException("Cannot set request height value to a number < 0 ");
        } else if (this.rotationDegrees < 0 || this.rotationDegrees > 360) {
            throw new IllegalArgumentException("Cannot set rotation degrees value to a number < 0 or > 360");
        }
    }
}
