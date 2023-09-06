package com.example.tune.model.songlist;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface SongListApiService {

    @GET("songs")
    Call<SongResponse> getSongList(@Header("Authorization") String token);
}
