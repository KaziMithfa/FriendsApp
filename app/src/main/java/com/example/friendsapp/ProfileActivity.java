package com.example.friendsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {



    private String reciveUserID,senderUserID;
    private CircleImageView profileImage;
    private TextView UserProfileName,UserProfileStatus;
    private Button SendMessageRequestBtn,DeclineMessageRequestBtn;
    private FirebaseAuth mAuth;

    private DatabaseReference UsersRef,ChatRequestRef,ContactsRef;
    private String currentState = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        reciveUserID = getIntent().getStringExtra("visit_user_id");

        profileImage = findViewById(R.id.visit_profile_image);
      UserProfileName = findViewById(R.id.visit_profile_name);
      UserProfileStatus = findViewById(R.id.visit_profile_status);
      SendMessageRequestBtn = findViewById(R.id.send_message_request_btn);
      DeclineMessageRequestBtn = findViewById(R.id.decline_message_request_btn);

      UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
      ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
      ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
      mAuth = FirebaseAuth.getInstance();
      senderUserID = mAuth.getCurrentUser().getUid();

      currentState = "new";


      RetriveUserInfo();


    }

    private void RetriveUserInfo() {


        UsersRef.child(reciveUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if(snapshot.child("image").exists()){
                    String image = snapshot.child("image").getValue().toString();
                    String name = snapshot.child("name").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.profile_image).into(profileImage);
                    UserProfileName.setText(name);
                    UserProfileStatus.setText(status);

                    ChatRequests();
                }

                else {

                    String name = snapshot.child("name").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();



                    UserProfileName.setText(name);
                    UserProfileStatus.setText(status);

                    ChatRequests();



                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void ChatRequests() {


        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.hasChild(reciveUserID)){
                    String request_type = snapshot.child(reciveUserID).child("request_type")
                            .getValue().toString();

                    if(request_type.equals("sent"))
                    {
                        currentState = "request_sent";
                        SendMessageRequestBtn.setText("Cancel Chat Request");

                    }

                    if(request_type.equals("received"))
                    {
                        currentState = "request_received";
                        SendMessageRequestBtn.setText("Accept Chat Request");



                        DeclineMessageRequestBtn.setVisibility(View.VISIBLE);
                        DeclineMessageRequestBtn.setEnabled(true);
                        DeclineMessageRequestBtn.setText("Cancel Chat Request");

                        DeclineMessageRequestBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelChatRequest();
                            }
                        });
                    }

                }

                else{
                    ContactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(reciveUserID))
                            {
                                currentState = "friends";
                                SendMessageRequestBtn.setText("Remove this contact");
                                DeclineMessageRequestBtn.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestBtn.setEnabled(false);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        if(reciveUserID.equals(senderUserID))
        {
            SendMessageRequestBtn.setVisibility(View.INVISIBLE);
        }

        else{

            SendMessageRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendMessageRequestBtn.setEnabled(false);

                    if(currentState.equals("new"))
                    {
                        SendChatRequest();
                    }

                    if(currentState.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }

                    if(currentState.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }

                    if(currentState.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }


                }
            });

        }

    }

    private void RemoveSpecificContact() {

        ContactsRef.child(senderUserID).child(reciveUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ContactsRef.child(reciveUserID).child(senderUserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(ProfileActivity.this, "The contact has been removed", Toast.LENGTH_SHORT).show();
                                currentState = "new";
                                SendMessageRequestBtn.setEnabled(true);
                                SendMessageRequestBtn.setText("Send Message");


                            }
                        }
                    });
                }
            }
        });


    }

    private void AcceptChatRequest() {

        ContactsRef.child(senderUserID)
                .child(reciveUserID).child("Contacts")
                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ContactsRef.child(reciveUserID)
                            .child(senderUserID).child("Contacts")
                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                ChatRequestRef.child(senderUserID).child(reciveUserID)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            ChatRequestRef.child(reciveUserID).child(senderUserID)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                    SendMessageRequestBtn.setEnabled(true);
                                                    currentState = "friends";
                                                    SendMessageRequestBtn.setText("Remove this contact");


                                                    DeclineMessageRequestBtn.setVisibility(View.INVISIBLE);
                                                    DeclineMessageRequestBtn.setEnabled(false);

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

    private void CancelChatRequest() {

        ChatRequestRef.child(senderUserID).child(reciveUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ChatRequestRef.child(reciveUserID)
                            .child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if(task.isSuccessful())
                            {

                                SendMessageRequestBtn.setEnabled(true);
                                currentState = "new";
                                SendMessageRequestBtn.setText("Send Message");

                                DeclineMessageRequestBtn.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestBtn.setEnabled(false);



                            }
                        }
                    });

                }
            }
        });
    }

    private void SendChatRequest() {

        Requests requests = new Requests(reciveUserID,"sent");

        ChatRequestRef.child(senderUserID).child(reciveUserID).setValue(requests).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Requests requests1 = new Requests(senderUserID,"received");
                            ChatRequestRef.child(reciveUserID).child(senderUserID).setValue(requests1).
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                SendMessageRequestBtn.setEnabled(true);
                                                currentState = "request_sent";
                                                 SendMessageRequestBtn.setText("Cancel Chat Request");
                                                 DeclineMessageRequestBtn.setVisibility(View.INVISIBLE);


                                            }
                                        }
                                    });


                        }
                    }
                });



//
//        ChatRequestRef.child(senderUserID).child(reciveUserID)
//                .child("request_type").setValue("sent")
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull @NotNull Task<Void> task) {
//                        if(task.isSuccessful()){
//                            ChatRequestRef.child(reciveUserID).child(senderUserID)
//                                    .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull @NotNull Task<Void> task) {
//
//                                    if(task.isSuccessful())
//                                    {
//                                        SendMessageRequestBtn.setEnabled(true);
//                                        currentState = "request_sent";
//                                        SendMessageRequestBtn.setText("Cancel Chat Request");
//
//                                    }
//
//                                }
//                            });
//
//                        }
//                    }
//                });

//        HashMap<String,Object>updateRequest = new HashMap<>();
//        updateRequest.put("request_type","sent");
//        updateRequest.put("id",reciveUserID);
//
//        ChatRequestRef.child(senderUserID).child(reciveUserID)
//                .updateChildren(updateRequest);
//
//        HashMap<String,Object>updateRequest2 = new HashMap<>();
//        updateRequest2.put("request_type","received");
//        updateRequest2.put("id",senderUserID);
//
//        ChatRequestRef.child(reciveUserID).child(senderUserID)
//                .updateChildren(updateRequest2);






    }
}