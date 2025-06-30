package com.printmate.PrintMate.Adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.printmate.PrintMate.Activity.GCodeDetaljiActivity;
import com.printmate.PrintMate.Modeli.Gkod;
import com.printmate.PrintMate.R;

import java.util.List;

public class GKodArhivaAdapter extends RecyclerView.Adapter<GKodArhivaAdapter.ViewHolder> {

    private final List<Gkod> gkodList;
    private final String baseUrl;
    private final String apiKey;
    private final OnItemClickListener listener;

    public GKodArhivaAdapter(List<Gkod> gkodList,
                             String baseUrl,
                             String apiKey,
                             OnItemClickListener listener) {
        this.gkodList = gkodList;
        this.baseUrl  = baseUrl;
        this.apiKey   = apiKey;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gkode, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gkod g = gkodList.get(position);
        holder.name.setText(g.getNaziv());
        // TODO: load thumbnail into holder.imgThumb if available

        holder.itemView.setOnClickListener(v -> {
            Log.d("GKodArhivaAdapter", "Clicked archive G-KOD id: " + g.getId());
            Intent intent = new Intent(v.getContext(), GCodeDetaljiActivity.class);
            intent.putExtra("GKOD_ID",   g.getId());
            intent.putExtra("GKOD_NAME", g.getNaziv());
            intent.putExtra("BASE_URL",  baseUrl);
            intent.putExtra("API_KEY",   apiKey);
            v.getContext().startActivity(intent);

            if (listener != null) listener.onItemClick(g);
        });
    }

    @Override
    public int getItemCount() {
        return gkodList != null ? gkodList.size() : 0;
    }

    public void updateData(List<Gkod> novi) {
        gkodList.clear();
        gkodList.addAll(novi);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Gkod gkod);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView imgThumb;

        ViewHolder(@NonNull View v) {
            super(v);
            imgThumb = v.findViewById(R.id.imgGkodThumb);
            name     = v.findViewById(R.id.txtGkodName);
        }
    }
}
