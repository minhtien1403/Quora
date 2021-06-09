package com.example.quora.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quora.R;
import com.example.quora.models.Comment;
import com.example.quora.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private List<Comment> commentList;
    private String postid;
    private FirebaseUser user;

    public CommentAdapter(Context context, List<Comment> commentList, String postid) {
        this.context = context;
        this.commentList = commentList;
        this.postid = postid;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comments_layout, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommentAdapter.ViewHolder holder, int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        Comment comment = commentList.get(position);

        holder.commentorComment.setText(comment.getComment());
        holder.commentDate.setText(comment.getDate());
        setUserInformation(holder.commentorProfileImage, holder.commentorUsername, comment.getPublisher());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView commentorProfileImage;
        public TextView commentorUsername, commentorComment, commentDate;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            commentorProfileImage = itemView.findViewById(R.id.commentor_profile_image);
            commentorUsername = itemView.findViewById(R.id.commentor_username);
            commentorComment = itemView.findViewById(R.id.commentor_comment);
            commentDate = itemView.findViewById(R.id.commentDate);
        }
    }

    private void setUserInformation(CircleImageView profileImageView, TextView usernameTextView, String publisherID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(publisherID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(context).load(user.getProfileimageurl()).into(profileImageView);
                usernameTextView.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
