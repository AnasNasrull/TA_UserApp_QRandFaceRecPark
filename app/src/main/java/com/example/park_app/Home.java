package com.example.park_app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Home extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Button Scan, QrKendaraan, History, Profil;
    EditText InputPlat;

    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fStateListener;

    private static final String TAG = Home.class.getSimpleName();

    String nim=null, id, plat, status=null;

    ArrayList<String> item_plat;
    ArrayAdapter<String> arrayAdpter;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        new LoadData().execute();

        fAuth = FirebaseAuth.getInstance();

        fStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    id = user.getUid();
                } else {
                    Toast.makeText(Home.this, "User Logout\n", Toast.LENGTH_SHORT).show();
                    Log.v(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(Home.this, Login.class);
                    startActivity(intent);
                }
            }
        };

        /*FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Data_UID");
        FirebaseUser user = fAuth.getCurrentUser();
        myRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nim = dataSnapshot.child("nim").getValue().toString();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/

        Scan = findViewById(R.id.scanqr);
        QrKendaraan = findViewById(R.id.qrkendaraan);
        History = findViewById(R.id.hist);
        Profil = findViewById(R.id.profil);

        Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status.equals("-")) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
                    final View mView = getLayoutInflater().inflate(R.layout.popup_view_dialog, null);

                    //Mengeset judul dialog
                    dialog.setTitle("Pilih Plat Kendaraan");
                    item_plat = new ArrayList<>();
                    item_plat.add("Masukkan plat manual");

                    InputPlat = mView.findViewById(R.id.dlg_plat);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("Kendaraan");

                    final Spinner spinner = mView.findViewById(R.id.spin);
                    //spinner.setOnItemSelectedListener(Home.this);

                    arrayAdpter = new ArrayAdapter<>(Home.this, android.R.layout.simple_spinner_item, item_plat);
                    arrayAdpter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(arrayAdpter);

                    myRef.child("Data_Pemilik").child(nim).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot chilSnapshot:dataSnapshot.getChildren()) {
                                String data = chilSnapshot.child("plat").getValue().toString();
                                item_plat.add(data);
                            }

                            arrayAdpter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            //Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });



                    dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, int i) {
                            //String kdrPribadi="";
                            if (spinner.getSelectedItem().equals("Masukkan plat manual")) {
                                plat = InputPlat.getText().toString();

                                if (TextUtils.isEmpty(InputPlat.getText().toString().trim())) {
                                    Toast.makeText(getApplicationContext(), "Masukkan Plat Kendaraan!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Query query = FirebaseDatabase.getInstance().getReference("Kendaraan").child("Data_Plat").orderByKey().equalTo(plat);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean find;
                                        find = snapshot.getValue() != null;
                                        if (!find) {
                                            Toast.makeText(getApplicationContext(), "Plat Kendaraan Tidak Terdaftar!", Toast.LENGTH_SHORT).show();
                                            dialogInterface.cancel();
                                        } else {
                                            //String kdrPribadi = "bukan";
                                            Intent intent = new Intent(Home.this, ScanQrCode.class);
                                            intent.putExtra("nim", nim);
                                            intent.putExtra("plat", plat);
                                            //intent.putExtra("kepemilikan", kdrPribadi);
                                            startActivity(intent);
                                        }
                                    }
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            } else {
                                plat = spinner.getSelectedItem().toString();
                                //kdrPribadi = "iya";

                                Intent intent = new Intent(Home.this, ScanQrCode.class);
                                intent.putExtra("nim", nim);
                                intent.putExtra("plat", plat);
                                //intent.putExtra("kepemilikan", kdrPribadi);
                                startActivity(intent);
                            }
                        }
                    });

                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    dialog.setCancelable(false);

                    //Menampilkan custom dialog
                    dialog.setView(mView);
                    AlertDialog dlg = dialog.create();
                    dlg.show();
                } else {
                    Toast.makeText(Home.this, "Anda Belum Keluar Dari Tempat Parkir!\n", Toast.LENGTH_SHORT).show();
                }
            }
        });

        QrKendaraan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, GenerateQrCode.class);
                intent.putExtra("nim", nim);
                startActivity(intent);
            }
        });

        History.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, History.class);
                intent.putExtra("nim", nim);
                startActivity(intent);
                //Toast.makeText(Home.this, "Data ditemukan "+ nim, Toast.LENGTH_SHORT).show();
            }
        });

        Profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(Home.this, "Data ditemukan "+ nim, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Home.this, Profil.class);
                intent.putExtra("nim", nim);
                startActivity(intent);
            }
        });
    }

/*    private void signOut(){
        fAuth.signOut();
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(fStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fStateListener != null) {
            fAuth.removeAuthStateListener(fStateListener);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (item_plat.get(i).equals("Masukkan plat manual")) {
            InputPlat.setVisibility(View.VISIBLE);

            plat = InputPlat.getText().toString();
            if (TextUtils.isEmpty(InputPlat.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "Masukkan Plat Kendaraan!", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            InputPlat.setVisibility(View.GONE);
            plat = item_plat.get(i);
        }
        Intent intent = new Intent(Home.this, ScanQrCode.class);
        intent.putExtra("nim", nim);
        intent.putExtra("plat", plat);
        startActivity(intent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Keluar Aplikasi");
        builder.setMessage("Anda yakin ingin keluar aplikasi? ");
        builder.setCancelable(false);

        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAndRemoveTask();
                finish();
                moveTaskToBack(true);
            }
        });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public class LoadData extends AsyncTask<String, String, String> {
        ProgressDialog dialog = new ProgressDialog(Home.this);

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            do {
                DatabaseReference myRef = database.getReference("Data_UID");
                FirebaseUser user = fAuth.getCurrentUser();
                myRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        nim = dataSnapshot.child("nim").getValue().toString();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            } while (nim==null);

            DatabaseReference Ref = database.getReference("Data_Pengguna");
            Ref.child(nim).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    status = dataSnapshot.child("plat").getValue().toString();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            return nim;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(dialog.isShowing())
                dialog.dismiss();
        }
    }
}
