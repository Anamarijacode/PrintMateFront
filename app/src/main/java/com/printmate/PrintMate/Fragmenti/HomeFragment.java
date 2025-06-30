// HomeFragment.java
package com.printmate.PrintMate.Fragmenti;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.printmate.PrintMate.Activity.OdabirActicity;
import com.printmate.PrintMate.Adapter.PrinterHomeAdapter;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Klijenti.OctoPrintController;
import com.printmate.PrintMate.Modeli.Printer;
import com.printmate.PrintMate.Modeli.PrinterHome;
import com.printmate.PrintMate.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    private RecyclerView rv;
    private View empty;
    private FloatingActionButton fab;
    private PrinterHomeAdapter adapter;
    private final List<PrinterHome> list = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        rv    = v.findViewById(R.id.printerRecyclerView);
        empty = v.findViewById(R.id.emptyStateLayout);
        fab   = v.findViewById(R.id.fabAddPrinter);

        empty.setOnClickListener(x ->
                startActivity(new Intent(getContext(), OdabirActicity.class))
        );
        fab.setOnClickListener(x ->
                startActivity(new Intent(getContext(), OdabirActicity.class))
        );

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PrinterHomeAdapter(list);
        rv.setAdapter(adapter);

        loadPrinters();
        return v;
    }

    private void loadPrinters() {
        // 1) Učitaj current user_id iz SharedPreferences
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("user_id", "");

        // 2) Retrofit poziv za listu printera
        AuthApi api = ApiClient.getAuthApi();
        api.getPrinters().enqueue(new retrofit2.Callback<List<PrinterHome>>() {
            @Override
            public void onResponse(
                    retrofit2.Call<List<PrinterHome>> call,
                    retrofit2.Response<List<PrinterHome>> response
            ) {
                List<PrinterHome> homes = response.body();
                if (homes == null || homes.isEmpty()) {
                    showEmpty();
                    return;
                }

                // 3) Filtriraj samo printere ovog usera
                List<PrinterHome> filtered = new ArrayList<>();
                for (PrinterHome ph : homes) {
                    if (currentUserId.equals(ph.getUserId())) {
                        filtered.add(ph);
                    }
                }
                if (filtered.isEmpty()) {
                    showEmpty();
                    return;
                }

                showList();
                list.clear();
                list.addAll(filtered);
                adapter.notifyDataSetChanged();

                // 4) Za svaki printer dohvaćaj model/sliku i pingaj OctoPrint
                for (int i = 0; i < list.size(); i++) {
                    final int pos = i;
                    PrinterHome ph = list.get(i);

                    // Retrofit: dohvat modela i slike
                    api.getPrinterModelById(ph.getModelPrintera())
                            .enqueue(new retrofit2.Callback<Printer>() {
                                @Override
                                public void onResponse(
                                        retrofit2.Call<Printer> call2,
                                        retrofit2.Response<Printer> resp2
                                ) {
                                    Printer model = resp2.body();
                                    if (model != null && model.getSlikaPrinteraBase64() != null) {
                                        byte[] data = Base64.decode(
                                                model.getSlikaPrinteraBase64(),
                                                Base64.DEFAULT
                                        );
                                        Bitmap bmp = BitmapFactory.decodeByteArray(
                                                data, 0, data.length
                                        );
                                        ph.setLogoBitmap(bmp);
                                    }
                                    // inicijalni status prije plena
                                    ph.setStatus(ph.isOnline() ? "Online" : "Offline");
                                    requireActivity().runOnUiThread(() ->
                                            adapter.notifyItemChanged(pos)
                                    );
                                }
                                @Override
                                public void onFailure(
                                        retrofit2.Call<Printer> call2,
                                        Throwable t
                                ) {
                                    // ignoriraj grešku pri učitavanju slike
                                }
                            });

                    // OkHttp: ping OctoPrint za realni online/offline status
                    OctoPrintController octo =
                            new OctoPrintController(ph.getBaseUrl(), ph.getApiKey());
                    octo.checkConnection(new Callback() {
                        @Override
                        public void onResponse(Call call, Response response) {
                            ph.setStatus(response.isSuccessful() ? "Online" : "Offline");
                            requireActivity().runOnUiThread(() ->
                                    adapter.notifyItemChanged(pos)
                            );
                        }
                        @Override
                        public void onFailure(Call call, IOException e) {
                            ph.setStatus("Offline");
                            requireActivity().runOnUiThread(() ->
                                    adapter.notifyItemChanged(pos)
                            );
                        }
                    });
                }
            }

            @Override
            public void onFailure(
                    retrofit2.Call<List<PrinterHome>> call,
                    Throwable t
            ) {
                Toast.makeText(getContext(),
                        "Greška: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }

            private void showEmpty() {
                empty.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
                fab.hide();
            }
            private void showList() {
                empty.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                fab.show();
            }
        });
    }
}
