package com.kksystems.musicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class AudioMediaManager implements Loader.OnLoadCompleteListener<Cursor> {
    private Context mContext;
    private CursorLoader mCursorLoader;

    private final TreeMap<String, MediaMetadataCompat> music = new TreeMap<>();

    public AudioMediaManager(Context p_context) {
        mContext = p_context;

        mCursorLoader = new CursorLoader(p_context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,null,null,null);
        mCursorLoader.registerListener(0, this);

        ContentObserver l_contentObserver = new ContentObserver(new Handler()) {
            /**
             * This method is called when a content change occurs.
             * <p>
             * Subclasses should override this method to handle content changes.
             * </p>
             *
             * @param selfChange True if this is a self-change notification.
             */
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                mCursorLoader.startLoading();
            }
        };
        ContentResolver l_contentResolver = p_context.getContentResolver();
        l_contentResolver.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, l_contentObserver);

        mCursorLoader.startLoading();
    }

    public void release() {
        mCursorLoader.unregisterListener(this);
    }

    @Override
    public void onLoadComplete(@NonNull Loader<Cursor> p_loader, Cursor p_data) {
        MediaMetadataRetriever l_metadata = new MediaMetadataRetriever();

        if (p_data != null && !p_data.isClosed() && p_data.moveToFirst()) {
            do {
                Uri l_trackUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        p_data.getLong(p_data.getColumnIndex(MediaStore.Audio.Media._ID)));
//                //l_metadata.setDataSource(mContext, l_trackUri);
//                l_metadata.setDataSource(p_data.getString(p_data.getColumnIndex(MediaStore.Audio.Media.DATA)));
//
//                String l_genre = l_metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                String l_genre = "";

                this.createMediaMetadataCompat(
                        p_data.getString(p_data.getColumnIndex(MediaStore.Audio.Media._ID)),
                        p_data.getLong(p_data.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                        p_data.getString(p_data.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        p_data.getString(p_data.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                        p_data.getString(p_data.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                        l_genre,
                        p_data.getLong(p_data.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                        TimeUnit.MILLISECONDS,
                        p_data.getString(p_data.getColumnIndex(MediaStore.Audio.Media.DATA))
                );
            } while (p_data.moveToNext());

            p_data.close();
        }

        l_metadata.release();
        mOnLoaded.onLoaded();
    }

    public interface OnLoaded {
        void onLoaded();
    }

    private OnLoaded mOnLoaded;
    public void setOnLoaded(OnLoaded p_onLoaded) {mOnLoaded = p_onLoaded;}

    public String getMusicFilename(String p_mediaId) {
        MediaMetadataCompat metadata = music.get(p_mediaId);
        return metadata != null ?
                metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI) : null;
    }

    public Bitmap getAlbumBitmap(String p_mediaId) {
        Bitmap bitmap = null;
        MediaMetadataRetriever l_metadata = new MediaMetadataRetriever();
        String l_trackPath = getMusicFilename(p_mediaId);
        l_metadata.setDataSource(l_trackPath);

        try {
            byte[] l_byteArray = l_metadata.getEmbeddedPicture();

            bitmap = BitmapFactory.decodeByteArray(l_byteArray, 0, l_byteArray.length);
        } catch(NullPointerException e) {
            e.printStackTrace();
        }

        l_metadata.release();

        return bitmap;
    }

    public long getDuration(String p_mediaId) {
        MediaMetadataRetriever l_metadata = new MediaMetadataRetriever();
        String l_trackPath = getMusicFilename(p_mediaId);
        l_metadata.setDataSource(l_trackPath);

        try {
            return Long.parseLong(l_metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch(NullPointerException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();

        for (MediaMetadataCompat metadata : music.values()) {
            MediaBrowserCompat.MediaItem item =
                    new MediaBrowserCompat.MediaItem(
                            metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                    );
            result.add(item);
        }

        return result;
    }

    public MediaMetadataCompat getMetadata(String p_mediaId) {
        MediaMetadataCompat.Builder l_builder = new MediaMetadataCompat.Builder(music.get(p_mediaId));
        l_builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getAlbumBitmap(p_mediaId));
        l_builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration(p_mediaId));

        return l_builder.build();
    }

    private void createMediaMetadataCompat(
            String mediaId,
            long albumId,
            String title,
            String artist,
            String album,
            String genre,
            long duration,
            TimeUnit durationUnit,
            String musicFilename) {
        music.put(
                mediaId,
                new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, musicFilename)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                                TimeUnit.MILLISECONDS.convert(duration, durationUnit))
                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                        .putString(
                            MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                                ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart/"), albumId).toString())
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .build());
    }
}
