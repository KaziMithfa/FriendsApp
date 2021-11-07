package com.example.friendsapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {
    private static final String TAG = "RequestFragment";
    private View RequestFragmentView;
    private RecyclerView myRecylerList;
    private DatabaseReference ChatRequestRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;




    public RequestFragment() {
        // Required empty public constructor
    }


    public static RequestFragment newInstance(String param1, String param2) {
        RequestFragment fragment = new RequestFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        RequestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        myRecylerList = RequestFragmentView.findViewById(R.id.chat_request_list);
        myRecylerList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");






        return RequestFragmentView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts>options
                = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserId),Contacts.class).build();


        FirebaseRecyclerAdapter<Contacts,RequestViewHolder>adapter
                = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull RequestFragment.RequestViewHolder holder, int position, @NonNull @NotNull Contacts model) {
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_decline_btn).setVisibility(View.VISIBLE);

                final  String list_user_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type")
                        .getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            String type = snapshot.getValue().toString();

                            if(type.equals("received"))
                            {
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                        if(snapshot.hasChild("image"))
                                        {
                                            final String requestUserImage = snapshot.child("image").getValue().toString();


                                            Picasso.get().load(requestUserImage).into(holder.profileImage);


                                        }



                                            final String requestUserName = snapshot.child("name").getValue().toString();
                                            final String requestUserStatus = snapshot.child("status").getValue().toString();


                                            holder.UserName.setText(requestUserName);
                                            holder.UserStatus.setText(requestUserName+" wants to connect with you");


                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[]
                                                        = new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Cancel"

                                                        };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUserName+"Chat Request");

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        if(i==0)
                                                        {

                                                            ContactsRef.child(currentUserId).child(list_user_id)
                                                                    .child("Contacts").setValue("Saved")
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                                            if(task.isSuccessful()){

                                                                                ContactsRef.child(list_user_id).child(currentUserId).child("Contacts").setValue("Saved")
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){

                                                                                                    ChatRequestRef.child(currentUserId)
                                                                                                            .child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                ChatRequestRef.child(list_user_id).child(currentUserId)
                                                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                                                                        if(task.isSuccessful())
                                                                                                                        {
                                                                                                                            Toast.makeText(getContext(), "Contact has been saved", Toast.LENGTH_SHORT).show();
                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                            }

                                                                                                        }
                                                                                                    });

                                                                                                }
                                                                                            }
                                                                                        });

                                                                            }

                                                                        }
                                                                    });


                                                        }
                                                        else if(i==1)
                                                        {

                                                            ChatRequestRef.child(currentUserId).child(list_user_id)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        ChatRequestRef.child(list_user_id).child(currentUserId)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                                Toast.makeText(getContext(), "The request has been deleted", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }
                                                });

                                                builder.show();


                                            }
                                        });



                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }

                            else if(type.equals("sent"))
                            {
                                Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Req sent");
                                holder.itemView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);

                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                        if(snapshot.hasChild("image"))
                                        {




                                            final String requestUserImage = snapshot.child("image").getValue().toString();
                                            Picasso.get().load(requestUserImage).into(holder.profileImage);

                                        }


                                            final String requestUserName = snapshot.child("name").getValue().toString();

                                            holder.UserName.setText(requestUserName);
                                            holder.UserStatus.setText("You have sent a request to  "+requestUserName);

                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    CharSequence options[]
                                                            = new CharSequence[]{
                                                                    "Cancel Chat Request"
                                                    };

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle("Already Sent Request");

                                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                            if(i==0){
                                                                ChatRequestRef.child(currentUserId)
                                                                        .child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                                        if(task.isSuccessful())
                                                                        {
                                                                            ChatRequestRef.child(list_user_id).child(currentUserId)
                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        Toast.makeText(getContext(), "You have cancel the chat request", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }

                                                                    }
                                                                });
                                                            }

                                                        }
                                                    });

                                                    builder.show();
                                                }
                                            });








                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }
                        }




                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            @NotNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent
                ,false);
                RequestViewHolder requestViewHolder = new RequestViewHolder(view);
                return  requestViewHolder;



            }
        };

        myRecylerList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class  RequestViewHolder extends RecyclerView.ViewHolder{
        TextView UserName,UserStatus;
        CircleImageView profileImage;
        Button AcceptBtn,CancelBtn;


        public RequestViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            UserName = itemView.findViewById(R.id.user_profile_name);
            UserStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptBtn = itemView.findViewById(R.id.request_accept_btn);
            CancelBtn = itemView.findViewById(R.id.request_decline_btn);


        }
    }
}

