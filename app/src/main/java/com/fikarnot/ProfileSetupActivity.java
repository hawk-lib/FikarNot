package com.fikarnot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fikarnot.connectivity.Internet;
import com.fikarnot.ui.profile.DataModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetupActivity extends AppCompatActivity {

    private CircleImageView profile_pic;
    private Button browse, submit_data;
    private TextInputEditText name_edittext,create_pin_edittext, confirm_pin_edittext;
    private  Uri filepath;
    private Bitmap bitmap;
    private String uid, name, pin, confirm_pin;
    private ProgressDialog dialog;
    private Internet network;
    private BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        network = new Internet(ProfileSetupActivity.this);
        network.setOnNetworkChangeListener();
        initialize();
    }

    private void initialize() {
        profile_pic = findViewById(R.id.profile_pic);
        browse = findViewById(R.id.browse);
        submit_data = findViewById(R.id.submit_data);
        name_edittext = findViewById(R.id.name_edittext);
        create_pin_edittext = findViewById(R.id.create_pin_edittext);
        confirm_pin_edittext = findViewById(R.id.confirm_pin_edittext);
        uid = SharedPrefManager.getInstance(ProfileSetupActivity.this).getUid();


        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withActivity(ProfileSetupActivity.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent, "Choose Profile Pic"), 1);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        submit_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = name_edittext.getText().toString();
                pin = create_pin_edittext.getText().toString();
                confirm_pin = confirm_pin_edittext.getText().toString();

                if (name.length() <= 2){
                    name_edittext.setError("Minimum 3 characters required!");
                    name_edittext.requestFocus();
                }else if(pin.length()<= 3){
                    create_pin_edittext.setError("Minimum/Maximum 4 characters!");
                    create_pin_edittext.requestFocus();
                }else if(confirm_pin.length() <= 3){
                    confirm_pin_edittext.setError("Minimum/Maximum 4 characters");
                    confirm_pin_edittext.requestFocus();
                }else if(pin.equals(confirm_pin)){
                    if (network.checkInternetConnection()) {
                        dialog = new ProgressDialog(ProfileSetupActivity.this);
                        dialog.setCancelable(false);
                        dialog.setTitle("Creating Account");
                        if (filepath != null) {
                            dialog.show();
                            FirebaseStorage storage = FirebaseStorage.getInstance();
//                StorageReference uploader = storage.getReference("Image1"+new Random().nextInt(50));
                            StorageReference uploader = storage.getReference("USERS").child(uid);
                            uploader.child("user_dp").putFile(filepath)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            uploader.child("user_dp").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    SharedPrefManager.getInstance(ProfileSetupActivity.this).saveUri(uri.toString());
                                                    storeImageInInternalStorage();
                                                    createAccount(uri.toString());

                                                }
                                            });
                                        }
                                    })
                                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                            float percent = ((100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                                            dialog.setMessage("Uploading Profile picture (" + (int) percent + "%)");
                                        }
                                    });
                        } else {
                            dialog.show();
                            createAccount("null");
                        }
                    }
                }else{
                    confirm_pin_edittext.setError("Pin not matched!");
                    confirm_pin_edittext.requestFocus();
                }
            }
        });
    }

    private void createAccount(String uri) {
        dialog.setMessage("Please wait while we create your account..");
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference root = db.getReference().child("USERS");
        DataModel obj = new DataModel(name, pin, uri);
        root.child(uid).setValue(obj).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    storeProfileInPreferences(obj);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                            startActivity(new Intent(ProfileSetupActivity.this, MainActivity.class));
                        }
                    }, 1000);
                }else{
                    dialog.dismiss();
                    Toast.makeText(ProfileSetupActivity.this, "Oops, something wrong.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void storeProfileInPreferences(DataModel obj) {
        SharedPrefManager.getInstance(ProfileSetupActivity.this).saveName(obj.getName());
        SharedPrefManager.getInstance(ProfileSetupActivity.this).savePin(obj.getPin());
        dialog.dismiss();
    }

    private void storeImageInInternalStorage() {
        File directory=new File(new ContextWrapper(getApplicationContext()).getDir("imageDir", Context.MODE_PRIVATE),"dp"+".jpeg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(directory);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if (requestCode==1 && resultCode==RESULT_OK) {
            filepath = data.getData();
            if (filepath != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(filepath);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    profile_pic.setImageBitmap(bitmap);
                } catch (Exception ex) {

                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    protected void onResume() {
        super.onResume();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.fikarnot.NOT_CONNECTED")){
                    if (dialog != null){
                        dialog.dismiss();
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.fikarnot.NOT_CONNECTED");
        intentFilter.addAction("com.fikarnot.CONNECTED");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

}