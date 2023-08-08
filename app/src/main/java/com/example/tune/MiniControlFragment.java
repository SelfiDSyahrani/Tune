package com.example.tune;

import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.tune.Event.PauseEvent;
import com.example.tune.Event.PlayEvent;
import com.example.tune.Event.SongUpdateEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class MiniControlFragment extends Fragment {
    ImageButton btnPlayPauseFrag;
    TextView songTitle, artist;
    SharedPreferences sharedPreferences;
    private static String SHARED_PREFERENCES_NAME = "myPreference";
    private static final String KEY_SONG_TITLE = "SongTitle";
    public static final String KEY_ARTIST = "Artist";

    private boolean isPlaying;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mini_control, container, false);
        btnPlayPauseFrag = (ImageButton) view.findViewById(R.id.playPauseBtn);
        songTitle = view.findViewById(R.id.songTitleFragment);
        artist = view.findViewById(R.id.fragmentArtist);


//

        btnPlayPauseFrag.setOnClickListener(v -> {
            if (isPlaying) {
                EventBus.getDefault().post(new PauseEvent());
            } else {
                EventBus.getDefault().post(new PlayEvent());
            }
            btnPlayPauseFrag.setImageResource(isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24);
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        btnPlayPauseFrag.setImageResource(isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE);
        String songTitleText = sharedPreferences.getString(KEY_SONG_TITLE, "");
        String artistText = sharedPreferences.getString(KEY_ARTIST, "");
        songTitle.setText(songTitleText);
        artist.setText(artistText);
    }

    private void updateFragment(SongUpdateEvent event) {
        if (event != null) {
            songTitle.setText(event.getSongTitle());
            artist.setText(event.getArtist());
        }
    }

    @Subscribe
    public void onSongUpdateEvent(SongUpdateEvent event) {
        isPlaying = event.isPlaying();
        updateFragment(event);
        btnPlayPauseFrag.setImageResource(isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24);
    }

}