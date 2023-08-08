package com.example.tune.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.net.URL;

public class Item implements Serializable {
    @SerializedName("singer")
    private String artist;
    @SerializedName("title")
    private String songTitle;
    private String album;
    @SerializedName("url")
    private String song;


    public Item(String songTitle, String artist, String album, String song) {
        this.songTitle = songTitle;
        this.artist = artist;
        this.album = album;
        this.song = song;
    }

    public String getArtist() {
        return artist;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getAlbum() {
        return album;
    }

    public String getSong() {
        return song;
    }
}