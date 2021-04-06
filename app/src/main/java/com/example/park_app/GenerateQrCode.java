package com.example.park_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class GenerateQrCode extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ImageView generateQrCode;
    Button Download;
    TextView DaftarKnd;

    QRGEncoder qrgEncoder;
    Bitmap bitmap;

    private FirebaseAuth fAuth;

    ArrayList<String> val;
    ArrayAdapter<String> arrayAdapter;

    String nim, plat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generateqr);

        nim = getIntent().getStringExtra("nim");

        fAuth = FirebaseAuth.getInstance();

        generateQrCode = findViewById(R.id.generate_qr_code);
        Download = findViewById(R.id.download);
        DaftarKnd = findViewById(R.id.txt_dft_kndr);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Data_Pengguna");
        myRef.child(nim).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                plat = dataSnapshot.child("plat").getValue().toString();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Query query = FirebaseDatabase.getInstance().getReference("Kendaraan").child("Data_Pemilik").orderByKey().equalTo(nim);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ////Log.e("app","found: "+snapshot.getValue());
                //String data = snapshot.child("nim").getValue().toString();
                if (snapshot.getValue() != null) {
                    //Toast.makeText(Home.this, "Ditemukan!"+snapshot.getValue(), Toast.LENGTH_SHORT).show();
                    Download.setEnabled(true);
                    AddQR();
                } else if (!plat.equals("-")) {
                    Download.setEnabled(true);
                    AddQRTemp();
                } else {
                    Download.setEnabled(false);
                    Toast.makeText(GenerateQrCode.this, "Belum Mendaftarkan Kendaraan!", Toast.LENGTH_SHORT).show();
                }

            }
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DaftarKnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GenerateQrCode.this, DaftarKendaraan.class);
                intent.putExtra("nim", nim);
                startActivity(intent);
            }
        });

    }

    private void AddQRTemp() {
        val = new ArrayList<>();

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(GenerateQrCode.this);

        arrayAdapter = new ArrayAdapter<>(GenerateQrCode.this, android.R.layout.simple_spinner_item, val);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        val.add(plat);
        arrayAdapter.notifyDataSetChanged();
    }

    private void AddQR() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Kendaraan");

        val = new ArrayList<>();

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(GenerateQrCode.this);

        arrayAdapter = new ArrayAdapter<>(GenerateQrCode.this, android.R.layout.simple_spinner_item, val);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        myRef.child("Data_Pemilik").child(nim).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                val.clear();
                if (!plat.equals("-")) {
                    val.add(plat);
                }
                for (DataSnapshot chilSnapshot:dataSnapshot.getChildren()) {
                    String data = chilSnapshot.child("plat").getValue().toString();
                    val.add(data);
                }

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
        //Toast.makeText(getApplicationContext(), val.get(i), Toast.LENGTH_LONG).show();

        qrgEncoder = new QRGEncoder(val.get(i), null, QRGContents.Type.TEXT, 600);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);

        bitmap = qrgEncoder.getBitmap();

        generateQrCode.setImageBitmap(bitmap);

        Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean save;
                String result;
                try {
                    String savePath = Environment.getExternalStorageDirectory() + "/QRCode/";
                    save = new QRGSaver().save(savePath, val.get(i), bitmap, QRGContents.ImageType.IMAGE_JPEG);
                    result = save ? "Image Saved" : "Image Not Saved";
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void onBackPressed() {
        Intent intent = new Intent(GenerateQrCode.this, Home.class);
        startActivity(intent);
    }
}
