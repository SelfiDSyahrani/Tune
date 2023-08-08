package com.example.tune;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.tune.Event.ForwardEvent;
import com.example.tune.Event.NextEvent;
import com.example.tune.Event.PauseEvent;
import com.example.tune.Event.PlayEvent;
import com.example.tune.Event.PrevEvent;
import com.example.tune.Event.ReplayEvent;
import com.example.tune.Event.SeekBarPositionEvent;
import com.example.tune.Event.SongUpdateEvent;
import com.example.tune.model.Item;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener {
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static MediaPlayer mediaPlayer;
    private static List<Item> songList;
    private static int currentSongIndex;
    private boolean isPrepared = false;

    private Handler handler;
    private static final int DELAY_MILLIS = 1000;


    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            List<Item> items = (List<Item>) intent.getSerializableExtra("SONG_LIST");
            if (items != null) {
                songList = new ArrayList<>(items);
                currentSongIndex = intent.getIntExtra("CURRENT_SONG_INDEX", 0);
                startForegroundService();
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnPreparedListener(this);
                    prepareMediaPlayer();
                }
                playMusic();
            }
        }
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "PLAY_PAUSE":
                    EventBus.getDefault().post(new PlayEvent());
                    break;
                case "PREVIOUS":
                    EventBus.getDefault().post(new PrevEvent());
                    break;
                case "NEXT":
                    EventBus.getDefault().post(new NextEvent());
                    break;
                case "FORWARD":
                    EventBus.getDefault().post(new ForwardEvent());
                    break;
                case "REPLAY":
                    EventBus.getDefault().post(new ReplayEvent());

            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        mp.start();
        handler = new Handler();
        handler.postDelayed(UpdateRunnable, 500);

