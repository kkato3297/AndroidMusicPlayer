package com.kksystems.musicplayer.view.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kksystems.musicplayer.Define;
import com.kksystems.musicplayer.R;
import com.kksystems.musicplayer.utils.InjectorUtils;
import com.kksystems.musicplayer.viewmodel.PlayerFragmentViewModel;

import java.util.Locale;

public final class PlayerFragment extends Fragment {
    private final String TAG = PlayerFragment.class.getSimpleName();

    private boolean mIsTracking = false;

    private View rootView = null;

    private ImageView jacketImage;
    private TextView titleLabel;
    private TextView albumLabel;
    private TextView artistLabel;
    private TextView currentTimeLabel;
    private TextView durationTimeLabel;
    private SeekBar playingSeekBar;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private FloatingActionButton playButton;

    private PlayerFragmentViewModel mModel;
    private String mMediaId;

    private void setPlayerLayout() {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            activity.findViewById(android.R.id.content).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void setPlaylistLayout() {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            activity.findViewById(android.R.id.content).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            //activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arg = getArguments();

        mMediaId = (arg != null) ? arg.getString(Define.MEDIA_ID) : null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.view_player, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mModel = ViewModelProviders
                .of(this, InjectorUtils.providePlayerFragmentViewModel(this.requireContext(), mMediaId))
                .get(PlayerFragmentViewModel.class);
        mModel.getPlaybackState().observe(this, new Observer<PlaybackStateCompat>() {
            @Override
            public void onChanged(@Nullable PlaybackStateCompat playbackState) {
                mControllerCallback.onPlaybackStateChanged(playbackState);
            }
        });
        mModel.getPlayingMedia().observe(this, new Observer<MediaMetadataCompat>() {
            @Override
            public void onChanged(@Nullable MediaMetadataCompat mediaMetadata) {
                mControllerCallback.onMetadataChanged(mediaMetadata);
            }
        });

        setPlayerLayout();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        setPlaylistLayout();

//        ViewGroup l_contentRoot = getActivity().findViewById(android.R.id.content);
//        l_contentRoot.setBackgroundResource(android.R.color.background_light);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view;

        assocViewInstance();
        setEventListener();

        titleLabel.setText("");
        albumLabel.setText("");
        artistLabel.setText("");
        updateTimeBar(0);
        updateCurrentTimeLabel(0);
        updateDurationTimeLabel(0);
    }

    private void assocViewInstance() {
        jacketImage = rootView.findViewById(R.id.jacketImage);
        titleLabel = rootView.findViewById(R.id.titleLabel);
        albumLabel = rootView.findViewById(R.id.albumLabel);
        artistLabel = rootView.findViewById(R.id.artistLabel);
        currentTimeLabel = rootView.findViewById(R.id.currentTimeLabel);
        durationTimeLabel = rootView.findViewById(R.id.durationTimeLabel);
        playingSeekBar = rootView.findViewById(R.id.playingSeekBar);
        prevButton = rootView.findViewById(R.id.prevButton);
        nextButton = rootView.findViewById(R.id.nextButton);
        playButton = rootView.findViewById(R.id.playButton);
    }

    private void setEventListener() {
        playingSeekBar.setOnSeekBarChangeListener(playingSeekBar_onSeekBarChange);
        prevButton.setOnClickListener(prevButton_onClick);
        nextButton.setOnClickListener(nextButton_onClick);
    }

    private SeekBar.OnSeekBarChangeListener playingSeekBar_onSeekBarChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
            // TODO: Implement this method
        }

