package com.printmate.PrintMate.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.printmate.PrintMate.Modeli.Printer;
import com.printmate.PrintMate.R;

import java.util.List;

public class PrinterAdapter extends RecyclerView.Adapter<PrinterAdapter.VH> {

    public interface OnSelectListener {
        void onSelect(Printer printer);
    }

    private List<Printer> list;
    private final Context ctx;
    private final OnSelectListener listener;

    public PrinterAdapter(Context ctx, List<Printer> list, OnSelectListener listener) {
        this.ctx = ctx;
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_printer, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Printer p = list.get(pos);
        h.textNaziv.setText(p.getNaziv());
        Bitmap bmp = p.getLogoBitmap();
        if (bmp != null) {
            h.imagePrinter.setImageBitmap(bmp);
        } else {
            h.imagePrinter.setImageResource(R.drawable.ic_home);
        }
        h.btnOdaberi.setOnClickListener(v -> listener.onSelect(p));
    }

    @Override public int getItemCount() {
        return list.size();
    }

    // —— NOVO: za Spinner filtriranje ——
    public void filterList(List<Printer> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }
    // ——————————————————————————

    static class VH extends RecyclerView.ViewHolder {
        ImageView imagePrinter;
        TextView textNaziv;
        AppCompatButton btnOdaberi;

        VH(@NonNull View itemView) {
            super(itemView);
            imagePrinter = itemView.findViewById(R.id.imagePrinter);
            textNaziv    = itemView.findViewById(R.id.textPrinter);
            btnOdaberi   = itemView.findViewById(R.id.buttonOdaberiPrinter);
        }
    }
}
