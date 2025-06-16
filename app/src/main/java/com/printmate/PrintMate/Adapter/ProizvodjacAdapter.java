package com.printmate.PrintMate.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.printmate.PrintMate.Modeli.Proizvodjac;
import com.printmate.PrintMate.R;

import java.util.List;

public class ProizvodjacAdapter extends RecyclerView.Adapter<ProizvodjacAdapter.ViewHolder> {

    private List<Proizvodjac> proizvodjaci;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Proizvodjac proizvodjac);
    }

    public ProizvodjacAdapter(List<Proizvodjac> proizvodjaci, Context context, OnItemClickListener listener) {
        this.proizvodjaci = proizvodjaci;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_proizvodac, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Proizvodjac p = proizvodjaci.get(position);
        holder.naziv.setText(p.getNaziv());
        holder.slika.setImageResource(p.getSlikaResId());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(p));
    }

    @Override
    public int getItemCount() {
        return proizvodjaci.size();
    }

    public void filterList(List<Proizvodjac> filteredList) {
        proizvodjaci = filteredList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView naziv;
        ImageView slika;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            naziv = itemView.findViewById(R.id.textProizvodjac);
            slika = itemView.findViewById(R.id.imageProizvodjaci);
        }
    }
}
