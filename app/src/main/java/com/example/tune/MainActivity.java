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

import com.example.tune.Event.SongUpdateEvent;
import com.example.tune.adapter.MyAdapter;
import com.example.tune.model.Item;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    List<Item> items = new ArrayList<Item>();
    Item clickedItem;
    MyAdapter adapter;
    RecyclerView recyclerView;
    private MockApi mockApi;
    SharedPreferences sharedPreferences;
    private static String SHARED_PREFERENCES_NAME = "myPreference";
    private static final String KEY_SONG_TITLE = "SongTitle";
    public static final String KEY_ARTIST = "Artist";
    private static final int LOADING_VIEW = 1;
    private static final int RECYCLER_VIEW = 2;
    private int currentView = LOADING_VIEW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showLoadingView();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://6152fa45c465200017d1a8e3.mockapi.io/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mockApi = retrofit.create(MockApi.class);

        getItems();
    }

    private void showRecyclerView() {
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(getApplicationContext(), items);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this::onItemClick);
        currentView = RECYCLER_VIEW;
    }

    private void showLoadingView() {
        setContentView(R.layout.layout_loading);
        currentView = LOADING_VIEW;
    }

    private void getItems() {
        Call<List<Item>> call = mockApi.getItem();

        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if(!response.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    return;
                }
                items.clear();
                items.addAll(response.body());
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
            public void onFailure(Call<List<Item>> call, Throwable t) {
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
        clickedItem = items.get(position);

        Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
        intent.putExtra("SONG_LIST", new ArrayList<>(items));
        intent.putExtra("CURRENT_SONG_INDEX", position);
        Intent intentActivity = new Intent(MainActivity.this, SongDetailScreenActivity.class);
        intent.putExtra("SONG_LIST", new ArrayList<>(items));
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
}