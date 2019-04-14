package com.kksystems.musicplayer.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.kksystems.musicplayer.MediaSessionConnection;

import java.util.List;

public class PlayerFragmentViewModel extends ViewModel {
    private String mMediaId;
    private MediaSessionConnection mMediaSessionConnection;
    private MutableLiveData<PlaybackStateCompat> mPlaybackState = new MutableLiveData<>();
    private MutableLiveData<MediaMetadataCompat> mPlayingMedia = new MutableLiveData<>();

    public PlayerFragmentViewModel(String mediaId, MediaSessionConnection mediaSessionConnection) {
        mMediaId = mediaId;
        mMediaSessionConnection = mediaSessionConnection;

        mMediaSessionConnection.subscribe(mediaId, subscriptionCallback);
        mMediaSessionConnection.getPlaybackState().observeForever(playbackStateObserver);
        mMediaSessionConnection.getPlayingMedia().observeForever(mediaMetadataObserver);
    }

    //Subscribeした際に呼び出されるコールバック
    private MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String p_parentId, @NonNull List<MediaBrowserCompat.MediaItem> p_children) {
            PlaybackStateCompat playbackState = mMediaSessionConnection.getPlaybackState().getValue();
            MediaMetadataCompat playingMedia = mMediaSessionConnection.getPlayingMedia().getValue();

            boolean isPrepared = false;
            boolean alreadyPlaying = false;

            if (playbackState != null) {
                int state = playbackState.getState();
                isPrepared =
                        state == PlaybackStateCompat.STATE_BUFFERING ||
                        state == PlaybackStateCompat.STATE_PLAYING ||
                        state == PlaybackStateCompat.STATE_PAUSED;
            }

            if (playingMedia != null) {
                String playingMediaId = playingMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                alreadyPlaying = playingMediaId.equals(p_parentId);
            }

            if (!isPrepared || !alreadyPlaying) {
                mMediaSessionConnection.getTransportControls().prepareFromMediaId(p_parentId, null);
            }
        }
    };

    private Observer<PlaybackStateCompat> playbackStateObserver = new Observer<PlaybackStateCompat>() {
        @Override
        public void onChanged(@Nullable PlaybackStateCompat playbackState) {
            mPlaybackState.postValue(playbackState);
        }
    };

    private Observer<MediaMetadataCompat> mediaMetadataObserver = new Observer<MediaMetadataCompat>() {
        @Override
        public void onChanged(@Nullable MediaMetadataCompat mediaMetadata) {
            mPlayingMedia.postValue(mediaMetadata);
        }
    };

    public MutableLiveData<PlaybackStateCompat> getPlaybackState() {
        return mPlaybackState;
    }

    public MutableLiveData<MediaMetadataCompat> getPlayingMedia() {
        return mPlayingMedia;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        mMediaSessionConnection.getPlaybackState().removeObserver(playbackStateObserver);
        mMediaSessionConnection.getPlayingMedia().removeObserver(mediaMetadataObserver);

        mMediaSessionConnection.unsubscribe(mMediaId, subscriptionCallback);
    }

    public void seekTo(long position) {
        mMediaSessionConnection.getTransportControls().seekTo(position);
    }

    public void skipToPrevious() {
        mMediaSessionConnection.getTransportControls().skipToPrevious();
    }

    public void skipToNext() {
        mMediaSessionConnection.getTransportControls().skipToNext();
    }

    public void play() {
        mMediaSessionConnection.getTransportControls().play();
    }

    public void pause() {
        mMediaSessionConnection.getTransportControls().pause();
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
            return (T) new PlayerFragmentViewModel(mMediaId, mMediaSessionConnection);
        }
    }
}
