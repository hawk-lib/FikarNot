package com.fikarnot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.fikarnot.connectivity.Internet;
import com.fikarnot.ui.authentication.FragmentAdapter;
import com.fikarnot.ui.authentication.OTP;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;


public class AuthenticationActivity extends AppCompatActivity implements OTP {

    private FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ViewPager2 viewPager2;
    FragmentAdapter fragmentAdapter;
    Dialog myDialog;
    private Internet network;
    BroadcastReceiver broadcastReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        mAuth = FirebaseAuth.getInstance();
        network = new Internet(AuthenticationActivity.this);
        network.setOnNetworkChangeListener();
        viewPager2 = findViewById(R.id.viewPager2);
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager2.setUserInputEnabled(false);
        viewPager2.setAdapter(fragmentAdapter);

    }




    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        if (network.checkInternetConnection()) {
            myDialog = new Dialog(AuthenticationActivity.this);
            myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            myDialog.setContentView(R.layout.loading_layout);
            myDialog.setCancelable(false);
            myDialog.getWindow().setBackgroundDrawable(AuthenticationActivity.this.getDrawable(R.drawable.card_white));
            myDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            myDialog.show();
            mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        SharedPrefManager.getInstance(AuthenticationActivity.this).saveUid(uid);

                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference root = db.getReference("USERS").child(uid);
                        root.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String name = String.valueOf(snapshot.child("name").getValue());
                                    String pin = String.valueOf(snapshot.child("pin").getValue());
                                    String uri = String.valueOf(snapshot.child("uri").getValue());

                                    SharedPrefManager.getInstance(AuthenticationActivity.this).saveUser(uid, name, pin, uri);
                                    myDialog.dismiss();

                                    finish();
                                    startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));

                                } else {
                                    myDialog.dismiss();
                                    finish();
                                    startActivity(new Intent(AuthenticationActivity.this, ProfileSetupActivity.class));
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                myDialog.dismiss();
                                Toast.makeText(AuthenticationActivity.this, "Unexpected error!", Toast.LENGTH_LONG).show();
                            }
                        });

                    } else {
                        myDialog.dismiss();
                        Toast.makeText(AuthenticationActivity.this, "Invalid Otp!", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }

    @Override
    public void getOTP(String phone) {
        if (network.checkInternetConnection()) {
            myDialog = new Dialog(AuthenticationActivity.this);
            myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            myDialog.setContentView(R.layout.loading_layout);
            myDialog.setCancelable(false);
            myDialog.getWindow().setBackgroundDrawable(AuthenticationActivity.this.getDrawable(R.drawable.card_white));
            myDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            myDialog.show();
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


                @Override
                public void onVerificationFailed(FirebaseException e) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    myDialog.dismiss();

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        // ...
                        Toast.makeText(AuthenticationActivity.this, "Invalid Request", Toast.LENGTH_SHORT).show();
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        // ...
                        Toast.makeText(AuthenticationActivity.this, "Registration full for today", Toast.LENGTH_SHORT).show();
                    }

                    // Show a message and update the UI
                    // ...
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    //signInWithPhoneAuthCredential(phoneAuthCredential);
                    final String otp = phoneAuthCredential.getSmsCode();
                    if (otp != null) {
                        myDialog.dismiss();
                        verifyOTP(otp);
                    }
//                        viewPager2.setCurrentItem(2);
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.

                    // Save verification ID and resending token so we can use them later
                    myDialog.dismiss();
                    mVerificationId = verificationId;
                    mResendToken = token;
                    SharedPrefManager.getInstance(AuthenticationActivity.this).saveMobile(phone);
                    viewPager2.setCurrentItem(1, true);

                    // ...
                }
            };
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber(phone)
                    .setTimeout(120L, TimeUnit.SECONDS)
                    .setActivity(AuthenticationActivity.this)
                    .setCallbacks(mCallbacks)
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);

        }
    }

    @Override
    public void verifyOTP(String otp) {
        signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(mVerificationId, otp));
    }

    @Override
    public void prevFragment() {
        viewPager2.setCurrentItem(0,true);
    }


    @Override
    protected void onResume() {
        super.onResume();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.fikarnot.NOT_CONNECTED")){
                    if (myDialog != null){
                        myDialog.dismiss();
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
