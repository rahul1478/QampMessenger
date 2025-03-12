package com.qamp.app.sources;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class AlbumListData {
    private Bitmap mAlbumCoverPicture = null;
    private String mAlbumName = null;
    private String mAlbumPictureUrl = null;
    private Boolean mHasImageDownloaded = false;
    private Integer mPhotoCount = 0;
    private List<AlbumPhotosData> mPhotosList = new ArrayList();

    public String getmAlbumName() {
        return this.mAlbumName;
    }

    public String getmAlbumPictureUrl() {
        return this.mAlbumPictureUrl;
    }

    public Bitmap getmAlbumCoverPicture() {
        return this.mAlbumCoverPicture;
    }

    public void setmAlbumName(String name) {
        this.mAlbumName = name;
    }

    public void setmAlbumPictureUrl(String totalPictures) {
        this.mAlbumPictureUrl = totalPictures;
    }

    public void setmAlbumCoverPicture(Bitmap albumCover) {
        this.mAlbumCoverPicture = albumCover;
    }

    public Integer getmPhotoCount() {
        return this.mPhotoCount;
    }

    public void setmPhotoCount(Integer mPhotoCount2) {
        this.mPhotoCount = mPhotoCount2;
    }

    public List<AlbumPhotosData> getmPhotosList() {
        return this.mPhotosList;
    }

    public void setmPhotosList(List<AlbumPhotosData> mPhotosList2) {
        this.mPhotosList = mPhotosList2;
    }

    public Boolean getmHasImageDownloaded() {
        return this.mHasImageDownloaded;
    }

    public void setmHasImageDownloaded(Boolean mHasImageDownloaded2) {
        this.mHasImageDownloaded = mHasImageDownloaded2;
    }
}
