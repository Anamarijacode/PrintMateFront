package com.printmate.PrintMate.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.printmate.PrintMate.Activity.PostavljanjeActivity;
import com.printmate.PrintMate.Model.Printer;
import com.printmate.PrintMate.R;

import java.util.List;

public class PrinterAdapter extends RecyclerView.Adapter<PrinterAdapter.PrinterViewHolder> {

    private Context context;
    private List<Printer> printerList;

    public PrinterAdapter(Context context, List<Printer> printerList) {
        this.context = context;
        this.printerList = printerList;
    }

    public void updateList(List<Printer> novaLista) {
        this.printerList = novaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrinterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_printer, parent, false);
        return new PrinterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrinterViewHolder holder, int position) {
        Printer printer = printerList.get(position);
        holder.textPrinter.setText(printer.getNaziv());
        holder.imagePrinter.setImageResource(printer.getSlikaResId());

        holder.btnPrinter.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostavljanjeActivity.class);
            intent.putExtra("naziv", printer.getNaziv());
            intent.putExtra("slika", printer.getSlikaResId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return printerList.size();
    }

    public static class PrinterViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePrinter;
        TextView textPrinter;
        AppCompatButton btnPrinter;

        public PrinterViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePrinter = itemView.findViewById(R.id.imagePrinter);
            textPrinter = itemView.findViewById(R.id.textPrinter);
            btnPrinter = itemView.findViewById(R.id.buttonOdaberiPrinter);
        }
    }
}
