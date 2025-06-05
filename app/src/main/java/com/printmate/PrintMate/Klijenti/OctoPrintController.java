package com.printmate.PrintMate.Klijenti;
import okhttp3.*;

public class OctoPrintController {

    private static final String BASE_URL = "https://shared-IXOUV9P3S1FP61F6XWR9AJ5X5LY7HVXS.octoeverywhere.com"; // tvoj link
    private static final String API_KEY = "AWp7HvlUgLO2tXC14cLr3LaUvgsA1wtVsOX9mhSbmMM"; // tvoj OctoPrint API key
///ovo je samo mala promjena da bivim dali se sve sprema
    private final OkHttpClient client = new OkHttpClient();

    public void sendCommand(String commandJson, Callback callback) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(commandJson, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/job")
                .post(body)
                .addHeader("X-Api-Key", API_KEY)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void startPrint(Callback callback) {
        sendCommand("{ \"command\": \"start\" }", callback);
    }

    public void pausePrint(Callback callback) {
        sendCommand("{ \"command\": \"pause\", \"action\": \"pause\" }", callback);
    }

    public void resumePrint(Callback callback) {
        sendCommand("{ \"command\": \"pause\", \"action\": \"resume\" }", callback);
    }

    public void cancelPrint(Callback callback) {
        sendCommand("{ \"command\": \"cancel\" }", callback);
    }
}
