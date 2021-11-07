package com.example.friendsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private Uri imageUri;
    private static final int GalleryPick = 1;
    private StorageTask uploadTask;
    private StorageReference storageprofilepictureRef;
    private String checker = " ";
    private String myUrl = " ";
    private Toolbar SettingsToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        UpdateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);

        SettingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();
        storageprofilepictureRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checker.equals("clicked"))
                {
                    UploadeImage();
                }
                else{
                    UpdateSettings();

                }



            }
        });


        //RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checker = "clicked";

                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);


            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

        RetrieveUserInfo();


    }

    private void UploadeImage() {

        String setuserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setuserName))
        {
            Toast.makeText(this, "Please, write your user name first", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please ,write your status", Toast.LENGTH_SHORT).show();
        }

        else{

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Upload Profile");
            progressDialog.setMessage("Please wait while we are checking the creditinals");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            if(imageUri!= null)
            {

                StorageReference filePath = storageprofilepictureRef.child(currentUserId + ".jpg");
                uploadTask = filePath.putFile(imageUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then( Task task) throws Exception {

                        if(!task.isSuccessful())
                        {
                            throw task.getException();

                        }


                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(Task<Uri> task) {

                        if(task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

                            HashMap<String,Object>userMap = new HashMap<>();
                            userMap.put("name",setuserName);
                            userMap.put("status",setStatus);
                            userMap.put("image",myUrl);

                            ref.child(currentUserId).updateChildren(userMap);
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Profile updated successfully....", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
                            finish();
                        }
                        else {
                            String message = task.getException().toString();
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this,message, Toast.LENGTH_SHORT).show();
                        }





                    }
                });





            }

            else {
                Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }


        }



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode ==RESULT_OK && data!= null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

           userProfileImage.setImageURI(imageUri);


        }

        else{
            Toast.makeText(this, "Error: please try again....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this,SettingsActivity.class));
            finish();


        }


    }

    private void RetrieveUserInfo() {




        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {

                        if(snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("image"))
                        {

                            String retrieveUserName = snapshot.child("name").getValue().toString();
                            String retrieveUserStatus = snapshot.child("status").getValue().toString();
                            String retrieveUserImage = snapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                            Picasso.get().load(retrieveUserImage).into(userProfileImage);
                        }

                        else if(snapshot.exists() && snapshot.hasChild("name")){

                            String retrieveUserName = snapshot.child("name").getValue().toString();
                            String retrieveUserStatus = snapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);



                        }


                        else{
                            Toast.makeText(SettingsActivity.this, "Please , insert the image and necessary information", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });


    }



    private void UpdateSettings() {

        String setuserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setuserName))
        {
            Toast.makeText(this, "Please, write your user name first", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please ,write your status", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String,Object>profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setuserName);
            profileMap.put("status",setStatus);

            RootRef.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete( Task<Void> task) {

                            if(task.isSuccessful())
                            {

                                Toast.makeText(SettingsActivity.this, "Profile update successfullly.....!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            }

                            else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        }


    }
}