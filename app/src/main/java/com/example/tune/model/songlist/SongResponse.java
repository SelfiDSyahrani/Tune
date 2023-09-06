package com.example.tune.model.songlist;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SongResponse {
    @SerializedName("rc")
    private String rc;

    @SerializedName("songs")
    private List<Song> songs;

    public String getRc() {
        return rc;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public SongResponse(String rc, List<Song> songs) {
        this.rc = rc;
        this.songs = songs;
    }
// Tambahkan getter untuk mengakses data respons
}

