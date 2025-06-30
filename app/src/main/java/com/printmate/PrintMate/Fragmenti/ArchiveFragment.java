package com.printmate.PrintMate.Fragmenti;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.printmate.PrintMate.Adapter.GKodArhivaAdapter;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Modeli.Gkod;
import com.printmate.PrintMate.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArchiveFragment extends Fragment {

    private RecyclerView recyclerView;
    private GKodArhivaAdapter adapter;
    private SearchView searchView;
    private final List<Gkod> masterList = new ArrayList<>();

    private String currentUserId;
    private String baseUrl;
    private String apiKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("auth", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", "");
        apiKey        = prefs.getString("api_key", "");
        baseUrl       = prefs.getString("base_url", "");

        Log.d("ArchiveFragment", "CurrentUserId iz prefs: '" + currentUserId + "'");
        Toast.makeText(requireContext(),
                "UID='" + currentUserId + "'",
                Toast.LENGTH_SHORT).show();
    }

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_archive, container, false);

        recyclerView = v.findViewById(R.id.gkodRecyclerView);
        searchView   = v.findViewById(R.id.searchViewProizvodjacArhiva);

        // ← use the archive adapter here
        adapter = new GKodArhivaAdapter(
                new ArrayList<>(),
                baseUrl,
                apiKey,
                gkod -> {
                    // dodatna logika po kliku (ako je potrebna)
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fetchArchive();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                applyFilter(query);
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                applyFilter(newText);
                return true;
            }
        });

        return v;
    }

    private void fetchArchive() {
        AuthApi api = ApiClient.getAuthApi();
        api.getGkodList().enqueue(new Callback<List<Gkod>>() {
            @Override
            public void onResponse(Call<List<Gkod>> call, Response<List<Gkod>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(),
                            "Neuspjelo preuzimanje arhive",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Gkod> all = response.body();
                Log.d("ArchiveFragment", "Server vratio " + all.size() + " GKOD-ova");
                Toast.makeText(getContext(),
                        "Server vratio " + all.size() + " stavki",
                        Toast.LENGTH_SHORT).show();

                masterList.clear();
                for (Gkod g : all) {
                    if (String.valueOf(g.getUserId()).equals(currentUserId)) {
                        masterList.add(g);
                    }
                }

                Log.d("ArchiveFragment", "Nakon filtera: " + masterList.size());
                adapter.updateData(masterList);
            }

            @Override
            public void onFailure(Call<List<Gkod>> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Greška: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter(String query) {
        String lower = query.toLowerCase().trim();
        List<Gkod> filtered = new ArrayList<>();
        for (Gkod g : masterList) {
            if (g.getNaziv().toLowerCase().contains(lower)) {
                filtered.add(g);
            }
        }
        adapter.updateData(filtered);
    }
}
