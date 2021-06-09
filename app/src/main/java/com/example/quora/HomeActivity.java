package com.example.quora;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quora.adapter.PostAdapter;
import com.example.quora.models.Post;
import com.example.quora.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.quora.R.id.logout;
import static com.example.quora.R.id.nav_cars;
import static com.example.quora.R.id.nav_finance;
import static com.example.quora.R.id.nav_foods;
import static com.example.quora.R.id.nav_sports;
import static com.example.quora.R.id.nav_tech;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private FloatingActionButton fab;

    private RecyclerView recyclerView;
    private ProgressBar progressCircular;
    private LinearLayoutManager linearLayoutManager;

    private PostAdapter postAdapter;
    private List<Post> postList;

    private CircleImageView navHeaderImage;
    private TextView navEmail, navUsername;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Quora App");

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AskAQuestionActivity.class);
                startActivity(intent);

            }
        });

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(HomeActivity.this).load(user.getProfileimageurl()).into(navHeaderImage);
                navUsername.setText(user.getUsername());
                navEmail.setText(user.getEmail());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        loadAllPost();

    }

    private void loadAllPost() {
        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference("questions_posts");
        postReference.addValueEventListener(new ValueEventListener() {
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
                progressCircular.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case nav_finance:
                Intent intent = new Intent(HomeActivity.this, CategorySelectedActivity.class);
                intent.putExtra("title", "Finance");
                startActivity(intent);
                break;
            case nav_sports:
                Intent intentS = new Intent(HomeActivity.this, CategorySelectedActivity.class);
                intentS.putExtra("title", "Sports");
                startActivity(intentS);
                break;
            case nav_foods:
                Intent intentF = new Intent(HomeActivity.this, CategorySelectedActivity.class);
                intentF.putExtra("title", "Food");
                startActivity(intentF);
                break;
            case nav_tech:
                Intent intentT = new Intent(HomeActivity.this, CategorySelectedActivity.class);
                intentT.putExtra("title", "Technology");
                startActivity(intentT);
                break;
            case nav_cars:
                Intent intentC = new Intent(HomeActivity.this, CategorySelectedActivity.class);
                intentC.putExtra("title", "Cars");
                startActivity(intentC);
                break;
            case logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void initView() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.home_toolbar);
        navigationView = findViewById(R.id.navigation_view);
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerView);
        progressCircular = findViewById(R.id.progress_circular);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(HomeActivity.this, postList);
        recyclerView.setAdapter(postAdapter);

        navHeaderImage = navigationView.getHeaderView(0).findViewById(R.id.nav_header_profile_image);
        navUsername = navigationView.getHeaderView(0).findViewById(R.id.nav_header_username);
        navEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_header_useremail);
        userReference = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

}