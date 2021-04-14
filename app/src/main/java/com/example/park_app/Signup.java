package com.example.park_app;

import android.Manifest;
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceRec;
import com.tzutalin.dlib.VisionDetRet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import cyd.awesome.material.AwesomeText;
import cyd.awesome.material.FontCharacterMaps;

public class Signup extends AppCompatActivity {
    private Button btSignUp2;
    private EditText etEmail2, etPassword2, Nama, NIM, Prodi;
    private String nama, nim, prodi;
    AwesomeText showPass;

    EditText et_image;
    Button btn_select_image;
    int BITMAP_QUALITY = 100;
    int MAX_IMAGE_SIZE = 500;

    private Bitmap bitmap;
    private File destination = null;
    private String imgPath = null;
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;

    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fStateListener;

    private static final String TAG = Signup.class.getSimpleName();

    boolean stat_pw = true;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fAuth = FirebaseAuth.getInstance();

        fStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.v(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.v(TAG, "onAuthStateChanged:signed_out");
                }
            }

        };

        btSignUp2 = findViewById(R.id.bt_signup2);
        etEmail2 = findViewById(R.id.et_email2);
        etPassword2 = findViewById(R.id.et_password2);
        Nama = findViewById(R.id.et_nama);
        NIM = findViewById(R.id.et_nim);
        Prodi = findViewById(R.id.et_prodi);
        TextView masuk = findViewById(R.id.tx_masuk);
        showPass = findViewById(R.id.show_pw2);

        btn_select_image = findViewById(R.id.btn_img);
        et_image = findViewById(R.id.et_img);

        showPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stat_pw) {
                    etPassword2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    stat_pw = false;
                    showPass.setMaterialDesignIcon(FontCharacterMaps.MaterialDesign.MD_VISIBILITY);
                    etPassword2.setSelection(etPassword2.length());
                } else {
                    etPassword2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    stat_pw = true;
                    showPass.setMaterialDesignIcon(FontCharacterMaps.MaterialDesign.MD_VISIBILITY_OFF);
                    etPassword2.setSelection(etPassword2.length());
                }
            }
        });

        btn_select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PackageManager pm = getPackageManager();
                    int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getPackageName());
                    if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                        final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Signup.this);
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
                        Toast.makeText(Signup.this, "Camera Permission error", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(Signup.this, "Camera Permission error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        //btSignUp2.setEnabled(false);

        destination = new File(Constants.getDLibDirectoryPath() + "/temp.jpg");

        btSignUp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(Nama.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Enter your name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(NIM.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Enter your NIM!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(Prodi.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Enter your Program Studi!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(etEmail2.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Enter your email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(etPassword2.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Enter your password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(et_image.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Enter your photo!", Toast.LENGTH_SHORT).show();
                    return;
                }

                nama = Nama.getText().toString().trim();
                nim = NIM.getText().toString().trim();
                prodi = Prodi.getText().toString().trim();

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

                signUp(etEmail2.getText().toString().trim(), etPassword2.getText().toString().trim(), nama, nim, prodi);
            }
        });

        masuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fAuth.signOut();

                Intent intent = new Intent(Signup.this, Login.class);
                startActivity(intent);
            }
        });
    }

    private void signUp(final String email, String password, final String nama, final String nim, final String prodi){

        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Toast.makeText(Signup.this, "Proses Pendaftaran Gagal",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseUser user = fAuth.getCurrentUser();

                            HashMap<String, String> pengguna = new HashMap();
                            pengguna.put("Nama", nama);
                            pengguna.put("NIM", nim);
                            pengguna.put("Prodi", prodi);
                            pengguna.put("plat", "-");

                            HashMap<String, String> id = new HashMap();
                            id.put("nim", nim);

                            FirebaseDatabase database = FirebaseDatabase.getInstance();

                            DatabaseReference Ref = database.getReference("Data_UID");
                            Ref.child(user.getUid()).setValue(id).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //
                                }
                            });

                            DatabaseReference myRef = database.getReference("Data_Pengguna");
                            myRef.child(nim).setValue(pengguna).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //
                                    Toast.makeText(Signup.this, "Proses Pendaftaran Berhasil",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                            fAuth.signOut();

                            Intent intent = new Intent(Signup.this, Login.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

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

    public void enableSubmitIfReady() {
        boolean isReady = Nama.getText().toString().length() > 0 && imgPath!=null;
        btSignUp2.setEnabled(isReady);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CAMERA) {
            try {
                Uri selectedImage = data.getData();
                bitmap = (Bitmap) data.getExtras().get("data");
                Bitmap scaledBitmap = scaleDown(bitmap, MAX_IMAGE_SIZE, true);
                et_image.setText(destination.getAbsolutePath());
                new detectAsync().execute(scaledBitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_GALLERY) {
            Uri selectedImage = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                Bitmap scaledBitmap = scaleDown(bitmap, MAX_IMAGE_SIZE, true);
                et_image.setText(getRealPathFromURI(selectedImage));
                new detectAsync().execute(scaledBitmap);

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
        ProgressDialog dialog = new ProgressDialog(Signup.this);

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
                imgPath = destination.getAbsolutePath();
            }
            return msg;
        }

        protected void onPostExecute(String result) {
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
                if (result!=null) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(Signup.this);
                    builder1.setMessage(result);
                    builder1.setCancelable(true);
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                    imgPath = null;
                    et_image.setText("");
                }
                //enableSubmitIfReady();
            }

        }
    }

    public void onBackPressed() {
        Intent intent = new Intent(Signup.this, Home.class);
        startActivity(intent);
    }
}
