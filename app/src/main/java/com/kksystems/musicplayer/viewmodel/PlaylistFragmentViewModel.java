package com.kksystems.musicplayer.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.kksystems.musicplayer.MediaSessionConnection;

import java.util.List;

public class PlaylistFragmentViewModel extends ViewModel {
    private String mMediaId;
    private MediaSessionConnection mMediaSessionConnection;
    private MutableLiveData<PlaybackStateCompat> mPlaybackState = new MutableLiveData<>();
    private MutableLiveData<List<MediaBrowserCompat.MediaItem>> mMediaList = new MutableLiveData<>();

    public PlaylistFragmentViewModel(String mediaId, MediaSessionConnection mediaSessionConnection) {
        mMediaId = mediaId;
        mMediaSessionConnection = mediaSessionConnection;

        mMediaSessionConnection.subscribe(mediaId, subscriptionCallback);
    }

    //Subscribeした際に呼び出されるコールバック
    private MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String p_parentId, @NonNull List<MediaBrowserCompat.MediaItem> p_children) {
            mMediaList.postValue(p_children);
        }
    };

    public LiveData<List<MediaBrowserCompat.MediaItem>> getMediaList() {
        return mMediaList;
    }

    public String getPlayingMediaId() {
        return mMediaSessionConnection.getPlayingMedia().getValue().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
    }

    public boolean isPlaying() {
        return mMediaSessionConnection.getPlaybackState().getValue().getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    public void setMedia(String mediaId) {
        mMediaSessionConnection.getTransportControls().prepareFromMediaId(mediaId, null);
    }

    public void play() {
        mMediaSessionConnection.getTransportControls().play();
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        mMediaSessionConnection.unsubscribe(mMediaId, subscriptionCallback);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private String mMediaId;
        private MediaSessionConnection mMediaSessionConnection;

        public Factory(String mediaId, MediaSessionConnection mediaSessionConnection) {
            mMediaId = mediaId;
            mMediaSessionConnection = mediaSessionConnection;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new PlaylistFragmentViewModel(mMediaId, mMediaSessionConnection);
        }
    }
}
