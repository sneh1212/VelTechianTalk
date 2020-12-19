package com.veltech.firebase.ravi.veltechiantalks;

import android.app.ProgressDialog;
import  android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import  android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.AuthProvider;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int RC_PHOTO_PICKER = 2,camera_code=7;
    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN=1;

    private ProgressDialog pd;
    private Uri imageUri;
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    //private ImageButton mPhotoPickerButton;
    private EmojiconEditText mMessageEditText;
    private ImageButton mSendButton;

    private CoordinatorLayout coordintor;
    private ImageView emojiImageView;
    private EmojIconActions emojIconActions;
    private FloatingActionButton mfab,mPhotoPickerButton,mcamera;
    private String mUsername;

private FirebaseDatabase mfirebaseDatabase;
private DatabaseReference mMessagesDatabaseReference;
private ChildEventListener mChildEventListener;

private FirebaseAuth mFirebaseAuth;
private  FirebaseAuth.AuthStateListener mAuthStateListener;
private FirebaseStorage mFirebaseStorage;
private StorageReference mchatPhotoStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;


        coordintor=(CoordinatorLayout)findViewById(R.id.coordinatorLayout);
        mfirebaseDatabase=FirebaseDatabase.getInstance();
        mFirebaseAuth=FirebaseAuth.getInstance();
        mFirebaseStorage=FirebaseStorage.getInstance();

        mMessagesDatabaseReference=mfirebaseDatabase.getReference().child("message");
        mchatPhotoStorageReference=mFirebaseStorage.getReference().child("chat_photos");

        // Initialize references to views
        pd=new ProgressDialog(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
       // mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        emojiImageView=(ImageView)findViewById(R.id.emojiButton);
        mMessageEditText = (EmojiconEditText) findViewById(R.id.messageEditText);
        mSendButton = (ImageButton) findViewById(R.id.sendButton);

//floating button initialization

        mfab=(FloatingActionButton) findViewById(R.id.floatingActionButton);
        mPhotoPickerButton=(FloatingActionButton) findViewById(R.id.gallery);
        mcamera=(FloatingActionButton) findViewById(R.id.camera);


        //emoji initializaztion
        emojIconActions=new EmojIconActions(getApplicationContext(),coordintor,emojiImageView,mMessageEditText);
        emojIconActions.ShowEmojicon();
        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);



        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        //floting button clicked event
        mfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPhotoPickerButton.getVisibility()!=View.VISIBLE&&mcamera.getVisibility()!=View.VISIBLE)
                {
                    mPhotoPickerButton.setVisibility(View.VISIBLE);
                    mcamera.setVisibility(View.VISIBLE);
                }
                else
                {

                    mPhotoPickerButton.setVisibility(View.GONE);
                    mcamera.setVisibility(View.GONE);
                }
            }
        });

        // ImagePickerButton shows an image picker to upload a image for a message

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);

            }
        });

        mcamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent m_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                imageUri = getImageUri();
                m_intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(m_intent, camera_code);


            }
        });


        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click

                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null);
                mMessagesDatabaseReference.push().setValue(friendlyMessage);

                // Clear input box
                mMessageEditText.setText("");
            }
        });


 mAuthStateListener=new FirebaseAuth.AuthStateListener() {
     @Override
     public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

         FirebaseUser user=firebaseAuth.getCurrentUser();
         if(user!=null)
         {
             OnSignedInInitialized(user.getDisplayName());

         }
         else
         {
             OnSignedOutCleanup();
             startActivityForResult(
                     AuthUI.getInstance()
                             .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                             .setProviders(
                                     AuthUI.EMAIL_PROVIDER,
                                     AuthUI.GOOGLE_PROVIDER)
                             .build(),
                     RC_SIGN_IN);
         }

     }
 };


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK)
                Toast.makeText(MainActivity.this, "signed in..", Toast.LENGTH_SHORT).show();
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "signed in cancelled..", Toast.LENGTH_SHORT).show();

                finish();
            }
        }

        else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK)
            {


                Uri selectedImageUri = data.getData();

                StorageReference photoRef = mchatPhotoStorageReference.child(selectedImageUri.getLastPathSegment());
                //upload image file to firebase storage
                pd.setMessage("uploading..");
                pd.show();

                photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        FriendlyMessage friendlyMessage = new FriendlyMessage(null, mUsername, downloadUrl.toString());
                        mMessagesDatabaseReference.push().setValue(friendlyMessage);

                        pd.dismiss();
                    }

                });
            }
        else if (requestCode == camera_code && resultCode == RESULT_OK)
        {
            StorageReference photoRef = mchatPhotoStorageReference.child(imageUri.getLastPathSegment());
            //upload image file to firebase storage
            pd.setMessage("uploading");
            pd.show();
            photoRef.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    FriendlyMessage friendlyMessage = new FriendlyMessage(null, mUsername, downloadUrl.toString());
                    mMessagesDatabaseReference.push().setValue(friendlyMessage);

                    pd.dismiss();
                }
            });

            }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
//signout code
AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener!=null)
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        detachDatabaseReadListener();
        mMessageAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

    }
    private void OnSignedInInitialized(String username) {
mUsername=username;
attachDatabaseReadListener();

    }
    private void OnSignedOutCleanup() {
mUsername=ANONYMOUS;
mMessageAdapter.clear();
detachDatabaseReadListener();


    }
    private void attachDatabaseReadListener()
    {
        if(mChildEventListener==null){
        mChildEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                FriendlyMessage friendlyMessage=dataSnapshot.getValue(FriendlyMessage.class);
                mMessageAdapter.add(friendlyMessage);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
    }}
    private void detachDatabaseReadListener()
    {
        if(mChildEventListener!=null)
        mMessagesDatabaseReference.removeEventListener(mChildEventListener);
        mChildEventListener=null;
    }

    private Uri getImageUri(){
        Uri mimgUri = null;
        File mfile;
        try {
            SimpleDateFormat msdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
             String mcurentDateandTime = msdf.format(new Date());
          File  mimagePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mcurentDateandTime + ".jpg");
            mfile = new File(String.valueOf(mimagePath));
            mimgUri = Uri.fromFile(mfile);
        } catch (Exception pe) {
        }
        return mimgUri;
    }

}
