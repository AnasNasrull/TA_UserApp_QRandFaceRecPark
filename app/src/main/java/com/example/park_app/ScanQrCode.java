package com.example.park_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQrCode extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    String nim, plat, kepemilikan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);

        nim = getIntent().getStringExtra("nim");
        plat = getIntent().getStringExtra("plat");
        kepemilikan = getIntent().getStringExtra("kepemilikan");
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(final Result rawResult) {
        //Log.v("TAG", rawResult.getText()); // Prints scan results
        //Log.v("TAG", rawResult.getBarcodeFormat().toString());
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
  //      builder.setTitle("Scan Result");
    //    builder.setMessage(rawResult.getText());
      //  AlertDialog alert1 = builder.create();
        //alert1.show();

        if (rawResult.getText().equals("Parkir Gedung C & D")) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference("Data_Pengguna");

            Date c = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
            String tm = new SimpleDateFormat("HHmmss").format(cal.getTime());
            String tgl = dt.format(c);
            String chKey = tgl + "" + tm;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat ndt = new SimpleDateFormat("dd-MM-yyyy");
            String ntgl = ndt.format(c);
            String wkt_msk = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());

            HashMap<String, String> history = new HashMap();
            history.put("tanggal", ntgl);
            history.put("waktu_masuk", wkt_msk);
            history.put("waktu_keluar", "-");
            history.put("plat", plat);
            history.put("NIM", nim);

            myRef.child(nim).child("Riwayat_Parkir").child(chKey).setValue(history).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //
                }
            });

            if (kepemilikan.equals("bukan")) {
                myRef.child(nim).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> postValues = new HashMap<String, Object>();
                        postValues.put("plat", plat);
                        myRef.child(nim).updateChildren(postValues);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            final DatabaseReference Ref = database.getReference("Parkir");
            Ref.child(tgl).child(tm).setValue(history).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(ScanQrCode.this, "Scan Berhasil!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ScanQrCode.this, Home.class);
                    startActivity(intent);
                }
            });
        } else {
            Toast.makeText(ScanQrCode.this, "QR Code Salah/Tidak terdaftar!", Toast.LENGTH_SHORT).show();
        }

        mScannerView.resumeCameraPreview(this);
    }

    public void onBackPressed() {
        Intent intent = new Intent(ScanQrCode.this, Home.class);
        startActivity(intent);
    }
}
