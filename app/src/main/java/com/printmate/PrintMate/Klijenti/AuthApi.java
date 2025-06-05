package com.printmate.PrintMate.Klijenti;

import com.printmate.PrintMate.Modeli.AuthResponse;
import com.printmate.PrintMate.Modeli.GoogleLoginRequest;
import com.printmate.PrintMate.Modeli.LoginRequest;
import com.printmate.PrintMate.Modeli.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("api/Auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/Auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/Auth/google-login")
    Call<AuthResponse> googleLogin(@Body GoogleLoginRequest request);
}