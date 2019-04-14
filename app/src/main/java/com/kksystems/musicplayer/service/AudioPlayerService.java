package com.kksystems.musicplayer.service;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import java.util.ArrayList;
import java.util.List;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.*;
import com.google.android.exoplayer2.util.*;
import com.kksystems.musicplayer.AudioMediaManager;
import com.kksystems.musicplayer.Define;
import com.kksystems.musicplayer.PlayerActivity;
import com.kksystems.musicplayer.R;

public class AudioPlayerService extends MediaBrowserServiceCompat
{
	private final String TAG = AudioPlayerService.class.getSimpleName();

	private Handler mHandler;
	private AudioManager mAudioMgr;
	private MediaSessionCompat mMediaSession;
	private ExoPlayer mExoPlayer;

	private int mIndexPlaying = 0;
	
	private List<MediaSessionCompat.QueueItem> mQueueItems = new ArrayList<>();

	private AudioMediaManager mMediaManager;

	@Override
	public void onCreate()
	{
		// TODO: Implement this method
        Log.d(TAG, "AudioPlayerService#onCreate");

        super.onCreate();

		mMediaManager = new AudioMediaManager(getApplicationContext());
		// >>>
		mMediaManager.setOnLoaded(new AudioMediaManager.OnLoaded() {
			@Override
			public void onLoaded() {
				int l_index = 0;
				List<MediaSessionCompat.QueueItem> l_queueItems = new ArrayList<>();

				for (MediaBrowserCompat.MediaItem l_media : mMediaManager.getMediaItems()) {
					l_queueItems.add(new MediaSessionCompat.QueueItem(l_media.getDescription(), l_index));
					l_index++;
				}
				mQueueItems = l_queueItems;

				mMediaSession.setQueue(mQueueItems);
			}
		});
		// <<<
		
		mAudioMgr = (AudioManager)getSystemService(AUDIO_SERVICE);
		mMediaSession = new MediaSessionCompat(getApplicationContext(), TAG);
		mMediaSession.setFlags(
			MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
			MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
			MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
		);
		mMediaSession.setCallback(callback);
		setSessionToken(mMediaSession.getSessionToken());
		
		mMediaSession.getController().registerCallback(new MediaControllerCompat.Callback() {
			private int mPrevState = PlaybackStateCompat.STATE_STOPPED;

			@Override
			public void onPlaybackStateChanged(PlaybackStateCompat p_state) {
                Log.d(TAG, "AudioPlayerService#onPlaybackStateChanged\n" +
                    "\tstate: " + p_state.getState() + "\n" +
                    "\tpos: " + p_state.getPosition() + "\n");

                if (mPrevState != p_state.getState()) {
					createNotification();
				}

				mPrevState = p_state.getState();
			}
			
			@Override
			public void onMetadataChanged(MediaMetadataCompat p_metadata) {
                Log.d(TAG, "AudioPlayerService#onMetadataChanged");

                createNotification();
			}
		});

		mExoPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), new DefaultTrackSelector());
		mExoPlayer.addListener(eventListener);

		mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run()
			{
				// TODO: Implement this method
                if (mExoPlayer.getPlaybackState() == Player.STATE_READY && mExoPlayer.getPlayWhenReady()) {
					updatePlaybackState();
				}

				mHandler.postDelayed(this, 50);
			}
		}, 50);
	}

	@Override
	public void onDestroy()
	{
		// TODO: Implement this method
        Log.d(TAG, "AudioPlayerService#onDestroy");

		mMediaSession.setActive(false);
		mMediaSession.release();
		mExoPlayer.stop();
		mExoPlayer.release();

		mMediaManager.release();
	}

	@Override
	public MediaBrowserServiceCompat.BrowserRoot onGetRoot(@Nullable String p1, int p2, Bundle p3)
	{
		// TODO: Implement this method
        Log.d(TAG, "AudioPlayerService#onGetRoot");

        return new BrowserRoot(Define.MediaId.ROOT_ID, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onLoadChildren(@NonNull String p_parentMediaId, @NonNull MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> p_result)
	{
		// TODO: Implement this method
        Log.d(TAG, "AudioPlayerService#onLoadChildren");

		List<MediaBrowserCompat.MediaItem> result = new ArrayList();

		if (p_parentMediaId.equals(Define.MediaId.ROOT_ID)) {
			result.add(new MediaBrowserCompat.MediaItem(
					new MediaDescriptionCompat.Builder()
							.setMediaId(Define.MediaId.ALBUM_ID).build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
			result.add(new MediaBrowserCompat.MediaItem(
					new MediaDescriptionCompat.Builder()
							.setMediaId(Define.MediaId.ALL_ID).build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
		} else if (p_parentMediaId.equals(Define.MediaId.ALBUM_ID)) {
			// enumrate album list
			List<String> albumList = new ArrayList<>();
			List<MediaBrowserCompat.MediaItem> list = mMediaManager.getMediaItems();
			for (MediaBrowserCompat.MediaItem item : list) {
				String albumId = item.getDescription().getDescription() != null ? item.getDescription().getDescription().toString() : null;
				if (albumList.indexOf(albumId) < 0) {
					albumList.add(albumId);
					result.add(new MediaBrowserCompat.MediaItem(
							new MediaDescriptionCompat.Builder()
									.setMediaId("album:" + albumId)
									.setIconUri(item.getDescription().getIconUri())
									.setSubtitle(item.getDescription().getSubtitle())
									.setTitle(item.getDescription().getDescription()).build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
				}
			}
		} else if (p_parentMediaId.indexOf("album:") == 0) {
			// enumrate album list
			List<MediaBrowserCompat.MediaItem> list = mMediaManager.getMediaItems();
			for (MediaBrowserCompat.MediaItem item : list) {
				String albumId = item.getDescription().getDescription() != null ? item.getDescription().getDescription().toString() : null;
				if (albumId.equals(p_parentMediaId.substring(p_parentMediaId.indexOf(":") + 1))) {
					result.add(item);
				}
			}
		} else if (p_parentMediaId.equals(Define.MediaId.ALL_ID)) {
			// enumrate all music media
			result = mMediaManager.getMediaItems();
		} else {
			// return mediaitem of selected media
        	List<MediaBrowserCompat.MediaItem> list = mMediaManager.getMediaItems();
        	for (MediaBrowserCompat.MediaItem item : list) {
        		if (p_parentMediaId.equals(item.getMediaId())) {
        			result.add(item);
				}
			}
		}

		p_result.sendResult(result);
	}
	
	private MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPrepareFromMediaId(String p_mediaId, Bundle p_extras) {
            // TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onPrepareFromMediaId");

            DataSource.Factory l_dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "AppName"));
            MediaSource l_mediaSource = new ExtractorMediaSource.Factory(l_dataSourceFactory).createMediaSource(Uri.parse("file://" + mMediaManager.getMusicFilename(p_mediaId)));

            for (MediaSessionCompat.QueueItem l_item : mQueueItems) {
                if (l_item.getDescription().getMediaId().equals(p_mediaId)) {
                    mIndexPlaying = (int) l_item.getQueueId();
                }
            }

            mExoPlayer.prepare(l_mediaSource);
            mMediaSession.setActive(true);

            mMediaSession.setMetadata(mMediaManager.getMetadata(p_mediaId));
        }

		@Override
		public void onPlayFromMediaId(String p_mediaId, Bundle p_extras) {
			// TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onPlayFromMediaId");

            DataSource.Factory l_dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "AppName"));
			MediaSource l_mediaSource = new ExtractorMediaSource.Factory(l_dataSourceFactory).createMediaSource(Uri.parse("file://" + mMediaManager.getMusicFilename(p_mediaId)));

			for (MediaSessionCompat.QueueItem l_item : mQueueItems) {
				if (l_item.getDescription().getMediaId().equals(p_mediaId)) {
					mIndexPlaying = (int)l_item.getQueueId();
				}
			}
			
			mExoPlayer.prepare(l_mediaSource);
			mMediaSession.setActive(true);
			
			onPlay();
			
			mMediaSession.setMetadata(mMediaManager.getMetadata(p_mediaId));
		}

		@Override
		public void onPlay()
		{
			// TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onPlay");

            // 最後まで再生されていたら、先頭に戻しておく
            if (mExoPlayer.getCurrentPosition() >= mExoPlayer.getDuration()) {
            	mExoPlayer.seekTo(0);
			}

			if (mAudioMgr.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				mMediaSession.setActive(true);
				mExoPlayer.setPlayWhenReady(true);
			}
		}

		@Override
		public void onPause()
		{
			// TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onPause");

			mExoPlayer.setPlayWhenReady(false);
			mAudioMgr.abandonAudioFocus(audioFocusChangeListener);
		}

		@Override
		public void onStop()
		{
			// TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onStop");

            onPause();
			
			mMediaSession.setActive(false);
			mAudioMgr.abandonAudioFocus(audioFocusChangeListener);
		}

		@Override
		public void onSeekTo(long pos)
		{
			// TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onSeekTo\n" +
					"\tpos = " + pos +
                    "\tplayer pos = " + mExoPlayer.getCurrentPosition());

            mExoPlayer.seekTo(pos);

            // >>>>>
            String l_mediaId = "";
            for (MediaSessionCompat.QueueItem l_item : mQueueItems) {
                if (l_item.getQueueId() == mIndexPlaying) {
                    l_mediaId = l_item.getDescription().getMediaId();
                }
            }
            DataSource.Factory l_dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "AppName"));
            MediaSource l_mediaSource = new ExtractorMediaSource.Factory(l_dataSourceFactory).createMediaSource(Uri.parse("file://" + mMediaManager.getMusicFilename(l_mediaId)));
            mExoPlayer.prepare(l_mediaSource, false, false);
            // <<<<<

            Log.d(TAG, "AudioPlayerService#onSeekTo\n" +
                    "\tpos = " + pos +
                    "\tplayer pos = " + mExoPlayer.getCurrentPosition());
		}

		@Override
		public void onSkipToNext()
		{
			// TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onSkipToNext");

            mIndexPlaying++;
			if (mIndexPlaying >= mQueueItems.size()) {
				mIndexPlaying = 0;
			}
			
			onPlayFromMediaId(mQueueItems.get(mIndexPlaying).getDescription().getMediaId(), null);
		}

		@Override
		public void onSkipToPrevious()
		{
			// TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onSkipToPrevious");

            mIndexPlaying--;
			if (mIndexPlaying < 0) {
				mIndexPlaying = mQueueItems.size() - 1;
			}
			
			onPlayFromMediaId(mQueueItems.get(mIndexPlaying).getDescription().getMediaId(), null);
		}

		@Override
		public void onSkipToQueueItem(long id)
		{
			// TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onSkipToQueueItem");

            onPlayFromMediaId(mQueueItems.get((int)id).getDescription().getMediaId(), null);
		}

		@Override
		public boolean onMediaButtonEvent(Intent mediaButtonEvent)
		{
			// TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onMediaButtonEvent");

            boolean isHandled = super.onMediaButtonEvent(mediaButtonEvent);

            if (!isHandled) {
				KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
					isHandled = false;
				}
				int keyCode = keyEvent.getKeyCode();
				switch (keyCode) {
					case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
						PlaybackStateCompat state = mMediaSession.getController().getPlaybackState();
						long validActions = state == null ? 0 : state.getActions();
						if ((validActions & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
							onSkipToPrevious();
						}
						isHandled = true;
						break;
					default:
						break;
				}
			}

            return isHandled;
		}
	};
	
	private Player.EventListener eventListener = new Player.DefaultEventListener() {
		@Override
		public void onPlayerStateChanged(boolean p1, int p2)
		{
			// TODO: Implement this method
            Log.d(TAG, "AudioPlayerService#onPlayerStateChanged\n" +
                    "\tpos: " + mExoPlayer.getCurrentPosition() + "\n");

            updatePlaybackState();
		}
	};

	private void updatePlaybackState() {
		int state = PlaybackStateCompat.STATE_NONE;
		
		switch (mExoPlayer.getPlaybackState()) {
			case Player.STATE_IDLE:
				state = PlaybackStateCompat.STATE_NONE;
				break;
			case Player.STATE_BUFFERING:
				state = PlaybackStateCompat.STATE_BUFFERING;
				break;
			case Player.STATE_READY:
				if (mExoPlayer.getPlayWhenReady()) {
					state = PlaybackStateCompat.STATE_PLAYING;
				} else {
					state = PlaybackStateCompat.STATE_PAUSED;
				}
				break;
			case Player.STATE_ENDED:
				state = PlaybackStateCompat.STATE_STOPPED;
				break;
		}

		mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
			.setActions(PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_STOP)
			.setState(state, mExoPlayer.getCurrentPosition(), mExoPlayer.getPlaybackParameters().speed)
			.build());
	}

    @TargetApi(26)
	private void createNotificationChannel() {
		NotificationManager l_notifMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			return;
		}

		if (l_notifMgr == null) {
			return;
		}

		// カテゴリー名（通知設定画面に表示される情報）
		String name = getString(R.string.notification_channel_name_play);
		// システムに登録するChannelのID
		String id = getPackageName() + ".notifych.play";
		// 通知の詳細情報（通知設定画面に表示される情報）
		String notifyDescription = getString(R.string.notification_channel_description_play);

		// Channelの取得と生成
		if (l_notifMgr.getNotificationChannel(id) != null) {
			l_notifMgr.deleteNotificationChannel(id);
		}
		android.app.NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
		mChannel.setDescription(notifyDescription);
		mChannel.enableVibration(false);
		mChannel.setVibrationPattern(null);
		l_notifMgr.createNotificationChannel(mChannel);
    }

	private void createNotification() {
	    this.createNotificationChannel();

		MediaControllerCompat l_controller = mMediaSession.getController();
		MediaMetadataCompat l_metadata = l_controller.getMetadata();
		
		if (l_metadata == null && !mMediaSession.isActive()) {
			return;
		}
		
		MediaDescriptionCompat l_description = l_metadata.getDescription();
		NotificationCompat.Builder l_notification = new NotificationCompat.Builder(getApplicationContext(), getPackageName() + ".notifych.play");
		
		l_notification
			.setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS)
			// set infomation of current music
			.setContentTitle(l_description.getTitle())
			.setContentText(l_description.getSubtitle())
			.setSubText(l_description.getDescription())
			.setLargeIcon(l_description.getIconBitmap())

            // 通知をクリックしたときのインテントを設定
            .setContentIntent(createContentIntent())

			.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
			
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

			.setColor(ContextCompat.getColor(this, R.color.colorAccent))

			.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
				.setMediaSession(mMediaSession.getSessionToken())
				.setShowActionsInCompactView(1));

		l_notification.addAction(new NotificationCompat.Action(
			R.drawable.exo_controls_previous, "prev",
			MediaButtonReceiver.buildMediaButtonPendingIntent(this,
				PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));

		if (l_controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
			l_notification.setSmallIcon(R.drawable.exo_controls_play);

			l_notification.addAction(new NotificationCompat.Action(
				R.drawable.exo_controls_pause, "pause",
				MediaButtonReceiver.buildMediaButtonPendingIntent(this,
					PlaybackStateCompat.ACTION_PAUSE)));
		} else {
			l_notification.setSmallIcon(R.drawable.exo_controls_pause);

            l_notification.addAction(new NotificationCompat.Action(
				R.drawable.exo_controls_play, "play",
				MediaButtonReceiver.buildMediaButtonPendingIntent(this,
					PlaybackStateCompat.ACTION_PLAY)));
		}
		
		l_notification.addAction(new NotificationCompat.Action(
			R.drawable.exo_controls_next, "next",
			MediaButtonReceiver.buildMediaButtonPendingIntent(this,
				PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));

		startForeground(0x3f5d6a90, l_notification.build());

        if (l_controller.getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING) {
			stopForeground(false);
		}
	}
	
	private PendingIntent createContentIntent() {
		final String mediaId = mMediaSession.getController().getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);

		Intent l_intent = new Intent(this, PlayerActivity.class);
		l_intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		if (mediaId != null && !mediaId.equals("")) {
			l_intent.putExtra("MEDIA_ID", mediaId);
		}
		
		return PendingIntent.getActivity(this, 1, l_intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
	AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
		new AudioManager.OnAudioFocusChangeListener() {
			@Override
			public void onAudioFocusChange(int p_focusChange)
			{
				// TODO: Implement this method
                Log.d(TAG, "AudioPlayerService#onAudioFocusChange");

                switch (p_focusChange) {
					case AudioManager.AUDIOFOCUS_LOSS:
						mMediaSession.getController().getTransportControls().pause();
						break;
					case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
						mMediaSession.getController().getTransportControls().pause();
						break;
					case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
						break;
					case AudioManager.AUDIOFOCUS_GAIN:
						mMediaSession.getController().getTransportControls().play();
						break;
				}
			}
		};
}
