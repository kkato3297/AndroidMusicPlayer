package com.kksystems.musicplayer.utils;

import android.content.ComponentName;
import android.content.Context;

import com.kksystems.musicplayer.service.AudioPlayerService;
import com.kksystems.musicplayer.MediaSessionConnection;
import com.kksystems.musicplayer.viewmodel.PlayerFragmentViewModel;
import com.kksystems.musicplayer.viewmodel.PlayerActivityViewModel;
import com.kksystems.musicplayer.viewmodel.PlaylistFragmentViewModel;

public final class InjectorUtils {
    private static MediaSessionConnection provideMediaSessionConnection(Context context) {
        return MediaSessionConnection.getInstance(context, new ComponentName(context, AudioPlayerService.class));
    }

    public static PlayerActivityViewModel.Factory providePlayerActivityViewModel(Context context) {
        final Context applicationContext = context.getApplicationContext();
        final MediaSessionConnection mediaSessionConnection = provideMediaSessionConnection(applicationContext);
        return new PlayerActivityViewModel.Factory(mediaSessionConnection);
    }

    public static PlayerFragmentViewModel.Factory providePlayerFragmentViewModel(Context context, String mediaId) {
        final Context applicationContext = context.getApplicationContext();
        final MediaSessionConnection mediaSessionConnection = provideMediaSessionConnection(applicationContext);
        return new PlayerFragmentViewModel.Factory(mediaId, mediaSessionConnection);
    }

    public static PlaylistFragmentViewModel.Factory providePlaylistFragmentViewModel(Context context, String mediaId) {
        final Context applicationContext = context.getApplicationContext();
        final MediaSessionConnection mediaSessionConnection = provideMediaSessionConnection(applicationContext);
        return new PlaylistFragmentViewModel.Factory(mediaId, mediaSessionConnection);
    }
}
