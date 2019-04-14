package com.kksystems.musicplayer;

import android.arch.lifecycle.MutableLiveData;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;

public class MediaSessionConnection {
    private final static String TAG = MediaSessionConnection.class.getSimpleName();
    private final static PlaybackStateCompat INIT_PLAYBACK_STATE =
            new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 0.f)
                .build();
    private final static MediaMetadataCompat INIT_METADATA =
            new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
                .build();

    private MutableLiveData<Boolean> mIsConnected;
    private MutableLiveData<PlaybackStateCompat> mPlaybackState;
    private MutableLiveData<MediaMetadataCompat> mNowPlaying;

    private MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController = null;

    private static MediaSessionConnection instance = null;

    private MediaSessionConnection(Context context, ComponentName serviceComponent) {
        Log.d(TAG, "MediaSessionConnection");

        mIsConnected = new MutableLiveData<Boolean>() {};
        mIsConnected.postValue(false);
        mPlaybackState = new MutableLiveData<PlaybackStateCompat>() {};
        mPlaybackState.postValue(INIT_PLAYBACK_STATE);
        mNowPlaying = new MutableLiveData<MediaMetadataCompat>() {};
        mNowPlaying.postValue(INIT_METADATA);

        mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback(context);
        if (mMediaBrowser == null) {
            mMediaBrowser = new MediaBrowserCompat(
                    context, serviceComponent, mMediaBrowserConnectionCallback, null);
            mMediaBrowser.connect();
        }
    }

    public void connect() {
        mMediaBrowser.connect();
    }

    public void disconnect() {
        mMediaBrowser.disconnect();

        instance = null;
    }

    public void subscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mMediaBrowser.subscribe(parentId, callback);
    }

    public void unsubscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mMediaBrowser.unsubscribe(parentId, callback);
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        return mMediaController.getTransportControls();
    }

    public MutableLiveData<PlaybackStateCompat> getPlaybackState() {
        return mPlaybackState;
    }

    public MutableLiveData<MediaMetadataCompat> getPlayingMedia() {
        return mNowPlaying;
    }

    public MutableLiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    public String getRoot() {
        return mMediaBrowser.getRoot();
    }

    public static MediaSessionConnection getInstance(Context context, ComponentName serviceComponent) {
        if (instance == null) {
            synchronized (MediaSessionConnection.class) {
                instance = new MediaSessionConnection(context, serviceComponent);
            }
        }

        return instance;
    }

    private final class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        private Context mContext;

        public MediaBrowserConnectionCallback(Context context) {
            mContext = context;
        }

        @Override
        public void onConnected() {
            super.onConnected();

            try {
                mMediaController = new MediaControllerCompat(mContext, mMediaBrowser.getSessionToken());
                mMediaController.registerCallback(new MediaControllerCallback());
            } catch(RemoteException e) {
                e.printStackTrace();
            }

            mPlaybackState.postValue(mMediaController.getPlaybackState() != null ? mMediaController.getPlaybackState() : mPlaybackState.getValue());
            mNowPlaying.postValue(mMediaController.getMetadata() != null ? mMediaController.getMetadata() : mNowPlaying.getValue());
            mIsConnected.postValue(true);
        }

        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();

            mIsConnected.postValue(false);
        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();

            mIsConnected.postValue(false);
        }
    }

    private final class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            mPlaybackState.postValue(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            mNowPlaying.postValue(metadata);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();

            mMediaBrowserConnectionCallback.onConnectionSuspended();
        }
    }
}
