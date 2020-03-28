package com.adrian.sofergt.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adrian.sofergt.CounterClass;
import com.adrian.sofergt.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    Button show_textSUS, save_textSUS, show_textJOS, save_textJOS, save_image, metriSave, msSave;

    EditText edit_sus, edit_jos;

    ImageView imageView;

    Uri imguri;
    boolean imgUploaded = false;
    String myPhoneNumber;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference firebaseRef, reference;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        CounterClass.Add();
        setTitle("Optiuni");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        storageReference = firebaseStorage.getReference("Images");

        show_textSUS = findViewById(R.id.settings_getTextSus);
        save_textSUS = findViewById(R.id.settings_setTextSus);
        edit_sus = findViewById(R.id.edit_textSUS);

        show_textJOS = findViewById(R.id.settings_getTextJOS);
        save_textJOS = findViewById(R.id.settings_setTextJOS);
        edit_jos = findViewById(R.id.edit_textJOS);

        imageView = findViewById(R.id.edit_image);
        save_image = findViewById(R.id.save_image);


        Bundle extras = getIntent().getExtras();


        if (extras != null) {
            myPhoneNumber = extras.getString("myPhoneNumber");
            setTitle("Comanda Acceptata");

        }


        reference = db.getReference();


        show_textSUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseRef = db.getReference("info");
                firebaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        edit_sus.setText(snapshot.child("maintext").getValue(String.class));
                        firebaseRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
        save_textSUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myPhoneNumber.equals("0747089167"))
                    if (edit_sus.getText().toString().length() > 5) {
                        firebaseRef = db.getReference("info");
                        firebaseRef.child("maintext").setValue(edit_sus.getText().toString());
                        edit_sus.setText("");
                        Toast.makeText(SettingsActivity.this, "Text salvat!", Toast.LENGTH_SHORT).show();
                    } else {

                        Toast.makeText(SettingsActivity.this, "Textul trebuie sa contina ceva!", Toast.LENGTH_SHORT).show();
                    }
            }
        });

        show_textJOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseRef = db.getReference("info");
                firebaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        edit_jos.setText(snapshot.child("secondtext").getValue(String.class));
                        firebaseRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
        save_textJOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myPhoneNumber.equals("0747089167"))
                    if (edit_jos.getText().toString().length() > 5) {
                        firebaseRef = db.getReference("info");
                        firebaseRef.child("secondtext").setValue(edit_jos.getText().toString());
                        edit_jos.setText("");
                        Toast.makeText(SettingsActivity.this, "Text salvat!", Toast.LENGTH_SHORT).show();
                    } else {

                        Toast.makeText(SettingsActivity.this, "Textul trebuie sa contina ceva!", Toast.LENGTH_SHORT).show();
                    }

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileChooser();
            }
        });

        save_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imgUploaded) {
                    FileUploader();

                } else {
                    Toast.makeText(SettingsActivity.this, "Te rog incarca o imagine.", Toast.LENGTH_SHORT).show();
                }


            }
        });


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

    public void FileUploader() {
        try {
            final StorageReference seReference = storageReference.child(myPhoneNumber + "." + getExtension());


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
                        FirebaseDatabase.getInstance().getReference("soferi").child(myPhoneNumber).child("url").setValue(downloadURL);
                        Toast.makeText(SettingsActivity.this, "Imagine salvata cu succes", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        } catch (Exception er) {
            Toast.makeText(this, er.toString(), Toast.LENGTH_LONG).show();
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CounterClass.Remove();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}