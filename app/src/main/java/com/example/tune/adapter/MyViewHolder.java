package com.example.tune.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tune.R;

public class MyViewHolder extends RecyclerView.ViewHolder {

    ImageView album;
    TextView songTitle;
    TextView artist;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        album = itemView.findViewById(R.id.imageview_album);
        songTitle = itemView.findViewById(R.id.songTitle);
        artist = itemView.findViewById(R.id.artist);
    }
}
