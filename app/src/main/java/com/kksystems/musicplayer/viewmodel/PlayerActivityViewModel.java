package com.kksystems.musicplayer.viewmodel;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.kksystems.musicplayer.MediaSessionConnection;

public class PlayerActivityViewModel extends ViewModel {
    private final String TAG = PlayerActivityViewModel.class.getSimpleName();

    private MediaSessionConnection mMediaSessionConnection;
    private LiveData<String> mRootMediaId;

    public PlayerActivityViewModel(MediaSessionConnection mediaSessionConnection) {
        mMediaSessionConnection = mediaSessionConnection;

        mRootMediaId = Transformations.map(mMediaSessionConnection.isConnected(), new Function<Boolean, String>() {
            @Override
            public String apply(Boolean isConnected) {
                Log.d(TAG, "apply: isConnected: " + isConnected);

                String mediaId = null;

                if (isConnected) {
                    mediaId = mMediaSessionConnection.getRoot();
                }

                return mediaId;
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        mMediaSessionConnection.disconnect();
    }

    public void setMedia(String mediaId) {
        mMediaSessionConnection.getTransportControls().prepareFromMediaId(mediaId, null);
    }

    public void prepare() {
        mMediaSessionConnection.getTransportControls().prepare();
    }

    public void play() {
        mMediaSessionConnection.getTransportControls().play();
    }

    public void pause() {
        mMediaSessionConnection.getTransportControls().pause();
    }

    public void subscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mMediaSessionConnection.subscribe(parentId, callback);
    }

    public void unsubscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mMediaSessionConnection.subscribe(parentId, callback);
    }

    public LiveData<String> getRootMediaId() {
        return mRootMediaId;
    }

    public boolean isPlaying() {
        PlaybackStateCompat playbackState = mMediaSessionConnection.getPlaybackState().getValue();
        return playbackState != null &&
               playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private MediaSessionConnection mMediaSessionConnection;

        public Factory(MediaSessionConnection mediaSessionConnection) {
            mMediaSessionConnection = mediaSessionConnection;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new PlayerActivityViewModel(mMediaSessionConnection);
        }
    }
}
