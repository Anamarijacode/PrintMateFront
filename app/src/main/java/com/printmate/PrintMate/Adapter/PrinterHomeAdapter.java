package com.printmate.PrintMate.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
            @NonNull ViewGroup parent,int viewType) {
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_printer_home,parent,false);
        return new PrinterViewHolder(v);
    }

    @Override public void onBindViewHolder(
            @NonNull PrinterViewHolder h,int pos) {
        PrinterHome p=printerList.get(pos);
        h.naziv .setText(p.getNaziv());
        h.status.setText(p.getStatus());
        if (p.getLogoBitmap()!=null) h.slika.setImageBitmap(p.getLogoBitmap());
        else                     h.slika.setImageResource(R.drawable.ic_printer);
    }

    @Override public int getItemCount(){ return printerList.size(); }

    static class PrinterViewHolder extends RecyclerView.ViewHolder {
        TextView  naziv,status;
        ImageView slika;
        PrinterViewHolder(@NonNull View v) {
            super(v);
            naziv  = v.findViewById(R.id.printerNaziv);
            status = v.findViewById(R.id.printerStatus);
            slika  = v.findViewById(R.id.printerSlika);
        }
    }
}
