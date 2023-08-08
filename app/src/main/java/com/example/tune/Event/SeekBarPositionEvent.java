package com.example.tune.Event;

public class SeekBarPositionEvent {
    private int seekBarPosition;

    public SeekBarPositionEvent(int seekBarPosition) {
        this.seekBarPosition = seekBarPosition;
    }

    public int getSeekBarPosition() {
        return seekBarPosition;
    }
}