//        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                currentSongIndex++;
//                if (currentSongIndex >= songList.size()) {
//                    currentSongIndex = 0;
//                }
//                prepareMediaPlayer();
//            }
//        });
    }

    private Runnable UpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                EventBus.getDefault().post(new SongUpdateEvent(
                        getCurrentSong().getSongTitle(),
                        getCurrentSong().getArtist(),
                        mediaPlayer.isPlaying(),
                        mediaPlayer.getDuration(),
                        mediaPlayer.getCurrentPosition()));
            }
            handler.postDelayed(this, DELAY_MILLIS);
        }
    };

    public void playMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause(); // Jika MediaPlayer memutar, jeda pemutaran.
            } else {
                mediaPlayer.reset(); // Reset MediaPlayer untuk memulai pemutaran baru.
                try {
                    mediaPlayer.setDataSource(songList.get(currentSongIndex).getSong());
                    mediaPlayer.prepareAsync(); // Asinkron persiapan pemutaran musik.
                    isPrepared = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void prepareMediaPlayer() {
        if (songList != null && currentSongIndex >= 0 && currentSongIndex < songList.size()) {
            try {
                Item currentSong = songList.get(currentSongIndex);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(currentSong.getSong());
                mediaPlayer.prepareAsync();
                isPrepared = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void togglePlayback() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public Item getCurrentSong() {
        if (songList != null && currentSongIndex >= 0 && currentSongIndex < songList.size()) {
            return songList.get(currentSongIndex);
        }
        return null;
    }

    public void playNextSong() {
        currentSongIndex++;
        if (currentSongIndex >= songList.size()) {
            currentSongIndex = 0;
        }
        prepareMediaPlayer();
        playMusic();
    }

    public void playPrevSong() {
        currentSongIndex--;
        if (currentSongIndex < 0) {
            currentSongIndex = songList.size() - 1;
        }
        prepareMediaPlayer();
        playMusic();
    }

    public String formatTime(int timeInMillis) {
        int seconds = (timeInMillis / 1000) % 60;
        int minutes = (timeInMillis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public class LocalBinder extends Binder {

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundService() {
        createNotificationChannel();
        startForeground(NOTIFICATION_ID,
                notification(
                        getCurrentSong().getSongTitle(),
                        getCurrentSong().getArtist(),
                        getDuration(), getCurrentPosition()));
    }

    private Notification notification(String songTitle, String artist, int max, int progress) {
        Intent notificationIntent = new Intent(this, SongDetailScreenActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                MediaPlayerService.this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent playPendingIntent = createPlaybackActionIntent("PLAY_PAUSE");
        PendingIntent nextPendingIntent = createPlaybackActionIntent("NEXT");
        PendingIntent prevPendingIntent = createPlaybackActionIntent("PREVIOUS");
        PendingIntent replayPendingIntent = createPlaybackActionIntent("REPLAY");
        PendingIntent forwardPendingIntent = createPlaybackActionIntent("FORWARD");

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_media_controls);
        notificationLayout.setOnClickPendingIntent(R.id.playPauseBtn, playPendingIntent);
        notificationLayout.setOnClickPendingIntent(R.id.prevBtn, prevPendingIntent);
        notificationLayout.setOnClickPendingIntent(R.id.nextBtn, nextPendingIntent);
        notificationLayout.setTextViewText(R.id.songTitleNotif, songTitle);
        notificationLayout.setTextViewText(R.id.ArtistNotif, artist);
        int playPauseIcon = isPlaying() ? R.drawable.baseline_pause_24_notif : R.drawable.baseline_play_arrow_notif;
        notificationLayout.setImageViewResource(R.id.playPauseBtn, playPauseIcon);

        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.expanded_notification);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.playPauseBtn, playPendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.prevBtn, prevPendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.nextBtn, nextPendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.btnReplayNotif, replayPendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.btnForwardNotif, forwardPendingIntent);
        notificationLayoutExpanded.setProgressBar(R.id.progressBarNotification, max, progress, false);
        notificationLayoutExpanded.setTextViewText(R.id.songTitleNotif, songTitle);
        notificationLayoutExpanded.setTextViewText(R.id.ArtistNotif, artist);
        notificationLayoutExpanded.setTextViewText(R.id.textCurrentTime, formatTime(progress));
        notificationLayoutExpanded.setTextViewText(R.id.textTotalTime, formatTime(max));
        int playPauseIconE = isPlaying() ? R.drawable.baseline_pause_24_notif : R.drawable.baseline_play_arrow_notif;
        notificationLayoutExpanded.setImageViewResource(R.id.playPauseBtn, playPauseIconE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(songTitle)
                .setContentText(artist)
                .setSmallIcon(R.drawable.logo)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.yellow))
                .setSound(null)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .build();
    }

    private PendingIntent createPlaybackActionIntent(String action) {
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private void updateNotification(SongUpdateEvent event) {
        createNotificationChannel();
        Notification notification = notification(event.getSongTitle(), event.getArtist(), getDuration(), getCurrentPosition());
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Foreground service channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mediaPlayer.stop();
//        stopForeground(true);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onNextEven(NextEvent event) {
        playNextSong();
    }

    @Subscribe
    public void onPrevEven(PrevEvent event) {
        playPrevSong();
    }

    @Subscribe
    public void onPlayEvent(PlayEvent event) {
        togglePlayback();
    }

    @Subscribe
    public void onPauseEvent(PauseEvent event) {
        mediaPlayer.pause();
    }

    @Subscribe
    public void onForwardEvent(ForwardEvent event) {
        int current = getCurrentPosition() + 10000;
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(current);
        }
    }

    @Subscribe
    public void onReplayEvent(ReplayEvent event) {
        int current = getCurrentPosition() - 10000;
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(current);
        }
    }

    @Subscribe
    public void onSeekBarPositionEvent(SeekBarPositionEvent event) {
        int seekBarPosition = event.getSeekBarPosition();
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(seekBarPosition);
        }
    }

    @Subscribe
    public void onSongUpdateEvent(SongUpdateEvent event) {
        updateNotification(event);
    }
}
