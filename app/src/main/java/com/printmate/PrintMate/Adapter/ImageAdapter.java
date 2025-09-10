package com.printmate.PrintMate.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Modeli.SlikaModelaDto;
import com.printmate.PrintMate.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final List<SlikaModelaDto> images = new ArrayList<>();

    public void setImages(List<SlikaModelaDto> newList) {
        images.clear();
        images.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(images.get(position));
    }

    @Override public int getItemCount() {
        return images.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
        void bind(SlikaModelaDto dto) {
            // prvo stavi neki placeholder ili clear
            ivImage.setImageDrawable(null);

            ApiClient.getAuthApi()
                    .getImage(dto.getIdSlike())
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                            if (res.isSuccessful() && res.body() != null) {
                                try {
                                    byte[] bytes = res.body().bytes();
                                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    ivImage.setImageBitmap(bmp);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // opcionalno: ivImage.setImageResource(R.drawable.ic_broken_image);
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            // opcionalno: ivImage.setImageResource(R.drawable.ic_broken_image);
                        }
                    });
        }

    }
}
