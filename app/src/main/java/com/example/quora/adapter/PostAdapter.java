package com.example.quora.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.quora.CommentActivity;
import com.example.quora.EditQuestionActivity;
import com.example.quora.R;
import com.example.quora.models.Post;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context context;
    public List<Post> postList;
    private FirebaseUser user;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.question_card_layout, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PostAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = postList.get(position);
        if (post.getQuestionImage() == null) {
            holder.questionImage.setVisibility(View.GONE);
        } else {
            holder.questionImage.setVisibility(View.VISIBLE);
        }
        Glide.with(context).load(post.getQuestionImage())
                .into(holder.questionImage);
        holder.questionTextView.setText(post.getQuestion());
        holder.topicTextView.setText(post.getTopic());
        holder.askedOnTextView.setText(post.getDate());
        holder.askedByTextView.setText(post.getAskedby());
        setPublisherInfo(holder.publisherProfileImage, post.getPublisher());
        isLiked(post.getPostid(),holder.likeImg);
        isDisLiked(post.getPostid(), holder.dislikeImg);
        getDislikesCount(holder.dislikeTextView, post.getPostid());
        getLikesCount(holder.likeTextView, post.getPostid());
        getCommentCount(holder.commentTextView, post.getPostid());

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.post_menu);
                if(!post.getPublisher().equals(user.getUid())) {
                    popupMenu.getMenu().findItem(R.id.edit_post).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete_post).setVisible(false);
                } else {
                    popupMenu.getMenu().findItem(R.id.edit_post).setVisible(true);
                    popupMenu.getMenu().findItem(R.id.delete_post).setVisible(true);
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.report_post:
                                reportPost(post.getPostid(), user.getUid());
                                break;
                            case R.id.edit_post:
                                editPost(post.getPostid());
                                break;
                            case R.id.delete_post:
                                deletePost(post.getPostid());
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        holder.likeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.likeImg.getTag().equals("like") && holder.dislikeImg.getTag().equals("dislike")) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("likes")
                            .child(post.getPostid())
                            .child(user.getUid())
                            .setValue(true);
                } else if (holder.likeImg.getTag().equals("like") && holder.dislikeImg.getTag().equals("disliked") ) {
                    //un dislike
                    FirebaseDatabase.getInstance().getReference()
                            .child("dislikes")
                            .child(post.getPostid())
                            .child(user.getUid())
                            .removeValue();
                    //set like to true
                    FirebaseDatabase.getInstance().getReference()
                            .child("likes")
                            .child(post.getPostid())
                            .child(user.getUid())
                            .setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference()
                            .child("likes")
                            .child(post.getPostid())
                            .child(user.getUid())
                            .removeValue();
                }
            }
        });

        holder.dislikeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.dislikeImg.getTag().equals("dislike") && holder.likeImg.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference()
                            .child("dislikes")
                            .child(post.getPostid())
                            .child(user.getUid())
                            .setValue(true);
                }else if (holder.dislikeImg.getTag().equals("dislike") && holder.likeImg.getTag().equals("liked")){
                    FirebaseDatabase.getInstance().getReference()
                            .child("likes")
                            .child(post.getPostid())
                            .child(user.getUid())
                            .removeValue();
                    FirebaseDatabase.getInstance().getReference()
                            .child("dislikes")
                            .child(post.getPostid())
                            .child(user.getUid())
                            .setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference()
                            .child("dislikes")
                            .child(post.getPostid())
                            .child(user.getUid())
                            .removeValue();
                }
            }
        });

        holder.commentsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisher", post.getPublisher());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView publisherProfileImage;
        public TextView askedByTextView, topicTextView, askedOnTextView,
                likeTextView, dislikeTextView, commentTextView;
        public ImageView more, questionImage, likeImg, dislikeImg, commentsImg, saveImg;
        public TextView questionTextView;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            publisherProfileImage = itemView.findViewById(R.id.publisher_profile_image);
            askedByTextView = itemView.findViewById(R.id.asked_by_txt);
            askedOnTextView = itemView.findViewById(R.id.askedOnTxt);
            topicTextView = itemView.findViewById(R.id.topicnameTxt);
            likeTextView = itemView.findViewById(R.id.likeCount);
            dislikeTextView = itemView.findViewById(R.id.dislikeCount);
            commentTextView = itemView.findViewById(R.id.commentCount);
            more = itemView.findViewById(R.id.more);
            questionImage = itemView.findViewById(R.id.question_image);
            likeImg = itemView.findViewById(R.id.like);
            dislikeImg = itemView.findViewById(R.id.dislike);
            commentsImg = itemView.findViewById(R.id.comment);
            saveImg = itemView.findViewById(R.id.save);
            questionTextView = itemView.findViewById(R.id.expandable_text);
        }

    }

    private void setPublisherInfo(ImageView publisherImage, String userID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(context).load(user.getProfileimageurl()).into(publisherImage);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void isLiked(String postID, ImageView likeImageView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("likes").child(postID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child(user.getUid()).exists()) {
                    likeImageView.setImageResource(R.drawable.ic_liked);
                    likeImageView.setTag("liked");
                } else {
                    likeImageView.setImageResource(R.drawable.ic_like);
                    likeImageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void isDisLiked(String postID, ImageView dislikeImageView) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("dislikes").child(postID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child(userID).exists()) {
                    dislikeImageView.setImageResource(R.drawable.ic_disliked);
                    dislikeImageView.setTag("disliked");
                } else {
                    dislikeImageView.setImageResource(R.drawable.ic_dislike);
                    dislikeImageView.setTag("dislike");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLikesCount(TextView lileTxt, String postID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("likes").child(postID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                lileTxt.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDislikesCount(TextView dislikeTxt, String postID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("dislikes").child(postID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                dislikeTxt.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCommentCount(TextView commentTxt, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                commentTxt.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reportPost(String postID, String reporterID) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Warning");
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage("Are you really want to report this post ?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseDatabase.getInstance().getReference("reportList")
                        .child(postID)
                        .child(reporterID)
                        .setValue(true);
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.show();
    }

    private void deletePost(String postID) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Warning");
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage("Are you really want to delete this post ?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseDatabase.getInstance().getReference("questions_posts")
                        .child(postID)
                        .removeValue();
                FirebaseDatabase.getInstance().getReference("likes")
                        .child(postID)
                        .removeValue();
                FirebaseDatabase.getInstance().getReference("dislikes")
                        .child(postID)
                        .removeValue();
                FirebaseDatabase.getInstance().getReference("comments")
                        .child(postID)
                        .removeValue();
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.show();
    }

    private void editPost(String postID) {
        Intent intent = new Intent(context, EditQuestionActivity.class);
        intent.putExtra("postid", postID);
        context.startActivity(intent);
    }

}
