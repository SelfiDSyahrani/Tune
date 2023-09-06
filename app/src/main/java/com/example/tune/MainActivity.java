package com.example.tune;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.tune.adapter.MyAdapter;
import com.example.tune.model.songlist.Song;
import com.example.tune.model.songlist.SongListApiService;
import com.example.tune.model.songlist.SongResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
//    List<Item> items = new ArrayList<Item>();

    List<Song> songs = new ArrayList<Song>();
    Song clickedItem;
    MyAdapter adapter;
    RecyclerView recyclerView;
    private SongListApiService songApi;

    SharedPreferences sharedPreferences, spIsLoggedIn;
    private static String SHARED_PREFERENCES_NAME = "myPreference";
    private static final String KEY_SONG_TITLE = "SongTitle";
    public static final String KEY_ARTIST = "Artist";
    private static final int LOADING_VIEW = 1;
    private static final int RECYCLER_VIEW = 2;
    private int currentView = LOADING_VIEW;
    String accessToken, refreshToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showLoadingView();

        sharedPreferences = getSharedPreferences("MyTokenPrefs", Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString("access_token", "");
        refreshToken = sharedPreferences.getString("refresh_token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://473d-103-209-187-142.ngrok.io/tune/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        songApi = retrofit.create(SongListApiService.class);

        getItems();
    }

    private void showRecyclerView() {
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(getApplicationContext(), songs);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this::onItemClick);
        currentView = RECYCLER_VIEW;
    }

    private void showLoadingView() {
        setContentView(R.layout.layout_loading);
        currentView = LOADING_VIEW;
    }

    private void getItems() {
        Call<SongResponse> call = songApi.getSongList("Bearer " + accessToken);
        call.enqueue(new Callback<SongResponse>(){

            @Override
            public void onResponse(Call<SongResponse> call, Response<SongResponse> response) {
                if(!response.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
                songs.clear();
                SongResponse songResponse = response.body();
                if (songResponse != null) {
                    songs.addAll(songResponse.getSongs());
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                if (currentView == LOADING_VIEW) {
                    showRecyclerView();
                }
                //showFragment harus di sini
                if(foregroundIsRunning()){
                    showFragment(new MiniControlFragment());
                }

            }
            @Override
            public void onFailure(Call<SongResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error, please make sure your connection is stable", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getItems();
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentcontrol,fragment,"MiniControlFragment");
        fragmentTransaction.commit();
    }

    public void onItemClick(int position) {
        clickedItem = songs.get(position);
        Log.d("SONG", "onItemClick: " + clickedItem.getArtist());
        Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
        intent.putExtra("SONG_LIST", new ArrayList<>(songs));
        intent.putExtra("CURRENT_SONG_INDEX", position);
        Intent intentActivity = new Intent(MainActivity.this, SongDetailScreenActivity.class);
        intent.putExtra("SONG_LIST", new ArrayList<>(songs));
        intent.putExtra("CURRENT_SONG_INDEX", position);

        startActivity(intentActivity);
         {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

    public boolean foregroundIsRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(service.service.getClassName().equals(MediaPlayerService.class.getName())){
                return true;
            }
        }
        return false;
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyTokenPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("access_token");
        editor.remove("refresh_token");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}