package com.example.quora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.quora.adapter.CommentAdapter;
import com.example.quora.models.Comment;
import com.example.quora.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView profileImageView;
    private RecyclerView recyclerView;
    private EditText commentEditText;
    private ImageView postComment;
    private ProgressDialog loader;
    private String postid;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        initView();
        getImage();
        loadAllComment();
        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = commentEditText.getText().toString();
                if (comment.isEmpty()) {
                    commentEditText.setError("Please say something");
                } else {
                    uploadComment(comment);
                }
            }
        });

    }

    private void loadAllComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(CommentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadComment(String comment) {
        loader.setMessage("Upload your comment");
        loader.setCanceledOnTouchOutside(false);
        loader.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postid);
        String commentID = reference.push().getKey();
        String date = DateFormat.getDateInstance().format(new Date());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("postid", postid);
        hashMap.put("comment",comment);
        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap.put("commentid", commentID);
        hashMap.put("date", date);

        reference.child(commentID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(CommentActivity.this, "Comment upload successfull", Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                } else {
                    Toast.makeText(CommentActivity.this, "Comment upload failed", Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                }
                commentEditText.setText("");
            }
        });
    }

    private void getImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(CommentActivity.this).load(user.getProfileimageurl()).into(profileImageView);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(CommentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {
        toolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        profileImageView = findViewById(R.id.cmt_profile_image);
        commentEditText = findViewById(R.id.commentTxt);
        postComment = findViewById(R.id.postComment);
        loader = new ProgressDialog(this);
        postid = getIntent().getStringExtra("postid");

        recyclerView = findViewById(R.id.cmt_recycleView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(CommentActivity.this, commentList, postid);
        recyclerView.setAdapter(commentAdapter);
    }
}