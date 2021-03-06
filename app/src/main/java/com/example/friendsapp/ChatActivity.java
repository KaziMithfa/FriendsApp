package com.example.friendsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReciverId,messageReceiverName,messageReceiverImage;

    private TextView userName,userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;
    private ImageButton sendMessageBtn,sendFilesButton;
    private EditText MessageInputText;
    private FirebaseAuth mAuth;
    private String currentUser;
    private DatabaseReference RootRef;
    private RecyclerView userMessageList;
    private String checker = " ",myUrl = " ";
    private StorageTask  uploadTask;
    private ProgressDialog loadingbar;

    private Uri fileUri;

    private final List<Messages>messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;

    private MessageAdapter messageAdapter;
   private String savecurrentDate,savecurrentTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReciverId = getIntent().getStringExtra("visit_user_id");
        messageReceiverName = getIntent().getStringExtra("visit_user_name");
        messageReceiverImage = getIntent().getStringExtra("visit_image");






        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionView);

        userName = findViewById(R.id.custom_profile_name);
        userImage = findViewById(R.id.custom_profile_image);
        userLastSeen = findViewById(R.id.custom_user_last_seen);

        sendMessageBtn = findViewById(R.id.send_message_button);
        sendFilesButton = findViewById(R.id.send_files_btn);
        MessageInputText = findViewById(R.id.input_message);


        messageAdapter = new MessageAdapter(messagesList);

        userMessageList = findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);

        loadingbar = new ProgressDialog(this);


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        savecurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        savecurrentTime = currentTime.format(calendar.getTime());


        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);




        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendMessage();

            }
        });

        DisplayLastSeen();

        start();

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {

                                "Images",
                                "PDF files",
                                "Ms Word Files"

                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the file");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(i == 0)
                        {

                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select image"),438);

                        }

                        if(i == 1)
                        {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF file"),438);




                        }

                        if(i == 2)
                        {
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select Ms word file"),438);

                        }

                    }
                });

                builder.show();

            }
        });


    }

    private void start(){
        RootRef.child("Messages").child(currentUser).child(messageReciverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                        Messages messages = snapshot.getValue(Messages.class);
                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == 438 && resultCode == RESULT_OK && data.getData() != null){


            loadingbar.setTitle("Sending File");
            loadingbar.setMessage("Please wait , while we are sending the file");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();

            fileUri = data.getData();

            if(! checker.equals("image"))
            {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                String messageSenderRef = "Messages/"+currentUser+"/"+messageReciverId;
                String messageReceiverRef  = "Messages/"+messageReciverId+"/"+currentUser;

                DatabaseReference userMessageKeyRef =RootRef.child("Messages")
                        .child(currentUser).child(messageReciverId).push();

                final   String messagePushId = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(messagePushId+"."+checker);





                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {


                            Map messageTextBody = new HashMap<>();
                            messageTextBody.put("message",task.getResult().getMetadata().getReference().getDownloadUrl().toString());
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",currentUser);
                            messageTextBody.put("to",messageReciverId);
                            messageTextBody.put("messageID",messagePushId);
                            messageTextBody.put("time",savecurrentTime);
                            messageTextBody.put("date",savecurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

                            RootRef.updateChildren(messageBodyDetails);
                            loadingbar.dismiss();

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        loadingbar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                        double p = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                       loadingbar.setMessage((int)p+"% uploading.....");
                    }
                });

            }

            else if( checker.equals("image"))
            {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                String messageSenderRef = "Messages/"+currentUser+"/"+messageReciverId;
                String messageReceiverRef  = "Messages/"+messageReciverId+"/"+currentUser;

                DatabaseReference userMessageKeyRef =RootRef.child("Messages")
                        .child(currentUser).child(messageReciverId).push();

              final   String messagePushId = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(messagePushId+"."+"jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull @NotNull Task task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Uri>task) {

                        if(task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();


                            Map messageTextBody = new HashMap<>();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",currentUser);
                            messageTextBody.put("to",messageReciverId);
                            messageTextBody.put("messageID",messagePushId);
                            messageTextBody.put("time",savecurrentTime);
                            messageTextBody.put("date",savecurrentDate);






                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task task) {

                                    if(task.isSuccessful())
                                    {
                                        loadingbar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        loadingbar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }

                                    MessageInputText.setText(" ");

                                }
                            });

                        }

                    }
                });


            }

            else{
                Toast.makeText(this, "Nothind selected , Error", Toast.LENGTH_SHORT).show();
            }




        }
    }

    private void DisplayLastSeen()
    {

        RootRef.child("Users").child(messageReciverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {


                if(snapshot.child("userState").hasChild("state"))
                {
                    String state = snapshot.child("userState").child("state").toString();
                    String date = snapshot.child("userState").child("date").toString();
                    String time = snapshot.child("userState").child("time").toString();

                    if(state.equals("online"))
                    {
                        userLastSeen.setText("online");
                    }

                    else if(state.equals("offline"))
                    {
                       userLastSeen.setText("Last Seen: "+date+" "+time);
                    }
                }

                else{
                    userLastSeen.setText("offline");

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        RootRef.child("Messages").child(currentUser).child(messageReciverId)
//                .addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
//                        Messages messages = snapshot.getValue(Messages.class);
//                        messagesList.add(messages);
//
//                        messageAdapter.notifyDataSetChanged();
//
//                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
//
//                    }
//
//                    @Override
//                     public void onCancelled(@NonNull @NotNull DatabaseError error) {
//
//                    }
//                });
//
//
//    }

    private void SendMessage() {

        String messageText = MessageInputText.getText().toString();
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "first write your message..........", Toast.LENGTH_SHORT).show();
        }

        else{
            String messageSenderRef = "Messages/"+currentUser+"/"+messageReciverId;
            String messageReceiverRef  = "Messages/"+messageReciverId+"/"+currentUser;

            DatabaseReference userMessageKeyRef =RootRef.child("Messages")
                    .child(currentUser).child(messageReciverId).push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap<>();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",currentUser);
            messageTextBody.put("to",messageReciverId);
            messageTextBody.put("messageID",messagePushId);
            messageTextBody.put("time",savecurrentTime);
            messageTextBody.put("date",savecurrentDate);




           // messageTextBody.put("message",messageText);
           // messageTextBody.put("message",messageText);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull @NotNull Task task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    MessageInputText.setText(" ");

                }
            });

        }

    }
}