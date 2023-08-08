package com.example.tune;

import com.example.tune.model.Item;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MockApi {
    @GET("spotipi")
    Call<List<Item>> getItem();

}
