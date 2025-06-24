package com.printmate.PrintMate.Klijenti;

import okhttp3.*;

import java.io.IOException;

public class OctoPrintController {
    private final OkHttpClient client;
    private final String baseUrl;
    private final String apiKey;

    public OctoPrintController(String baseUrl, String apiKey) {
        this.baseUrl  = baseUrl;
        this.apiKey   = apiKey;
        this.client   = new OkHttpClient();
    }

    /** GET /api/version samo da provjerimo je li printer živ */
    public void checkConnection(Callback callback) {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/version")
                .get()
                .addHeader("X-Api-Key", apiKey)
                .build();
        client.newCall(request).enqueue(callback);
    }

    // Ostavio sam ti i metode za kontrolu printanja...
    private void sendCommand(String commandJson, Callback callback) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(commandJson, JSON);

        Request request = new Request.Builder()
                .url(baseUrl + "/api/job")
                .post(body)
                .addHeader("X-Api-Key", apiKey)
                .build();

        client.newCall(request).enqueue(callback);
    }
    public void startPrint(Callback c)   { sendCommand("{\"command\":\"start\"}", c); }
    public void pausePrint(Callback c)   { sendCommand("{\"command\":\"pause\",\"action\":\"pause\"}", c); }
    public void resumePrint(Callback c)  { sendCommand("{\"command\":\"pause\",\"action\":\"resume\"}", c); }
    public void cancelPrint(Callback c)  { sendCommand("{\"command\":\"cancel\"}", c); }
}
