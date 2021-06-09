package com.example.quora;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private EditText usernameEdt, passwordEdt, fullnameEdt, emailEdt;
    private Button registerButton;
    private TextView loginTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private ProgressDialog loader;
    private String onlineUserID = "";
    private Uri resultUri;
    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initView();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(intent, 1);
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEdt.getText().toString().trim();
                String fullname = fullnameEdt.getText().toString().trim();
                String password = passwordEdt.getText().toString().trim();
                String email = emailEdt.getText().toString().trim();

                if(username.isEmpty()) {
                    usernameEdt.setError("Username is required");
                }
                if(fullname.isEmpty()) {
                    fullnameEdt.setError("Fullname is required");
                }
                if(password.isEmpty()) {
                    passwordEdt.setError("Password is required");
                }
                if (password.length() < 6) {
                    passwordEdt.setError("Password must have at least 6 character");
                }
                if(email.isEmpty()) {
                    emailEdt.setError("Email is required");
                }
                if (!validate(email)) {
                    emailEdt.setError("Your email is not correct");
                }
                if(resultUri == null) {
                    Toast.makeText(RegistrationActivity.this, "Profile image is required", Toast.LENGTH_SHORT).show();
                }
                else {
                    loader.setMessage("Registration in progress");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(RegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            } else {
                                onlineUserID = mAuth.getCurrentUser().getUid();
                                reference = FirebaseDatabase.getInstance().getReference().child("users").child(onlineUserID);
                                Map hashMap = new HashMap();
                                hashMap.put("username", username);
                                hashMap.put("fullname", fullname);
                                hashMap.put("id", onlineUserID);
                                hashMap.put("email", email);
                                reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(RegistrationActivity.this, "Details set Successfully", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(RegistrationActivity.this, "Failed to upload data " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }

                                        finish();
                                        loader.dismiss();
                                    }
                                });
                                StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile image").child(onlineUserID);
                                UploadTask uploadTask = filePath.putFile(resultUri);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        if (taskSnapshot.getMetadata().getReference() != null) {
                                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String imageUrl = uri.toString();
                                                    Map hashMap = new HashMap();
                                                    hashMap.put("profileImageUrl", imageUrl);
                                                    reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                                        @Override
                                                        public void onSuccess(Object o) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(RegistrationActivity.this, "Registration success", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(RegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                    finish();
                                                }
                                            });
                                        }
                                    }
                                });
                                Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                                loader.dismiss();
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null) {
            resultUri = data.getData();
            profileImage.setImageURI(resultUri);
        } else {
            Toast.makeText(this, "Some thing wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public void initView() {
        profileImage = findViewById(R.id.profileImage);
        usernameEdt = findViewById(R.id.username);
        passwordEdt = findViewById(R.id.password);
        emailEdt = findViewById(R.id.loginEmail);
        fullnameEdt = findViewById(R.id.fullname);
        registerButton = findViewById(R.id.registerBtn);
        loginTextView = findViewById(R.id.regPageQuestion);
        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);
    }

    private boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
}