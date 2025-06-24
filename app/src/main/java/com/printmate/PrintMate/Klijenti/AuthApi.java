package com.printmate.PrintMate.Klijenti;

import com.printmate.PrintMate.Modeli.AuthResponse;
import com.printmate.PrintMate.Modeli.GoogleLoginRequest;
import com.printmate.PrintMate.Modeli.LoginRequest;
import com.printmate.PrintMate.Modeli.Printer;
import com.printmate.PrintMate.Modeli.PrinterHome;
import com.printmate.PrintMate.Modeli.Proizvodjac;
import com.printmate.PrintMate.Modeli.RegisterRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AuthApi {
    @POST("api/Auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/Auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/Auth/google-login")
    Call<AuthResponse> googleLogin(@Body GoogleLoginRequest request);
    @GET("api/home/proizvodjacPrintera")
    Call<List<Proizvodjac>> getProizvodjaci();
    @GET("api/home/modelPrintera")
    Call<List<Printer>> getModelPrintera(@Query("idProizvodjaca") int id);
    @GET("api/home/printer")
    Call<List<PrinterHome>> getPrinters();

    // Dodavanje novog korisničkog printera
    @POST("api/home/printer")
    Call<PrinterHome> addPrinter(@Body PrinterHome printer);

    // Dohvat modela printera (iz kojeg vadiš Base64 sliku)
    @GET("api/home/modelPrintera/{modelId}")
    Call<Printer> getPrinterModelById(@Path("modelId") int modelId);
}