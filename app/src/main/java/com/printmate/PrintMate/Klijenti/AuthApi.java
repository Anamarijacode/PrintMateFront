package com.printmate.PrintMate.Klijenti;

import com.printmate.PrintMate.Modeli.AuthResponse;
import com.printmate.PrintMate.Modeli.DostupanMaterijal;
import com.printmate.PrintMate.Modeli.Gkod;
import com.printmate.PrintMate.Modeli.GoogleLoginRequest;
import com.printmate.PrintMate.Modeli.LoginRequest;
import com.printmate.PrintMate.Modeli.PopisZaKupovinuStavka;
import com.printmate.PrintMate.Modeli.Printer;
import com.printmate.PrintMate.Modeli.PrinterHome;
import com.printmate.PrintMate.Modeli.Proizvodjac;
import com.printmate.PrintMate.Modeli.RegisterRequest;
import com.printmate.PrintMate.Modeli.RezervniDio;
import com.printmate.PrintMate.Modeli.SlikaModelaDto;
import com.printmate.PrintMate.Modeli.StatistikaDomain;
import com.printmate.PrintMate.Modeli.UploadResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Multipart;
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

    // === DODAJ OVO: ===
    @DELETE("api/home/printer/{id}")
    Call<Void> deletePrinter(@Path("id") int id);
    @Multipart
    @POST("api/GCode/upload")
    Call<ResponseBody> uploadGkod(
            @Part MultipartBody.Part file,
            @Part("naziv") RequestBody naziv,
            @Part("idUsera") RequestBody userId
    );

    @GET("api/home/gKod")
    Call<List<Gkod>> getGkodList();
    @GET("api/GCode/{id}/text")
    Call<String> getGcodeText(@Path("id") int id);
    @Multipart
    @POST("api/SlikaModela/upload")
    Call<UploadResponse> upload(
            @Part MultipartBody.Part file,
            @Part("gKodId") RequestBody gKodId
    );

    @GET("api/SlikaModela/gcode/{gKodId}")
    Call<List<SlikaModelaDto>> getByGCode(@Path("gKodId") int gKodId);

    @GET("api/SlikaModela/{id}")
    Call<ResponseBody> getImage(@Path("id") int idSlike);
    // with this:
    @GET("statistika/gcode/{id}")
    Call<List<StatistikaDomain>> getStatistikaByGCode(@Path("id") int gcodeId);

    @GET("gcode/{id}/pdf")
    Call<byte[]> generatePdfByGCode(@Path("id") int gcodeId);

    @GET("statistika/cijena/excel/{id}")
    Call<byte[]> generateCijenaPrintaExcel(@Path("id") int gcodeId);
    @POST("gcode/{id}/start")
    Call<Void> startPrintAndRecord(@Path("id") int gcodeId);

    /** Dohvati sve dostupne materijale */
    @GET("dostupanMaterijal")
    Call<List<DostupanMaterijal>> getMaterials();

    /** Dodaj novi materijal */
    @POST("dostupanMaterijal")
    Call<DostupanMaterijal> addMaterial(@Body DostupanMaterijal newMaterial);


    // ----- Rezervni dijelovi -----

    /** Dohvati sve rezervne dijelove */
    @GET("rezervniDio")
    Call<List<RezervniDio>> getSpareParts();

    /** Dodaj novi rezervni dio */
    @POST("rezervniDio")
    Call<RezervniDio> addSparePart(@Body RezervniDio newPart);


    // ----- Stavke popisa za kupovinu -----

    /** Dohvati sve stavke iz popisa za kupovinu */
    @GET("popisZaKupovinuStavka")
    Call<List<PopisZaKupovinuStavka>> getShoppingList();

    /** Dodaj novu stavku u popis za kupovinu */
    @POST("popisZaKupovinuStavka")
    Call<PopisZaKupovinuStavka> addShoppingListItem(@Body PopisZaKupovinuStavka newItem);
}
