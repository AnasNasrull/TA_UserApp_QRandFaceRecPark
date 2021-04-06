package com.example.park_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DaftarKendaraan extends AppCompatActivity {
    EditText Plat, Merk;
    Button Daftar;

    private RecyclerView rvList;
    private AdapterDaftarKendaraan adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<getKendaraan> dataKendaraan;

    String nim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerkndrn);

        nim = getIntent().getStringExtra("nim");

        Plat = findViewById(R.id.et_plat);
        Merk = findViewById(R.id.et_merk);
        Daftar = findViewById(R.id.bt_reg_kndrn);

        rvList = findViewById(R.id.rv_kndr);
        rvList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rvList.setLayoutManager(layoutManager);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Kendaraan");

        database.child("Data_Pemilik").child(nim).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataKendaraan = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    getKendaraan dtList = noteDataSnapshot.getValue(getKendaraan.class);
                    dtList.setKey(noteDataSnapshot.getKey());

                    dataKendaraan.add(dtList);
                }

                adapter = new AdapterDaftarKendaraan(dataKendaraan, DaftarKendaraan.this, nim);
                rvList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(), "Error"+databaseError, Toast.LENGTH_SHORT).show();
            }
        });

        //------------------------------------------------------------------------------------------
        Daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(Plat.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Masukkan Plat Kendaraan!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(Merk.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Masukkan Merk Kendaraan!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //============================================================
                Query query = FirebaseDatabase.getInstance().getReference("Kendaraan").child("Data_Plat").orderByChild("plat").equalTo(Plat.getText().toString().trim());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ////Log.e("app","found: "+snapshot.getValue());
                        if (snapshot.getValue() != null) {
                            Toast.makeText(DaftarKendaraan.this, "Kendaraan Sudah Terdaftar!", Toast.LENGTH_SHORT).show();
                        } else {
                            //Toast.makeText(DaftarKendaraan.this, "Kosong :"+snapshot.getValue(), Toast.LENGTH_SHORT).show();
                            AddKendaraan();
                        }

                    }
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    private void AddKendaraan() {
        final HashMap<String, String> pengguna = new HashMap();
        pengguna.put("plat",Plat.getText().toString().trim());
        pengguna.put("merk", Merk.getText().toString().trim());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Kendaraan");

        myRef.child("Data_Pemilik").child(nim).child(Plat.getText().toString().trim()).setValue(pengguna).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //
                Toast.makeText(DaftarKendaraan.this, "Proses Pendaftaran Kendaraan Berhasil",
                        Toast.LENGTH_SHORT).show();
            }
        });

        pengguna.clear();
        pengguna.put("plat",Plat.getText().toString().trim());

        myRef.child("Data_Plat").child(Plat.getText().toString().trim()).setValue(pengguna).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pengguna.clear();
                Plat.getText().clear();
                Merk.getText().clear();
            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(DaftarKendaraan.this, GenerateQrCode.class);
        startActivity(intent);
    }
}
