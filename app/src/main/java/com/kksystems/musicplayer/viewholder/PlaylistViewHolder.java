package com.kksystems.musicplayer.viewholder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kksystems.musicplayer.R;

public class PlaylistViewHolder extends RecyclerView.ViewHolder {
    public CardView cardView;
    public ImageView jacketView;
    public TextView titleView;
    public TextView artistView;

    public PlaylistViewHolder(View itemView) {
        super(itemView);

        cardView = itemView.findViewById(R.id.playlist_item);
        jacketView = itemView.findViewById(R.id.listItemJacketImage);
        titleView = itemView.findViewById(R.id.listItemTitle);
        artistView = itemView.findViewById(R.id.listItemArtist);
    }
}
