package com.example.park_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdapterDaftarKendaraan extends RecyclerView.Adapter<AdapterDaftarKendaraan.ViewHolder> {
    private ArrayList<getKendaraan> data;
    Context context;
    String nim;

    public AdapterDaftarKendaraan(ArrayList<getKendaraan> data1, Context context1, String nim1){
        data = data1;
        context = context1;
        nim = nim1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView Plat, Merek, Hapus;

        ViewHolder(View v) {
            super(v);
            Plat = v.findViewById(R.id.list_plat);
            Merek = v.findViewById(R.id.list_merek);
            Hapus = v.findViewById(R.id.list_hapus);
        }
    }

    @Override
    public AdapterDaftarKendaraan.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_daftar_kendaraan, parent, false);

        AdapterDaftarKendaraan.ViewHolder vh = new AdapterDaftarKendaraan.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(AdapterDaftarKendaraan.ViewHolder holder, final int position) {
        final String plat = ": " + data.get(position).getPlat();
        final String merek = ": " + data.get(position).getMerk();
        final String key = data.get(position).getKey();

        holder.Hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Konfirmasi");
                dialog.setMessage("Anda Yakin Ingin Menghapus Kendaraan Ini?");

                dialog.setPositiveButton("Yakin", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Kendaraan");
                        Query query = ref.child("Data_Pemilik").child(nim).orderByKey().equalTo(key);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });

                        Query query22 = ref.child("Data_Plat").orderByKey().equalTo(key);
                        query22.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                }

                                Toast.makeText(context, "Data Berhasil Dihapus", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });
                    }
                });

                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                dialog.setCancelable(false);

                AlertDialog dlg = dialog.create();
                dlg.show();
            }
        });

        holder.Plat.setText(plat);
        holder.Merek.setText(merek);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
