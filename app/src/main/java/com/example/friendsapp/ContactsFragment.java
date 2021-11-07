package com.example.friendsapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView mycontactsList;
    private DatabaseReference ContactsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUser;
    FirebaseRecyclerAdapter<Contacts,ContactsViewHolder>adapter;


    public ContactsFragment() {
        // Required empty public constructor
    }


    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
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
        ContactsView =  inflater.inflate(R.layout.fragment_contacts, container, false);
        mycontactsList = ContactsView.findViewById(R.id.contacts_list);
        mycontactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();



        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUser);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts>options
                = new FirebaseRecyclerOptions.Builder<Contacts>().
                setQuery(ContactsRef,Contacts.class).build();

        adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ContactsViewHolder holder, int position, @NonNull @NotNull Contacts model) {

                String userId = getRef(position).getKey();

                UsersRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        if(snapshot.exists())
                        {


                            if(snapshot.child("userState").hasChild("state"))
                            {
                                String state = snapshot.child("userState").child("state").toString();
                                String date = snapshot.child("userState").child("date").toString();
                                String time = snapshot.child("userState").child("time").toString();

                                if(state.equals("online"))
                                {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }

                                else if(state.equals("offline"))
                                {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }

                            else{
                               holder.onlineIcon.setVisibility(View.INVISIBLE);

                            }


                            if(snapshot.hasChild("image"))
                            {
                                String name = snapshot.child("name").getValue().toString();
                                String status = snapshot.child("status").getValue().toString();
                                String image = snapshot.child("image").getValue().toString();


                                holder.userName.setText(name);
                                holder.userStatus.setText(status);
                                Picasso.get().load(image).into(holder.profileImage);


                            }

                            else{

                                String name = snapshot.child("name").getValue().toString();
                                String status = snapshot.child("status").getValue().toString();


                                holder.userName.setText(name);
                                holder.userStatus.setText(status);

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
            public ContactsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_display_layout,parent,false);
                ContactsViewHolder contactsViewHolder = new ContactsViewHolder(view);
                return contactsViewHolder;
            }
        };


        adapter.startListening();
        mycontactsList.setAdapter(adapter);




    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}