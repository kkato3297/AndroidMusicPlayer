package com.kksystems.musicplayer.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kksystems.musicplayer.R;
import com.kksystems.musicplayer.model.PlaylistMediaItemData;
import com.kksystems.musicplayer.viewholder.PlaylistViewHolder;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class PlaylistGridViewAdapter extends RecyclerView.Adapter<PlaylistViewHolder> {
    private List<PlaylistMediaItemData> mList;

    public PlaylistGridViewAdapter(List<PlaylistMediaItemData> list) {
        mList = list;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item_grid, parent,false);
        return new PlaylistViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.cardView.setTag(mList.get(position).getMediaId());
        holder.titleView.setText(mList.get(position).getTitle());
        holder.artistView.setText(mList.get(position).getArtist());

        final PlaylistViewHolder holder2 = holder;
        final int position2 = position;

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Uri uri = mList.get(position2).getJacketImageUri();
                BitmapDecodeTask task = new BitmapDecodeTask(holder.cardView.getContext(), holder2);
                task.execute(uri);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private class BitmapDecodeTask extends AsyncTask<Uri, Integer, Integer> {
        private final static int STATE_OK = 0;
        private final static int STATE_NG = -1;
        private Context mContext;
        private PlaylistViewHolder mHolder;
        private Bitmap mBitmap;

        public BitmapDecodeTask(Context context, PlaylistViewHolder holder) {
            mContext = context;
            mHolder = holder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mHolder.jacketView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        @Override
        protected Integer doInBackground(Uri... uriarray) {
            ContentResolver contentResolver = mContext.getContentResolver();

            try {
                InputStream is = contentResolver.openInputStream(uriarray[0]);
                mBitmap = BitmapFactory.decodeStream(is);
            } catch(FileNotFoundException e) {
                return STATE_NG;
            }
            return STATE_OK;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (integer == STATE_OK) {
                mHolder.jacketView.setImageBitmap(mBitmap);
            } else {
                mHolder.jacketView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }
}
