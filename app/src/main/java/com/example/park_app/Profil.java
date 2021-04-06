package com.example.park_app;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceRec;
import com.tzutalin.dlib.VisionDetRet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Profil extends AppCompatActivity {
    ImageView foto;
    Button Logout;
    TextView Nama, Nim, Prodi, Email, Setting, ChPass, ChFoto;//, ChEmail;
    EditText EtChPassword;//EtChEmail

    int BITMAP_QUALITY = 100;
    int MAX_IMAGE_SIZE = 500;

    private Bitmap bitmap;
    private File destination = null;
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;

    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fStateListener;
    FirebaseUser user;

    String nim, nama, prodi, email;
    int jFt = 0, jPw = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        fAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        fStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(Profil.this, "User Logout\n", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Profil.this, Login.class);
                    startActivity(intent);
                }
            }
        };

        nim = getIntent().getStringExtra("nim");

        Logout = findViewById(R.id.bt_logout);
        foto = findViewById(R.id.img_profil);
        Nama = findViewById(R.id.txt_nama);
        Nim = findViewById(R.id.txt_nim);
        Prodi = findViewById(R.id.txt_prodi);
        Email = findViewById(R.id.txt_email);
        Setting = findViewById(R.id.txt_setting_profil);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        storageReference.child("images/"+nim+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Glide.with(Profil.this)
                        .load(uri)
                        .into(foto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(getApplicationContext(), "GAGAL", Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Data_Pengguna");

        myRef.child(nim).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nama = dataSnapshot.child("Nama").getValue(String.class);
                prodi = dataSnapshot.child("Prodi").getValue(String.class);
                email = dataSnapshot.child("Email").getValue(String.class);
                Nama.setText(": "+nama);
                Nim.setText(": "+nim);
                Prodi.setText(": "+prodi);
                Email.setText(": "+user.getEmail());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Button btChPw, btChImg;
                final LinearLayout LL_set;

                //final AlertDialog.Builder dialog = new AlertDialog.Builder(Profil.this);
                //final View view = getLayoutInflater().inflate(R.layout.setting_dialog, null);
                final Dialog dialog = new Dialog(Profil.this);
                dialog.setContentView(R.layout.setting_dialog);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                //ChEmail = dialog.findViewById(R.id.set_email);
                ChPass = dialog.findViewById(R.id.set_password);
                ChFoto = dialog.findViewById(R.id.set_foto);
                btChImg = dialog.findViewById(R.id.btn_img_setting);
                btChPw = dialog.findViewById(R.id.btn_pw_setting);
                LL_set = dialog.findViewById(R.id.LL_setting);
                EtChPassword = dialog.findViewById(R.id.et_chpw);

                /*ChEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        final AlertDialog.Builder dgChEmail = new AlertDialog.Builder(Profil.this);
                        final View vw = getLayoutInflater().inflate(R.layout.popup_ch_email, null);
                        dgChEmail.setCancelable(false);

                        EtChEmail = vw.findViewById(R.id.et_chemail);
                        final String mail = EtChEmail.getText().toString().trim();

                        dgChEmail.setPositiveButton("Ganti", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (TextUtils.isEmpty(EtChEmail.getText().toString().trim())) {
                                    Toast.makeText(getApplicationContext(), "Masukkan Email!", Toast.LENGTH_SHORT).show();
                                } else {
                                    fAuth.fetchSignInMethodsForEmail(EtChEmail.getText().toString().trim())
                                            .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), "123456");
                                                    user.reauthenticate(credential)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                                    user.updateEmail(mail)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(Profil.this, "Email Berhasil Diubah", Toast.LENGTH_SHORT).show();
                                                                                        FirebaseAuth.getInstance().signOut();
                                                                                        Intent intent = new Intent(Profil.this, Login.class);
                                                                                        startActivity(intent);
                                                                                        finish();
                                                                                    } else {
                                                                                        Toast.makeText(Profil.this, "Terjadi Kesalahan, Silakan Coba Lagi", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                }
                                            });
                                }
                            }
                        });

                        dgChEmail.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                        dgChEmail.setView(vw);
                        AlertDialog dlgChE = dgChEmail.create();
                        dlgChE.show();
                    }
                });*/

                ChPass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jFt=0;
                        btChImg.setVisibility(View.GONE);
                        if (jPw%2==0) {
                            LL_set.setVisibility(View.VISIBLE);

                            btChPw.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (TextUtils.isEmpty(EtChPassword.getText().toString().trim())) {
                                        Toast.makeText(getApplicationContext(), "Masukkan Email!", Toast.LENGTH_SHORT).show();
                                    } else if (EtChPassword.getText().toString().trim().equals(user.getEmail())){
                                        FirebaseAuth.getInstance().signOut();
                                        fAuth.sendPasswordResetEmail(EtChPassword.getText().toString().trim())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(Profil.this, "Silahkan Cek Email Anda Untuk Mengubah Password", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(Profil.this, Login.class);
                                                            startActivity(intent);
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(Profil.this, "Terjadi Kesalahan, Pastikan Email Benar\nSilakan Coba Lagi", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            LL_set.setVisibility(View.GONE);
                        }
                        jPw++;

                        /*dialog.dismiss();
                        final AlertDialog.Builder dgChPassword = new AlertDialog.Builder(Profil.this);
                        final View vv = getLayoutInflater().inflate(R.layout.popup_ch_password, null);
                        dgChPassword.setCancelable(false);

                        EtChPassword = vv.findViewById(R.id.et_chpw);

                        dgChPassword.setPositiveButton("Ganti", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (TextUtils.isEmpty(EtChPassword.getText().toString().trim())) {
                                    Toast.makeText(getApplicationContext(), "Masukkan Email!", Toast.LENGTH_SHORT).show();
                                } else if (EtChPassword.getText().toString().trim().equals(user.getEmail())){
                                    FirebaseAuth.getInstance().signOut();
                                    fAuth.sendPasswordResetEmail(EtChPassword.getText().toString().trim())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(Profil.this, "Silahkan Cek Email Anda Untuk Mengubah Password", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(Profil.this, Login.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(Profil.this, "Terjadi Kesalahan, Pastikan Email Benar\nSilakan Coba Lagi", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                        dgChPassword.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                        dgChPassword.setView(vv);
                        AlertDialog dlgChP = dgChPassword.create();
                        dlgChP.show();*/
                    }
                });

                ChFoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jPw=0;
                        LL_set.setVisibility(View.GONE);
                        if (jFt %2 == 0) {
                            btChImg.setVisibility(View.VISIBLE);

                            btChImg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        PackageManager pm = getPackageManager();
                                        int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getPackageName());
                                        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                                            final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
                                            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Profil.this);
                                            builder.setTitle("Select Option");
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int item) {
                                                    if (options[item].equals("Take Photo")) {
                                                        dialog.dismiss();
                                                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                        startActivityForResult(intent, PICK_IMAGE_CAMERA);
                                                    } else if (options[item].equals("Choose From Gallery")) {
                                                        dialog.dismiss();
                                                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                        startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                                                    } else if (options[item].equals("Cancel")) {
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                            builder.show();
                                        } else
                                            Toast.makeText(Profil.this, "Camera Permission error", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Toast.makeText(Profil.this, "Camera Permission error", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                            destination = new File(Constants.getDLibDirectoryPath() + "/temp.jpg");
                        } else {
                            btChImg.setVisibility(View.GONE);
                        }

                        jFt++;
                    }
                });

                dialog.show();
            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fAuth.signOut();
                Toast.makeText(Profil.this, "User Logout!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Profil.this, Login.class);
                startActivity(intent);
            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(Profil.this, Home.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CAMERA) {
            try {
                Uri selectedImage = data.getData();
                bitmap = (Bitmap) data.getExtras().get("data");
                Bitmap scaledBitmap = scaleDown(bitmap, MAX_IMAGE_SIZE, true);
                new Profil.detectAsync().execute(scaledBitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_GALLERY) {
            Uri selectedImage = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                Bitmap scaledBitmap = scaleDown(bitmap, MAX_IMAGE_SIZE, true);
                new Profil.detectAsync().execute(scaledBitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
        return newBitmap;
    }

    private class detectAsync extends AsyncTask<Bitmap, Void, String> {
        ProgressDialog dialog = new ProgressDialog(Profil.this);

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Detecting face...");
            dialog.setCancelable(false);
            dialog.show();
            super.onPreExecute();
        }

        protected String doInBackground(Bitmap... bp) {
            FaceRec mFaceRec = new FaceRec(Constants.getDLibDirectoryPath());
            List<VisionDetRet> results;
            results = mFaceRec.detect(bp[0]);
            String msg = null;
            if (results.size()==0) {
                msg = "Tidak Ada Wajah Yang Terdeteksi. Silahkan Pilih Foto Lain";
            } else if (results.size() > 1) {
                msg = "Terdeteksi Lebih Dari Satu Wajah. Silahkan Pilih Foto Lain";
            } else {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bp[0].compress(Bitmap.CompressFormat.JPEG, BITMAP_QUALITY, bytes);
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                msg = "Foto Profil Berhasil Diubah";

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference mountainImagesRef = storageRef.child("images/"+nim+".jpg");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = mountainImagesRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        //Toast.makeText(getApplicationContext(), "Data Gagal Disimpan !", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        //Toast.makeText(getApplicationContext(), "Data Berhasil Disimpan !", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return msg;
        }

        protected void onPostExecute(String result) {
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
                if (result!=null) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(Profil.this);
                    builder1.setMessage(result);
                    builder1.setCancelable(true);
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
                //enableSubmitIfReady();
            }

        }
    }
}
