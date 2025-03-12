package com.qamp.app.sources;

import android.graphics.Bitmap;

public class AlbumPhotosData {
    private Bitmap mGridPicture = null;
    private Boolean mHasDownloadedGP = false;
    private String mPictueUrl = null;
    private String mSourceUrl = null;

    public String getmPictueUrl() {
        return this.mPictueUrl;
    }

    public String getmSourceUrl() {
        return this.mSourceUrl;
    }

    public void setmPictueUrl(String mPictueUrl2) {
        this.mPictueUrl = mPictueUrl2;
    }

    public void setmSourceUrl(String mSourceUrl2) {
        this.mSourceUrl = mSourceUrl2;
    }

    public Bitmap getmGridPicture() {
        return this.mGridPicture;
    }

    public void setmGridPicture(Bitmap mGridPicture2) {
        this.mGridPicture = mGridPicture2;
    }

    public Boolean getmHasDownloadedGP() {
        return this.mHasDownloadedGP;
    }

    public void setmHasDownloadedGP(Boolean mHasDownloadedGP2) {
        this.mHasDownloadedGP = mHasDownloadedGP2;
    }
}
