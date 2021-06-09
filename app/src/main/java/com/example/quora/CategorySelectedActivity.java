package com.example.quora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.quora.adapter.PostAdapter;
import com.example.quora.models.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CategorySelectedActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selected);
        initView();
    }

    public void initView() {
        toolbar = findViewById(R.id.category_toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.CategoryRecyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(CategorySelectedActivity.this, postList);
        recyclerView.setAdapter(postAdapter);

        if (getIntent().getExtras() != null) {
            title = getIntent().getStringExtra("title");
            getSupportActionBar().setTitle(title);
            loadPostByCategory();
        }
    }

    private void loadPostByCategory() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("questions_posts");
        Query query = reference.orderByChild("topic").equalTo(title.toLowerCase());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post == null) {
                        break;
                    }
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(CategorySelectedActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}