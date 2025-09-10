package com.printmate.PrintMate.Klijenti;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    public static final String BASE_URL = "http://31.147.206.25:8082/";

    public static AuthApi getAuthApi() {
        if (retrofit == null) {
            // Podesi custom OkHttpClient s dužim timeoutovima
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)   // za uspostavu veze
                    .writeTimeout(60, TimeUnit.SECONDS)     // za upload (slanje tijela)
                    .readTimeout(60, TimeUnit.SECONDS)      // za odgovor
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(AuthApi.class);
    }
}
