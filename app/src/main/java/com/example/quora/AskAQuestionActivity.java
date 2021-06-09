package com.example.quora;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.StorageTask;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class AskAQuestionActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner spinner;
    private EditText questionEdt;
    private ImageView imageView;
    private Button cancelBtn, postBtn;

    private String askedByName = "";
    private DatabaseReference databaseReference;
    private DatabaseReference questionReference;
    private StorageReference storageReference;
    private StorageTask uploadTask;
    private ProgressDialog loader;
    private  String myUrl = "";
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String onlineUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ask_aquestion);
        initView();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                askedByName = snapshot.child("fullname").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.topics));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performValidation();
            }
        });
    }

    public void  initView() {
        toolbar = findViewById(R.id.question_toolbar);
        spinner = findViewById(R.id.spiner);
        questionEdt = findViewById(R.id.question_text);
        imageView = findViewById(R.id.questionImage);
        cancelBtn = findViewById(R.id.cancelBtn);
        postBtn = findViewById(R.id.postBtn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ask a question");
        loader = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        onlineUserId = user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(onlineUserId);
        questionReference = FirebaseDatabase.getInstance().getReference("questions_posts");
        storageReference = FirebaseStorage.getInstance().getReference("questions");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    String getQuestion() {
        return questionEdt.getText().toString().trim();
    }

    String getTopic() {
        return spinner.getSelectedItem().toString();
    }

    String getDate = DateFormat.getDateInstance().format(new Date());

    private void performValidation() {
        if (getQuestion().isEmpty()) {
            questionEdt.setError("Question is required");
            return;
        }
        if (imageUri == null) {
            uploadQuestionWithNoImage();
        } else {
            uploadQuestionWithImage();
        }
    }

    private void startLoader() {
        loader.setMessage("uploading");
        loader.setCanceledOnTouchOutside(false);
        loader.show();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadQuestionWithNoImage() {
        startLoader();
        String postid = questionReference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("postid", postid);
        hashMap.put("question", getQuestion());
        hashMap.put("publisher", onlineUserId);
        hashMap.put("topic", getTopic());
        hashMap.put("askedby", askedByName);
        hashMap.put("date", getDate);

        questionReference.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AskAQuestionActivity.this,
                            "Post Successfull",
                            Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                    Intent intent = new Intent(AskAQuestionActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AskAQuestionActivity.this,
                            "Post Failed",
                            Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                }
            }
        });
    }

    private void uploadQuestionWithImage() {
        startLoader();
        final StorageReference fileReference;
        fileReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
        uploadTask = fileReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull @NotNull Task task) throws Exception {
                if (!task.isComplete()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull @NotNull Task task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = (Uri) task.getResult();
                    myUrl = downloadUri.toString();
                    String postid = questionReference.push().getKey();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("postid", postid);
                    hashMap.put("question", getQuestion());
                    hashMap.put("publisher", onlineUserId);
                    hashMap.put("topic", getTopic());
                    hashMap.put("askedby", askedByName);
                    hashMap.put("questionImage", myUrl);
                    hashMap.put("date", getDate);
                    questionReference.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AskAQuestionActivity.this,
                                        "Post Successfull",
                                        Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                                Intent intent = new Intent(AskAQuestionActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(AskAQuestionActivity.this,
                                        "Post Failed",
                                        Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(AskAQuestionActivity.this,
                        "Failed to upload",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}