package com.kksystems.musicplayer.model;

import android.graphics.Bitmap;
import android.net.Uri;

public class PlaylistMediaItemData {
    private String mMediaId;
    private Bitmap mJacketImage;
    private Uri mJacketImageUri;
    private String mTitle;
    private String mArtist;
    private String mGenre;

    public String getMediaId() {
        return mMediaId;
    }

    public void setMediaId(String mMediaId) {
        this.mMediaId = mMediaId;
    }

    public Bitmap getJacketImage() {
        return mJacketImage;
    }

    public void setJacketImage(Bitmap mJacketImage) {
        this.mJacketImage = mJacketImage;
    }

    public Uri getJacketImageUri() {
        return mJacketImageUri;
    }

    public void setJacketImageUri(Uri mJacketImageUri) {
        this.mJacketImageUri = mJacketImageUri;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getGenre() {
        return mGenre;
    }

    public void setGenre(String mGenre) {
        this.mGenre = mGenre;
    }
}
