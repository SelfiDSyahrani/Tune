package com.example.tune.model.songlist;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Song implements Serializable {
    @SerializedName("song_id")
    private int songId;

    @SerializedName("title")
    private String title;

    @SerializedName("genre")
    private String genre;

    @SerializedName("song_url")
    private String songUrl;

    @SerializedName("artist")
    private String artist;

    @SerializedName("album")
    private String album;

    @SerializedName("album_img")
    private String albumImg;

    public Song(int songId, String title, String genre, String songUrl, String artist, String album, String albumImg) {
        this.songId = songId;
        this.title = title;
        this.genre = genre;
        this.songUrl = songUrl;
        this.artist = artist;
        this.album = album;
        this.albumImg = albumImg;
    }

    public int getSongId() {
        return songId;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumImg() {
        return albumImg;
    }
}
