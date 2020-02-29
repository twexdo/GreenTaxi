package com.adrian.sofergt;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    StorageReference storageReference;
    EditText nume, nr_tel;
    Button button;
    Context c;
    String nr_tel_string;
    ImageView imageView;
    Uri imguri;
    boolean imgUploaded = false;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register");
        nume = findViewById(R.id.t_nume);
        nr_tel = findViewById(R.id.t_nr_tel);
        storageReference = FirebaseStorage.getInstance().getReference("Images");
        button = findViewById(R.id.b_register);
        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileChooser();
            }
        });


        c = this.getApplicationContext();
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

                        usersRef.child(nr_tel_string).setValue(new Sofer(nume_string, nr_tel_string, 0, 46.318675, 24.294813));


                        FileHelper h = new FileHelper();
                        h.writeToFile(nume_string, c, true);
                        h.writeToFile("\n" + nr_tel_string, c, false);
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
                    usersRef.child(nr_tel_string).child("url").setValue(downloadURL);
                    finish();
                }

            }
        });

    }
}
