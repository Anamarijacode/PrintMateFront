package com.printmate.PrintMate.Fragmenti;

import android.content.Intent;
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
import com.printmate.PrintMate.Modeli.Printer;
import com.printmate.PrintMate.Modeli.PrinterHome;
import com.printmate.PrintMate.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private RecyclerView rv;
    private View empty;
    private FloatingActionButton fab;
    private PrinterHomeAdapter adapter;
    private List<PrinterHome> list = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        rv    = v.findViewById(R.id.printerRecyclerView);
        empty = v.findViewById(R.id.emptyStateLayout);
        fab   = v.findViewById(R.id.fabAddPrinter);

        // Tap on empty state also opens "add printer"
        empty.setOnClickListener(x ->
                startActivity(new Intent(getContext(), OdabirActicity.class))
        );

        // RecyclerView setup
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PrinterHomeAdapter(list);
        rv.setAdapter(adapter);

        // FAB opens "add printer"
        fab.setOnClickListener(x ->
                startActivity(new Intent(getContext(), OdabirActicity.class))
        );

        loadPrinters();
        return v;
    }

    private void loadPrinters() {
        AuthApi api = ApiClient.getAuthApi();
        api.getPrinters().enqueue(new Callback<List<PrinterHome>>() {
            @Override
            public void onResponse(Call<List<PrinterHome>> call, Response<List<PrinterHome>> response) {
                List<PrinterHome> homes = response.body();

                if (homes == null || homes.isEmpty()) {
                    // prazan slučaj: prikaži empty-state, sakrij RecyclerView i FAB
                    empty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                    fab.hide();
                    return;
                }

                // imamo printere: prikaži RecyclerView i FAB, sakrij empty-state
                empty.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                fab.show();

                list.clear();
                list.addAll(homes);
                adapter.notifyDataSetChanged();

                // za svaki printer dohvat modela i logo slike
                for (int i = 0; i < list.size(); i++) {
                    final int pos = i;
                    PrinterHome ph = list.get(i);
                    api.getPrinterModelById(ph.getModelPrintera()).enqueue(new Callback<Printer>() {
                        @Override
                        public void onResponse(Call<Printer> call2, Response<Printer> resp2) {
                            Printer model = resp2.body();
                            if (model != null && model.getSlikaPrinteraBase64() != null) {
                                byte[] data = Base64.decode(model.getSlikaPrinteraBase64(), Base64.DEFAULT);
                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                ph.setLogoBitmap(bmp);
                            }
                            ph.setStatus(ph.isOnline() ? "Online" : "Offline");
                            requireActivity().runOnUiThread(() -> adapter.notifyItemChanged(pos));
                        }

                        @Override
                        public void onFailure(Call<Printer> call2, Throwable t) {
                            // ignoriramo grešku pri učitavanju modela/slike
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<PrinterHome>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