        @Override
        public void onStartTrackingTouch(SeekBar p1) {
            // TODO: Implement this method
            mIsTracking = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar p1) {
            // TODO: Implement this method
            mModel.seekTo(playingSeekBar.getProgress());
            mIsTracking = false;
        }
    };

    private View.OnClickListener prevButton_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View p1)
        {
            // TODO: Implement this method
            mModel.skipToPrevious();
        }
    };

    private View.OnClickListener nextButton_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View p1)
        {
            // TODO: Implement this method
            mModel.skipToNext();
        }
    };

    private View.OnClickListener playButton_pausing_OnClick = new View.OnClickListener() {
        @Override
        public void onClick(View p1)
        {
            // TODO: Implement this method
            mModel.play();
        }
    };

    private View.OnClickListener playButton_playing_OnClick = new View.OnClickListener() {
        @Override
        public void onClick(View p1)
        {
            // TODO: Implement this method
            mModel.pause();
        }
    };

    private MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        /**
         * Override to handle changes in playback state.
         *
         * @param p_state The new playback state of the session
         */
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat p_state) {
            Log.d(TAG, "onPlaybackStateChanged\n" +
                    "\tstate: " + p_state.getState() + "\n" +
                    "\tpos: " + p_state.getPosition() + "\n");

            //プレイヤーの状態によってボタンの挙動とアイコンを変更する
            if (p_state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                playButton.setOnClickListener(playButton_playing_OnClick);
                playButton.setImageResource(R.drawable.exo_controls_pause);
            } else {
                playButton.setOnClickListener(playButton_pausing_OnClick);
                playButton.setImageResource(R.drawable.exo_controls_play);
            }

            updateCurrentTimeLabel((int) p_state.getPosition());
            // シークバー操作中は、シークバーを更新しない
            if (!mIsTracking && p_state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                Log.d(TAG, "onPlaybackStateChanged, pos = " + p_state.getPosition());
                updateTimeBar((int) p_state.getPosition());
            }
        }

        /**
         * Override to handle changes to the current metadata.
         *
         * @param p_metadata The current metadata for the session or null if none.
         * @see MediaMetadataCompat
         */
        @Override
        public void onMetadataChanged(MediaMetadataCompat p_metadata) {
            Log.d(TAG, "onMetadataChanged");

            try {
                final Bitmap l_backBitmap = doBlurEffect(doBlurEffect(getBackgroundBitmap(p_metadata.getDescription().getIconBitmap())));
                final BitmapDrawable l_drawable = new BitmapDrawable(getResources(), l_backBitmap);
                // l_drawable.setAlpha(0);
//                ViewGroup l_contentRoot = getActivity().findViewById(android.R.id.content);
//                l_contentRoot.setBackground(l_drawable);
                rootView.setBackground(l_drawable);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            titleLabel.setText(p_metadata.getDescription().getTitle());
            albumLabel.setText(p_metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
            artistLabel.setText(p_metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            jacketImage.setImageBitmap(p_metadata.getDescription().getIconBitmap());
            updateDurationTimeLabel((int) p_metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
            playingSeekBar.setMax((int) p_metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        }
    };

    private Bitmap getBackgroundBitmap(Bitmap p_bitmap) {
        //幅と高さの取得
        int l_imageWidth = p_bitmap.getWidth();
        int l_imageHeight = p_bitmap.getHeight();

        //トリミングする幅、高さ、座標の設定
        Point l_point = new Point();
        Size l_size;
        getActivity().getWindowManager().getDefaultDisplay().getRealSize(l_point);
        if (l_point.x < l_point.y) {
            l_size = new Size((int)((double)(l_imageHeight * l_point.x) / l_point.y), l_imageHeight);
        } else {
            l_size = new Size(l_imageWidth, (int)((double)(l_imageWidth * l_point.y) / l_point.x));
        }

        int l_nWidth = l_size.getWidth();
        int l_nHeight = l_size.getHeight();
        int l_startX = (l_imageWidth - l_nWidth) / 2;
        int l_startY = (l_imageHeight - l_nHeight) / 2;

        //トリミングしたBitmapの作成
        return Bitmap.createBitmap(p_bitmap, l_startX, l_startY, l_nWidth, l_nHeight, null, true);
    }

    private Bitmap doBlurEffect(Bitmap p_bitmap) {
        Bitmap l_result = p_bitmap.copy(p_bitmap.getConfig(), true);

        final RenderScript l_rs = RenderScript.create(getContext());
        final Allocation l_src = Allocation.createFromBitmap(l_rs, p_bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        final Allocation l_dst = Allocation.createTyped(l_rs, l_src.getType());
        final ScriptIntrinsicBlur l_script = ScriptIntrinsicBlur.create(l_rs, Element.U8_4(l_rs));
        l_script.setRadius(25);
        l_script.setInput(l_src);
        l_script.forEach(l_dst);
        l_dst.copyTo(l_result);

        return l_result;
    }

    private void updateCurrentTimeLabel(int p_currentPosition) {
        int l_min = p_currentPosition / 60000;
        int l_sec = p_currentPosition / 1000 % 60;

        currentTimeLabel.setText(String.format(Locale.getDefault(), "%1$02d:%2$02d", l_min, l_sec));
    }

    private void updateDurationTimeLabel(int p_durationTime) {
        int l_min = p_durationTime / 60000;
        int l_sec = p_durationTime / 1000 % 60;

        durationTimeLabel.setText(String.format(Locale.getDefault(), "%1$02d:%2$02d", l_min, l_sec));
    }

    private void updateTimeBar(int p_currentPosition) {
        playingSeekBar.setProgress(p_currentPosition);
    }
}
