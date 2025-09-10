// PrinterHomeAdapter.java
package com.printmate.PrintMate.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.printmate.PrintMate.Activity.MainActivity;
import com.printmate.PrintMate.Fragmenti.MainFragment;
import com.printmate.PrintMate.Modeli.PrinterHome;
import com.printmate.PrintMate.R;

import java.util.List;

public class PrinterHomeAdapter
        extends RecyclerView.Adapter<PrinterHomeAdapter.PrinterViewHolder> {

    private final List<PrinterHome> printerList;
    public PrinterHomeAdapter(List<PrinterHome> printerList) {
        this.printerList = printerList;
    }

    @NonNull @Override
    public PrinterViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_printer_home, parent, false);
        return new PrinterViewHolder(v);
    }

    @Override public void onBindViewHolder(
            @NonNull PrinterViewHolder h, int pos
    ) {
        PrinterHome p = printerList.get(pos);
        h.naziv.setText(p.getNaziv());
        h.status.setText(p.getStatus());
        if (p.getLogoBitmap() != null) {
            h.slika.setImageBitmap(p.getLogoBitmap());
        } else {
            h.slika.setImageResource(R.drawable.ic_printer);
        }

        h.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("printer_id", p.getIdPrintera());
            intent.putExtra("printer_name", p.getNaziv());
            intent.putExtra("base_url", p.getBaseUrl());
            intent.putExtra("api_key", p.getApiKey());

            // PROSLIJEDI SLIKU
            if (p.getLogoBitmap() != null) {
                String logoBase64 = bitmapToBase64(p.getLogoBitmap());
                intent.putExtra("printer_image_base64", logoBase64);
            } else {
                intent.putExtra("printer_image_base64", "");
            }

            context.startActivity(intent);
        });



    }

    @Override public int getItemCount() {
        return printerList.size();
    }

    static class PrinterViewHolder extends RecyclerView.ViewHolder {
        TextView naziv, status;
        ImageView slika;
        PrinterViewHolder(@NonNull View v) {
            super(v);
            naziv  = v.findViewById(R.id.printerNaziv);
            status = v.findViewById(R.id.printerStatus);
            slika  = v.findViewById(R.id.printerSlika);
        }
    }
    private String bitmapToBase64(Bitmap bitmap) {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    }

}
