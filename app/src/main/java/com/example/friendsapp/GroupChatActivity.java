package com.example.friendsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displaytextMessage;
    private FirebaseAuth mAuth;
    private String currentGroupName,currentUserId,currentUserName;
    private DatabaseReference UsersRef,GroupNameRef,GroupMessageKeyRef;
    private String currentDate,currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getStringExtra("groupName");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);




        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        displaytextMessage = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.my_scroll_view);

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfotoDatabase();
                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });






    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded( DataSnapshot snapshot,  String previousChildName) {

                if(snapshot.exists())
                {
                    DisplayMessages(snapshot);
                }

            }

            @Override
            public void onChildChanged( DataSnapshot snapshot, String previousChildName) {

                if(snapshot.exists())
                {
                    DisplayMessages(snapshot);
                }



            }

            @Override
            public void onChildRemoved( DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved( DataSnapshot snapshot,  String previousChildName) {

            }

            @Override
            public void onCancelled( DatabaseError error) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot snapshot) {

       Iterator iterator = snapshot.getChildren().iterator();


       while (iterator.hasNext())
       {
           String chatDate = ((DataSnapshot) iterator.next()).getValue().toString();
           String chatMessage = ((DataSnapshot) iterator.next()).getValue().toString();
           String chatName = ((DataSnapshot) iterator.next()).getValue().toString();
           String chatTime = ((DataSnapshot) iterator.next()).getValue().toString();


           displaytextMessage.append(chatName+": \n"+chatMessage +"\n"+chatTime+"\n"+chatDate+"\n\n\n");

           mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
       }


    }

    private void SaveMessageInfotoDatabase() {

        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please input message first.............", Toast.LENGTH_SHORT).show();
        }

        else{
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat  = new SimpleDateFormat("hh:mm");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String,Object>groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messageKey);



            HashMap<String,Object>messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);

        }

    }

    private void GetUserInfo() {

        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    currentUserName = snapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled( DatabaseError error) {

            }
        });


    }
}