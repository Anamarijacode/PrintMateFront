package com.printmate.PrintMate.Klijenti;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import okhttp3.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kontroler za komunikaciju s OctoPrint API-jem i opcionalnim backend servisom.
 */
public class OctoPrintController {
    private final HttpUrl octoBaseUrl;
    private final HttpUrl backendBaseUrl;
    private final String apiKey;
    private final OkHttpClient client;

    /**
     * Konstruktor s vlastitim backend servisom.
     *
     * @param octoBaseUrl    URL OctoPrint instance
     * @param apiKey         API ključ za OctoPrint
     * @param backendBaseUrl URL vašeg backend servisa (ili null)
     */
    public OctoPrintController(String octoBaseUrl, String apiKey, String backendBaseUrl) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
        String octo = ensureScheme(octoBaseUrl);
        HttpUrl parsedOcto = HttpUrl.parse(octo);
        if (parsedOcto == null) {
            throw new IllegalArgumentException("Neispravan OctoPrint URL: " + octoBaseUrl);
        }
        this.octoBaseUrl = parsedOcto;
        if (backendBaseUrl != null) {
            String backend = ensureScheme(backendBaseUrl);
            HttpUrl parsedBackend = HttpUrl.parse(backend);
            if (parsedBackend == null) {
                throw new IllegalArgumentException("Neispravan backend URL: " + backendBaseUrl);
            }
            this.backendBaseUrl = parsedBackend;
        } else {
            this.backendBaseUrl = null;
        }
    }

    /**
     * Konstruktor bez vlastitog backend servisa.
     */
    public OctoPrintController(String octoBaseUrl, String apiKey) {
        this(octoBaseUrl, apiKey, null);
    }

    /**
     * Ako URL ne sadrži shemu, doda http://
     */
    private static String ensureScheme(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return "http://" + url;
    }

    /**
     * GET /api/version – provjera dostupnosti OctoPrinta.
     */
    public void checkConnection(Callback callback) {
        Request request = new Request.Builder()
                .url(octoBaseUrl.newBuilder().addPathSegments("api/version").build())
                .get()
                .addHeader("X-Api-Key", apiKey)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * GET /api/printer – dohvaća stanje printera s history=true.
     */
    public void getPrinterState(Callback callback) {
        HttpUrl url = octoBaseUrl.newBuilder()
                .addPathSegments("api/printer")
                .addQueryParameter("history", "true")
                .addQueryParameter("limit", "100")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-Api-Key", apiKey)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * GET /api/settings – dohvaća sve postavke printera.
     */
    public void getPrinterSettings(Callback callback) {
        Request request = new Request.Builder()
                .url(octoBaseUrl.newBuilder().addPathSegments("api/settings").build())
                .get()
                .addHeader("X-Api-Key", apiKey)
                .build();
        client.newCall(request).enqueue(callback);
    }

    // Pomoćna metoda za slanje job komandi
    private void sendJobCommand(String json, Callback callback) {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(octoBaseUrl.newBuilder().addPathSegments("api/job").build())
                .post(body)
                .addHeader("X-Api-Key", apiKey)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /** Pokreni ispis */
    public void startPrint(Callback callback) {
        sendJobCommand("{\"command\":\"start\"}", callback);
    }

    /** Pauziraj ispis */
    public void pausePrint(Callback callback) {
        sendJobCommand("{\"command\":\"pause\",\"action\":\"pause\"}", callback);
    }

    /** Nastavi pauzirani ispis */
    public void resumePrint(Callback callback) {
        sendJobCommand("{\"command\":\"pause\",\"action\":\"resume\"}", callback);
    }

    /** Otkazivanje ispisa */
    public void cancelPrint(Callback callback) {
        sendJobCommand("{\"command\":\"cancel\"}", callback);
    }

    /**
     * Kombinirano: pošalji start na OctoPrint, zatim zabilježi start događaj na backendu.
     */
    public void startPrintAndRecord(int gcodeId, Callback callback) {
        startPrint(new Callback() {
            @Override public void onFailure(Call call, IOException e) { callback.onFailure(call, e); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure(call, new IOException("OctoPrint returned " + response.code()));
                } else {
                    recordStatsOnBackend(gcodeId, callback);
                }
            }
        });
    }

    // POST prazno tijelo na /gcode/{id}/start
    private void recordStatsOnBackend(int gcodeId, Callback callback) {
        if (backendBaseUrl == null) {
            callback.onFailure(null, new IOException("Backend URL nije konfiguriran"));
            return;
        }
        Request request = new Request.Builder()
                .url(backendBaseUrl.newBuilder().addPathSegments("gcode/"+gcodeId+"/start").build())
                .post(RequestBody.create(new byte[0], null))
                .build();
        client.newCall(request).enqueue(callback);
    }

    // Pomoćna metoda za custom GCODE komande
    private void sendPrinterCommand(String json, Callback callback) {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(octoBaseUrl.newBuilder().addPathSegments("api/printer/command").build())
                .post(body)
                .addHeader("X-Api-Key", apiKey)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /** Postavi feedrate (%) */
    public void setFeedrate(int factor, Callback callback) {
        sendPrinterCommand("{\"command\":\"feedrate\",\"factor\":"+factor+"}", callback);
    }

    /** Postavi brzinu ventilatora (0–255) */
    public void setFanSpeed(int value, Callback callback) {
        sendPrinterCommand("{\"command\":\"M106\",\"S\":"+value+"}", callback);
    }

    /** Postavi temperaturu dizne */
    public void setNozzleTemp(int temp, Callback callback) {
        String json = "{\"command\":\"target\",\"targets\":{\"tool0\":"+temp+"}}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request req = new Request.Builder()
                .url(octoBaseUrl.newBuilder().addPathSegments("api/printer/tool").build())
                .post(body)
                .addHeader("X-Api-Key", apiKey)
                .build();
        client.newCall(req).enqueue(callback);
    }

    /** Postavi temperaturu podloge */
    public void setBedTemp(int temp, Callback callback) {
        String json = "{\"command\":\"target\",\"target\":"+temp+"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request req = new Request.Builder()
                .url(octoBaseUrl.newBuilder().addPathSegments("api/printer/bed").build())
                .post(body)
                .addHeader("X-Api-Key", apiKey)
                .build();
        client.newCall(req).enqueue(callback);
    }

    /** Postavi Z-offset */
    public void setZOffset(float zoffset, Callback callback) {
        sendPrinterCommand("{\"commands\":[\"M851 Z"+zoffset+"\",\"M500\"]}", callback);
    }

    /** Dohvati history, izračunaj statistiku i pošalji na backend */
    public void fetchAndRecordStats(int gcodeId, Callback backendCallback) {
        getPrinterState(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                backendCallback.onFailure(call, e);
            }
            @Override public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    backendCallback.onFailure(call, new IOException("Printer API returned " + response.code()));
                    return;
                }
                String json = response.body().string();
                try {
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                    // temperatura
                    List<Double> tempVals = new ArrayList<>();
                    JsonObject tempObj = root.getAsJsonObject("temperature");
                    if (tempObj!=null && tempObj.has("history")) {
                        for (JsonElement el : tempObj.getAsJsonArray("history")) {
                            JsonObject e2 = el.getAsJsonObject();
                            if (e2.has("tool0") && e2.get("tool0").isJsonObject()) {
                                JsonObject tool = e2.getAsJsonObject("tool0");
                                if (tool.has("actual") && !tool.get("actual").isJsonNull()) {
                                    tempVals.add(tool.get("actual").getAsDouble());
                                }
                            }
                        }
                    }
                    if (tempVals.isEmpty()) tempVals.add(0.0);
                    double minT = Collections.min(tempVals);
                    double maxT = Collections.max(tempVals);
                    double avgT = tempVals.stream().mapToDouble(d->d).average().orElse(0);
                    // feedrate
                    List<Double> feedVals = new ArrayList<>();
                    JsonObject feedObj = root.getAsJsonObject("feedrate");
                    if (feedObj!=null && feedObj.has("history")) {
                        for (JsonElement el : feedObj.getAsJsonArray("history")) {
                            JsonObject e2 = el.getAsJsonObject();
                            if (e2.has("feedrate") && !e2.get("feedrate").isJsonNull()) {
                                feedVals.add(e2.get("feedrate").getAsDouble());
                            }
                        }
                    }
                    if (feedVals.isEmpty()) feedVals.add(0.0);
                    double minF = Collections.min(feedVals);
                    double maxF = Collections.max(feedVals);
                    double avgF = feedVals.stream().mapToDouble(d->d).average().orElse(0);
                    // materijal
                    double material = 0;
                    JsonObject job = root.getAsJsonObject("job");
                    if (job!=null && job.has("filament")) {
                        JsonObject filo = job.getAsJsonObject("filament");
                        if (filo.has("tool0")) {
                            JsonObject t0 = filo.getAsJsonObject("tool0");
                            if (t0.has("length") && !t0.get("length").isJsonNull()) {
                                material = t0.get("length").getAsDouble();
                            }
                        }
                    }
                    // izgradnja JSON-a
                    JsonObject stat = new JsonObject();
                    stat.addProperty("GKodId", gcodeId);
                    stat.addProperty("UkupnaPotrosnjaMaterijala", material);
                    long pt = root.getAsJsonObject("state").get("printTime").getAsLong();
                    stat.addProperty("UkupnoVrijemeRada", pt);
                    stat.addProperty("MinimalnaTemperatura", (int)minT);
                    stat.addProperty("MaximalnaTemperatura", (int)maxT);
                    stat.addProperty("ProsjecnaTemperatura", (int)avgT);
                    stat.addProperty("MinimalnaBrzinaIspisa", (int)minF);
                    stat.addProperty("MaximalnaBrzinaIspisa", (int)maxF);
                    stat.addProperty("ProsjecnaBrzinaIspisa", (int)avgF);
                    // POST to backend
                    if (backendBaseUrl==null) {
                        backendCallback.onFailure(call, new IOException("Backend URL nije konfiguriran"));
                        return;
                    }
                    RequestBody body = RequestBody.create(stat.toString(), MediaType.get("application/json; charset=utf-8"));
                    HttpUrl url2 = backendBaseUrl.newBuilder().addPathSegments("api/home/statistika").build();
                    Request req = new Request.Builder().url(url2).post(body).addHeader("X-Api-Key", apiKey).build();
                    client.newCall(req).enqueue(backendCallback);
                } catch (JsonSyntaxException ex) {
                    backendCallback.onFailure(call, new IOException("Parse error: " + ex.getMessage()));
                }
            }
        });
    }
}
