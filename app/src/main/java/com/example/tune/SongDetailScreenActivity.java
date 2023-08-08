package com.example.tune;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tune.Event.ForwardEvent;
import com.example.tune.Event.NextEvent;
import com.example.tune.Event.PauseEvent;
import com.example.tune.Event.PlayEvent;
import com.example.tune.Event.PrevEvent;
import com.example.tune.Event.ReplayEvent;
import com.example.tune.Event.SeekBarPositionEvent;
import com.example.tune.Event.SongUpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SongDetailScreenActivity extends AppCompatActivity {
    private MediaPlayerService mediaPlayerService;
    public SeekBar seekBar;
    private ImageButton btnPlayPause, btnPrev, btnNext, btnReplay, btnForward;
    private TextView songTitle, artist;
    private TextView textTotalTime, textCurrentTime;
    private boolean isUserSeeking;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail_screen);
        mediaPlayerService = new MediaPlayerService();

        songTitle = findViewById(R.id.textSongTitle);
        artist = findViewById(R.id.textArtist);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalTime = findViewById(R.id.textTotalTime);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrevious);
        btnReplay = findViewById(R.id.btnReplay);
        btnForward = findViewById(R.id.btnForward);
        seekBar = findViewById(R.id.seekBar);

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    EventBus.getDefault().post(new PauseEvent());
                    btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24);
                } else {
                    EventBus.getDefault().post(new PlayEvent());
                    btnPlayPause.setImageResource(R.drawable.baseline_pause_24);
                }
            }
        });

        btnPrev.setOnClickListener(v -> {
            EventBus.getDefault().post(new PrevEvent());
        });

        btnNext.setOnClickListener(v -> {
            EventBus.getDefault().post(new NextEvent());
        });

        btnForward.setOnClickListener(v -> {
            EventBus.getDefault().post(new ForwardEvent());
        });

        btnReplay.setOnClickListener(v -> {
            EventBus.getDefault().post(new ReplayEvent());
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    textCurrentTime.setText(mediaPlayerService.formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                int seekBarPosition = seekBar.getProgress();
                EventBus.getDefault().post(new SeekBarPositionEvent(seekBarPosition));
            }
        });
    }

    private void updateSeekBar(SongUpdateEvent event) {
        if (event != null) {
            int mCurrentPosition = event.getCurrentPosition();
            seekBar.setProgress(mCurrentPosition);
            textCurrentTime.setText(mediaPlayerService.formatTime(mCurrentPosition));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }

    private void updateUI(SongUpdateEvent event) {
        if (event != null) {
            songTitle.setText(event.getSongTitle());
            artist.setText(event.getArtist());
            textTotalTime.setText(mediaPlayerService.formatTime(mediaPlayerService.getDuration()));
            if (event.isPlaying()) {
                btnPlayPause.setImageResource(R.drawable.baseline_pause_24);
            } else {
                btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSongUpdateEvent(SongUpdateEvent event) {
        updateUI(event);
        int duration = event.getDuration();
        isPlaying = event.isPlaying();
        seekBar.setMax(duration);
        textTotalTime.setText(mediaPlayerService.formatTime(duration));
        updateSeekBar(event);
    }
}