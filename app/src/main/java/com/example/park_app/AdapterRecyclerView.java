package com.example.park_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdapterRecyclerView extends RecyclerView.Adapter<AdapterRecyclerView.ViewHolder> {
    private ArrayList<getData> data;

    public AdapterRecyclerView(ArrayList<getData> data1){
        data = data1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView Plat, Tanggal, Masuk, Keluar;

        ViewHolder(View v) {
            super(v);
            Plat = v.findViewById(R.id.txt_plat_hst);
            Tanggal = v.findViewById(R.id.txt_tgl_hst);
            Masuk = v.findViewById(R.id.txt_masuk_hst);
            Keluar = v.findViewById(R.id.txt_keluar_hst);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String tanggal = data.get(position).getTanggal();
        final String plat = ": " + data.get(position).getPlat();
        final String masuk = ": " + data.get(position).getWaktu_masuk();
        final String keluar = ": " + data.get(position).getWaktu_keluar();

        String tgl = "";

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date d = sdf.parse(tanggal);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dt = new SimpleDateFormat("dd MMM yyyy");
            tgl = dt.format(d);
        } catch (ParseException ignored) {

        }

        holder.Tanggal.setText(tgl);
        holder.Plat.setText(plat);
        holder.Masuk.setText(masuk);
        holder.Keluar.setText(keluar);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
