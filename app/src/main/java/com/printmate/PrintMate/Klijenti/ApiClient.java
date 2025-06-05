package com.printmate.PrintMate.Klijenti;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static AuthApi getAuthApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://192.168.76.176:44311/") // Zamijeni s pravim IP-om ako testiraš lokalno
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(AuthApi.class);
    }
}
