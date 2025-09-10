package com.printmate.PrintMate.Fragmenti;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.printmate.PrintMate.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainFragment extends Fragment {

    // Proslijeđeno iz Adaptera:
    private int printerId;
    private String printerName, baseUrl, apiKey, imageBase64;

    // UI reference:
    private TextView naslovView, statusOnlineView, tempNozzleView, tempBedView, fanSpeedView, zOffsetView;
    private ImageView printerImageView;
    private LineChart chart;

    // Za grafikon:
    private final List<Entry> toolEntries = new ArrayList<>();
    private final List<Entry> bedEntries = new ArrayList<>();
    private float targetTool = 200, targetBed = 60;

    // Networking:
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchTemperatures();
            handler.postDelayed(this, 1500); // svake 1.5 sekunde
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            printerId    = args.getInt("printer_id");
            printerName  = args.getString("printer_name");
            baseUrl      = args.getString("base_url");
            apiKey       = args.getString("api_key");
            imageBase64  = args.getString("printer_image_base64");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        // UI binding
        naslovView       = v.findViewById(R.id.textNaslovDetaljiAndroid);
        statusOnlineView = v.findViewById(R.id.statusOnline);
        tempNozzleView   = v.findViewById(R.id.temp_nozzle);
        tempBedView      = v.findViewById(R.id.temp_bed);
        fanSpeedView     = v.findViewById(R.id.fan_speed);
        zOffsetView      = v.findViewById(R.id.z_offset);
        printerImageView = v.findViewById(R.id.printerImage);
        chart            = v.findViewById(R.id.temperature_chart);

        // Postavi naziv
        if (printerName != null) naslovView.setText(printerName);

        // Postavi sliku printera iz base64 stringa
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                printerImageView.setImageBitmap(bmp);
            } catch (Exception e) {
                printerImageView.setImageResource(R.drawable.baseline_no_photography_24);
            }
        } else {
            printerImageView.setImageResource(R.drawable.baseline_no_photography_24);
        }

        setupChart();
        handler.post(refreshRunnable);

        return v;
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(true);
        chart.setBorderWidth(1f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.printMateWhite));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(300f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.input_secondary));

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(13f);
        legend.setDrawInside(false);
        legend.setYOffset(8f);
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.input_secondary));

        updateLimitLines();
    }

    private void updateLimitLines() {
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines();

        int toolLineColor = ContextCompat.getColor(requireContext(), R.color.tool_line_color);
        int bedLineColor = ContextCompat.getColor(requireContext(), R.color.bed_line_color);


        LimitLine targetToolLine = new LimitLine(targetTool, "Target T: " + targetTool + "°C");
        targetToolLine.setLineColor(toolLineColor);
        targetToolLine.setLineWidth(1.5f);
        targetToolLine.enableDashedLine(8, 8, 0);
        targetToolLine.setTextColor(toolLineColor);

        LimitLine targetBedLine = new LimitLine(targetBed, "Target Bed: " + targetBed + "°C");
        targetBedLine.setLineColor(bedLineColor);
        targetBedLine.setLineWidth(1.5f);
        targetBedLine.enableDashedLine(8, 8, 0);
        targetBedLine.setTextColor(bedLineColor);

        leftAxis.addLimitLine(targetToolLine);
        leftAxis.addLimitLine(targetBedLine);
    }

    private void fetchTemperatures() {
        if (baseUrl == null || apiKey == null) return;
        String url = baseUrl + "/api/printer";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Api-Key", apiKey)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("OctoPrint", "API fetch failed", e);
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    String res = response.body().string();
                    JSONObject obj = new JSONObject(res);

                    final float actualTool = (float) obj.getJSONObject("temperature").getJSONObject("tool0").getDouble("actual");
                    final float targetToolApi = (float) obj.getJSONObject("temperature").getJSONObject("tool0").getDouble("target");
                    final float actualBed = (float) obj.getJSONObject("temperature").getJSONObject("bed").getDouble("actual");
                    final float targetBedApi = (float) obj.getJSONObject("temperature").getJSONObject("bed").getDouble("target");

                    // Dummy podaci za fan i z offset (zamijeni pravim ako imaš u API-ju!)
                    final int fanSpeed = obj.has("fan_speed") ? obj.getInt("fan_speed") : 0;
                    final double zOffset = obj.has("z_offset") ? obj.getDouble("z_offset") : -0.60;

                    requireActivity().runOnUiThread(() -> {
                        float x = toolEntries.size();

                        toolEntries.add(new Entry(x, actualTool));
                        bedEntries.add(new Entry(x, actualBed));
                        if (toolEntries.size() > 1800) { // 45min ako je svakih 1.5s
                            toolEntries.remove(0);
                            bedEntries.remove(0);
                        }

                        targetTool = targetToolApi;
                        targetBed = targetBedApi;
                        updateChart();

                        tempNozzleView.setText(String.format(Locale.US, "Temp dizne: %.1f °C", actualTool));
                        tempBedView.setText(String.format(Locale.US, "Temp podloge: %.1f °C", actualBed));
                        fanSpeedView.setText(String.format(Locale.US, "Brzina ventilatora: %d", fanSpeed));
                        zOffsetView.setText(String.format(Locale.US, "Z-offset: %.2f", zOffset));
                        statusOnlineView.setText("Online"); // ili prema tvom API statusu
                    });
                } catch (Exception e) {
                    Log.e("OctoPrint", "Parse error", e);
                }
            }
        });
    }

    private void updateChart() {
        updateLimitLines();

        int toolColor = ContextCompat.getColor(requireContext(), R.color.tool_line_color);
        int bedColor = ContextCompat.getColor(requireContext(), R.color.bed_line_color);

        LineDataSet toolSet = new LineDataSet(toolEntries, "Actual T");
        toolSet.setColor(toolColor);
        toolSet.setLineWidth(2f);
        toolSet.setDrawCircles(false);
        toolSet.setDrawValues(false);
        toolSet.setValueTextColor(toolColor); // boja za vrijednosti ako ih prikazuješ

        LineDataSet bedSet = new LineDataSet(bedEntries, "Actual Bed");
        bedSet.setColor(bedColor);
        bedSet.setLineWidth(2f);
        bedSet.setDrawCircles(false);
        bedSet.setDrawValues(false);
        bedSet.setValueTextColor(bedColor);

        LineData data = new LineData(toolSet, bedSet);
        chart.setData(data);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(refreshRunnable);
    }
}
