package com.kksystems.musicplayer;

import android.media.*;
import android.content.*;
import android.net.*;
import android.widget.*;
import java.io.*;
import android.app.*;
import android.util.*;
import android.text.format.*;
import android.graphics.*;

public class MediaManager
{
	private MediaPlayer player;
	private String filePath;
	private MediaMetadataRetriever metadataRetriver;
	private Context context;

	public MediaManager(Context p_context) {
		context = p_context;

		player = new MediaPlayer();
		metadataRetriver = new MediaMetadataRetriever();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}

	public void open(String p_path) {
		try {
			File l_file = new File(p_path);
			FileInputStream l_istream = new FileInputStream(l_file);
			filePath = p_path;
			player.setDataSource(l_istream.getFD());
			metadataRetriver.setDataSource(l_istream.getFD());
			player.prepare();
		} catch (Exception exception) {
			Toast.makeText(context, exception.getMessage(), Toast.LENGTH_LONG).show();
			exception.printStackTrace();
		}
	}

	public void play() {
		if (player.isPlaying()) {
			return;
		}

		player.start();
	}
	
	public void stop() {
		if (!player.isPlaying()) {
			return;
		}

		player.stop();
		try {
			player.prepare();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
	
	public boolean isPlaying() {
		return player.isPlaying();
	}
	
	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}
	
	public int getDuration() {
		return player.getDuration();
	}
	
	public void setCurrentPosition(int p_position) {
		player.seekTo(p_position);
	}
	
	public Bitmap getJacketBitmap() {
		final byte[] l_byteData = metadataRetriver.getEmbeddedPicture();
		return BitmapFactory.decodeByteArray(l_byteData, 0, l_byteData.length);
	}
	
	public String getTitle() {
		return metadataRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
	}
	
	public String getAlbum() {
		return metadataRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
	}
	
	public String getArtist() {
		return metadataRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
	}
	
	public String getPath() {
		return filePath;
	}
}
