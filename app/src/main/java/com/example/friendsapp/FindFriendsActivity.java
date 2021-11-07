package com.example.friendsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private RecyclerView findfriendsRecylerList;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        findfriendsRecylerList = findViewById(R.id.find_friends_recycler_list);
        findfriendsRecylerList.setHasFixedSize(true);
        findfriendsRecylerList.setLayoutManager(new LinearLayoutManager(this));





        mtoolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


    }

    @Override
    protected void onStart() {
        super.onStart();




        FirebaseRecyclerOptions<Contacts>options
                = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UsersRef,Contacts.class).build();

       FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder>adapter
               = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
           @Override
           protected void onBindViewHolder(@NonNull @NotNull FindFriendsViewHolder holder, int position, @NonNull @NotNull Contacts model) {


               holder.userName.setText(model.getName());
               holder.userStatus.setText(model.getStatus());
               Picasso.get().load(model.getImage()).into(holder.profileImage);


               holder.itemView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       String visit_user_id = getRef(position).getKey();

                       Intent intent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                       intent.putExtra("visit_user_id",visit_user_id);
                       startActivity(intent);

                   }
               });





           }

           @NonNull
           @NotNull
           @Override
           public FindFriendsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
               View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
               FindFriendsViewHolder findFriendsViewHolder = new FindFriendsViewHolder(view);

               return findFriendsViewHolder;
           }
       };

       findfriendsRecylerList.setAdapter(adapter);
       adapter.startListening();







    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;


        public FindFriendsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);

        }
    }

    }


