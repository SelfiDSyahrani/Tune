package com.example.tune.model.Register;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RegisterApiService {
    @POST("user")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);
}
