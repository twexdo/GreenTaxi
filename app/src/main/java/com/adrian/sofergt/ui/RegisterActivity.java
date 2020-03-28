package com.adrian.sofergt.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.adrian.sofergt.CounterClass;
import com.adrian.sofergt.MainActivity;
import com.adrian.sofergt.R;
import com.adrian.sofergt.objects.Sofer;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;



public class RegisterActivity extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    StorageReference storageReference;
    private static final String TAG = "RegisterActivity:debug";
    Button button;
    Context c;
    EditText nume, nr_tel, auth_cod;
    TextView auth_text;
    String nr_tel_string;
    ImageView imageView;
    Uri imguri;
    boolean imgUploaded = false;
    DatabaseReference usersRef;
    ProgressBar authPB;
    private Context context;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        CounterClass.Add();

        setTitle("Register");
        nume = findViewById(R.id.t_nume);
        nr_tel = findViewById(R.id.t_nr_tel);
        storageReference = FirebaseStorage.getInstance().getReference("Images");
        button = findViewById(R.id.b_register);
        imageView = findViewById(R.id.imageView);
        auth_cod = findViewById(R.id.auth_edittext);
        auth_text = findViewById(R.id.auth_text);
        authPB = findViewById(R.id.auth_progressbar);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileChooser();
            }
        });


        c = this.getApplicationContext();

        auth_cod.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (auth_cod.getText().toString().length() == 6) {
                    auth_cod.setTextColor(Color.GREEN);

                    String txt_code = auth_cod.getText().toString();
                    if (txt_code.length() != 6) {

                        auth_cod.setError("cod gresit...");
                        auth_cod.requestFocus();
                        return;
                    }
                    verifyCode(txt_code);


                } else {
                    auth_cod.setTextColor(Color.RED);
                }

            }
        });



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nume.length() > 1 && nr_tel.length() >= 10) {
                    if (imgUploaded) {
                        FileUploader();

                        String nume_string = nume.getText().toString();
                        nr_tel_string = nr_tel.getText().toString();

                        nume_string = nume_string.replaceAll("\\s+", "");
                        nr_tel_string = nr_tel_string.replaceAll("\\s+", "");

                        usersRef = db.getReference("soferi");

                        usersRef.child("+4" + nr_tel_string).setValue(new Sofer(nume_string, "+4" + nr_tel_string, 0, 46.318675, 24.294813));
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                "+4" + nr_tel_string,        // Phone number to verify
                                60,                 // Timeout duration 2 seconds
                                TimeUnit.SECONDS,   // Unit of timeout
                                RegisterActivity.this,              // Activity (for callback binding)
                                mCallbacks);        // OnVerificationStateChangedCallbacks

                        auth_cod.setVisibility(View.VISIBLE);
                        auth_text.setVisibility(View.VISIBLE);
                        authPB.setVisibility(View.VISIBLE);
                        button.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Se trimite codul...", Toast.LENGTH_SHORT).show();

                        Intent fin = new Intent();
                        setResult(42, fin);


                    } else {
                        Toast.makeText(c, "Te rog adauga o imagine.", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(c, "Date insuficiente!", Toast.LENGTH_SHORT).show();
                }
            }

        });


        context = this;
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Toast.makeText(context, "Incearca din nou ...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted");
                String codee = credential.getSmsCode();
                if (codee != null) {
                    verifyCode(codee);
                }

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(RegisterActivity.this).create();
                alertDialog.setTitle("Eroare");
                alertDialog.setMessage(e.getMessage());

                alertDialog.show();
            }

            @Override
            public void onCodeSent(@NonNull String s,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent");
                verificationId = s;
            }
        };


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imguri = data.getData();
            Bitmap a1 = null;
            try {
                a1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imguri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap resized = Bitmap.createScaledBitmap(Objects.requireNonNull(a1), 300, 300, true);

            imageView.setImageBitmap(resized);
            imgUploaded = true;
        }
    }

    public void FileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    private String getExtension() {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(imguri));

    }

    public void FileUploader() {
        final StorageReference seReference = storageReference.child(nr_tel_string + "." + getExtension());


        UploadTask uploadTask = seReference.putFile(imguri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                // Continue with the task to get the download URL
                return seReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String downloadURL = Objects.requireNonNull(downloadUri).toString();
                    usersRef.child("+4" + nr_tel_string).child("url").setValue(downloadURL);

                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CounterClass.Remove();
    }

    private void sighInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Felicitari !", Toast.LENGTH_LONG).show();
                            Intent start_mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(start_mainActivity);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Am intampinat o problema, te rog incearca din nou...", Toast.LENGTH_SHORT).show();
                            usersRef.child(nr_tel_string).setValue(null);
                        }
                    }
                });
    }

    public void verifyCode(String codee) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codee);
        sighInWithCredential(credential);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }
}
