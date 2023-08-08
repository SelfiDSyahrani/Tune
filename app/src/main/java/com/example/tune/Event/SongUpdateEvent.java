package com.example.tune.Event;

public class SongUpdateEvent {
    private String songTitle;
    private String artist;
    private boolean isPlaying;
    private int duration;
    private int currentPosition;

    public SongUpdateEvent(String songTitle, String artist, boolean isPlaying, int duration, int currentPosition) {
        this.songTitle = songTitle;
        this.artist = artist;
        this.isPlaying = isPlaying;
        this.duration = duration;
        this.currentPosition = currentPosition;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getArtist() {
        return artist;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
    public int getDuration(){
        return duration;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public static class playEvent {
    }
}
