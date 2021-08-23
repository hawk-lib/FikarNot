package com.fikarnot;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;

import com.fikarnot.connectivity.Internet;
import com.fikarnot.ui.main.AccountsModel;
import com.fikarnot.ui.main.FragmentAdapter;
import com.fikarnot.ui.main.NewsFragment;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FragmentAdapter fragmentAdapter;
    ViewPager2 viewPager2;
    TabLayout tabs;
    FloatingActionMenu floatingMenu;
    FloatingActionButton fab_add, fab_close, fab_refresh;
    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar;
    CircleImageView dpView;
    View navHeader;
    File dp;
    TextView account_name;
    String message = "";
    ImageView open_drawer, close_drawer;
    //BroadcastReceiver broadcastReceiver;
    Uri filepath;
    Bitmap picture;
    ImageView profile_picture;
    String uid;
    Handler handler;
    Runnable runnable;
    //for internet
    private boolean isNetworkAvailable;
    private View view;
    private AlertDialog alertDialog;
    private ConnectivityManager connectivityManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        runnable = () -> exit();
        setOnNetworkChangeListener();
        initialize();

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initialize() {
        viewPager2 = findViewById(R.id.view_pager2);
        floatingMenu = findViewById(R.id.floating_menu);
        fab_add = findViewById(R.id.fab_add);
        fab_close = findViewById(R.id.fab_close);
        fab_refresh = findViewById(R.id.fab_refresh);
        tabs = findViewById(R.id.tabs);
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager2.setAdapter(fragmentAdapter);
        viewPager2.setCurrentItem(1, false);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabs, viewPager2, true, (tab, position) -> {

            if (position == 0){
                tab.setText("News");
                tab.setIcon(getDrawable(R.drawable.ic_news));

            } else if (position == 1) {
                tab.setText("Data");
                tab.setIcon(R.drawable.ic_accounts);
            } else {
                tab.setText("Sale");
                tab.setIcon(R.drawable.ic_sale);
            }
        });
        tabLayoutMediator.attach();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0){
                    fab_add.setVisibility(View.GONE);
                    fab_refresh.setVisibility(View.VISIBLE);
                }else if(position == 1){
                    fab_refresh.setVisibility(View.GONE);
                    fab_add.setVisibility(View.VISIBLE);
                }else{
                    fab_add.setVisibility(View.GONE);
                    fab_refresh.setVisibility(View.GONE);
                }
            }
        });

        uid = SharedPrefManager.getInstance(MainActivity.this).getUid();

        drawer = findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        dp = new File(new ContextWrapper(getApplicationContext()).getDir("imageDir", Context.MODE_PRIVATE),"dp"+".jpeg");
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        navigationView.setItemIconTintList(null);
        navHeader = navigationView.getHeaderView(0);
        dpView = navHeader.findViewById(R.id.dpView);
        open_drawer = findViewById(R.id.open_drawer);
        open_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START,true);
            }
        });
        close_drawer = navHeader.findViewById(R.id.close_drawer);
        close_drawer.setOnClickListener(view -> drawer.closeDrawer(GravityCompat.START,true));

        dpView.setOnClickListener(view -> updateProfile());

        account_name = navHeader.findViewById(R.id.account_name);
        account_name.setText(SharedPrefManager.getInstance(getApplicationContext()).getName());
        account_name.setOnClickListener(view -> updateProfile());

        fab_add.setOnClickListener(new View.OnClickListener() {

            Dialog myDialog;
            TextInputEditText title, username, password;
            ImageView dismiss;

            @Override
            public void onClick(View view) {
                myDialog = new Dialog(MainActivity.this);
                myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                myDialog.setContentView(R.layout.add_account_dialog);
                myDialog.setCancelable(false);
                myDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.round_corners));
                myDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dismiss = myDialog.findViewById(R.id.dismiss3);
                dismiss.setOnClickListener(view1 -> myDialog.dismiss());
                title = myDialog.findViewById(R.id.titleET);
                username = myDialog.findViewById(R.id.usernameET1);
                password = myDialog.findViewById(R.id.passwordET1);

                password.setOnEditorActionListener((textView, i, keyEvent) -> {
                    if (i == EditorInfo.IME_ACTION_GO){
                        performCheck();
                        return true;
                    }
                    return false;
                });
                Button submit = myDialog.findViewById(R.id.submit);
                submit.setOnClickListener(view12 -> performCheck());
                myDialog.show();
            }

            private void performCheck() {
                String account_title = String.valueOf(title.getText()).toUpperCase();
                String uname = String.valueOf(username.getText());
                String pass = String.valueOf(password.getText());
                if (account_title.length() <= 2) {
                    title.setError("Minimum 3 characters required!");
                    title.requestFocus();
                } else if (uname.length() == 0) {
                    username.setError("Enter username...");
                    username.requestFocus();
                } else if (pass.length() <= 3) {
                    password.setError("Enter your password...");
                    password.requestFocus();
                } else {
                    Dialog dialog = new Dialog(MainActivity.this);
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.loading_layout);
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.round_shape_card));
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.show();
                    }finally {
                        if (checkInternetConnection()) {
                            FirebaseDatabase db = FirebaseDatabase.getInstance();
                            DatabaseReference root = db.getReference("USERS")
                                    .child(SharedPrefManager.getInstance(MainActivity.this).getUid())
                                    .child("accounts").child(account_title);
                            root.setValue(new AccountsModel(uname, pass))
                                    .addOnCompleteListener(task -> {
                                        dialog.dismiss();
                                        if (task.isSuccessful()) {
                                            myDialog.dismiss();
                                            Toast.makeText(MainActivity.this, "Added Successfully.", LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Failed to Update!", LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            dialog.dismiss();
                        }
                    }
                }
            }
        });

        fab_close.setOnClickListener(view -> exit());

        fab_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Fragment> fragment = getSupportFragmentManager().getFragments();
                for (Fragment f : fragment){
                    if (f != null && f instanceof NewsFragment){
                        ((NewsFragment)f).pressed();
                        break;
                    }
                }
            }
        });



        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (dp.exists()){
                    Bitmap bitmap = BitmapFactory.decodeFile(dp.getAbsolutePath());
                    dpView.setImageBitmap(bitmap);
                }else if (!SharedPrefManager.getInstance(MainActivity.this).getUri().equals("null")){
                    Picasso.get().load(SharedPrefManager.getInstance(MainActivity.this).getUri()).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            try {
                                FileOutputStream fos = new FileOutputStream(dp);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                fos.close();
                            } catch (java.io.IOException e) {
                                e.printStackTrace();
                            }finally {
                                Bitmap bitmap1 = BitmapFactory.decodeFile(dp.getAbsolutePath());
                                dpView.setImageBitmap(bitmap1);
                            }

                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
                }
            }
        });
    }

    private void updateProfile() {
        filepath = null;
        picture = null;
        Dialog profile_dialog = new Dialog(MainActivity.this);
        profile_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        profile_dialog.setContentView(R.layout.profile_update_dialog);
        profile_dialog.setCancelable(true);
        profile_dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.round_shape_card));
        profile_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        profile_picture = profile_dialog.findViewById(R.id.profile_picture);
        if (dp.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(dp.getAbsolutePath());
            profile_picture.setImageBitmap(bitmap);
        }
        Button remove = profile_dialog.findViewById(R.id.remove);
        Button update_pic = profile_dialog.findViewById(R.id.update_pic);
        Button update_name = profile_dialog.findViewById(R.id.update_name);
        Button browse1 = profile_dialog.findViewById(R.id.browse1);

        TextInputEditText name_edittext1 = profile_dialog.findViewById(R.id.name_edittext1);
        name_edittext1.setText(SharedPrefManager.getInstance(MainActivity.this).getName());
        name_edittext1.setSelection(name_edittext1.getText().length());
        remove.setOnClickListener(view -> {
                if (dp.exists()) {
                    Dialog dialog = new Dialog(MainActivity.this);
                    try {
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.loading_layout);
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.round_shape_card));
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.show();
                    }finally {
                        if (checkInternetConnection()) {
                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(
                                    SharedPrefManager.getInstance(MainActivity.this).getUri()
                            );
                            storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseDatabase.getInstance().getReference("USERS").child(uid).child("uri").setValue("null")
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        SharedPrefManager.getInstance(MainActivity.this).saveUri("null");
                                                        profile_picture.setImageResource(R.mipmap.default_avatar_profile_icon);
                                                        dpView.setImageResource(R.mipmap.default_avatar_profile_icon);
                                                        FileOutputStream fos = null;
                                                        try {
                                                            fos = new FileOutputStream(dp);
                                                            // Use the compress method on the BitMap object to write image to the OutputStream
                                                            picture.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        } finally {
                                                            try {
                                                                Bitmap bitmap = BitmapFactory.decodeFile(dp.getAbsolutePath());
                                                                dpView.setImageBitmap(bitmap);
                                                                fos.close();
                                                                Toast.makeText(MainActivity.this, "Profile photo updated", LENGTH_SHORT).show();
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                            dialog.dismiss();
                                                        }
                                                    }
                                                });
                                        Toast.makeText(MainActivity.this, "Profile photo removed!", LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Failed to remove Profile photo!", LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this,"Profile photo not available!", LENGTH_SHORT).show();
                }
            });
        browse1.setOnClickListener(view -> Dexter.withActivity(MainActivity.this)
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
                }).check());
        update_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkInternetConnection()){
                    if (filepath != null){
                        Dialog dialog = new Dialog(MainActivity.this);
                        try {
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.loading_layout);
                            dialog.setCancelable(false);
                            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.round_shape_card));
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.show();
                        }finally {
                            String link = SharedPrefManager.getInstance(MainActivity.this).getUri();
                            try {
                                if (!link.equals("null")) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(link).delete();
                                }
                            }finally {
                                StorageReference uploader = FirebaseStorage.getInstance().getReference("USERS").child(SharedPrefManager.getInstance(MainActivity.this).getUid());
                                uploader.child("user_dp").putFile(filepath).addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {
                                    uploader.child("user_dp").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            FirebaseDatabase.getInstance().getReference("USERS").child(uid)
                                                    .child("uri").setValue(uri.toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                if (dp.exists()) {
                                                                    dp.delete();
                                                                }
                                                                SharedPrefManager.getInstance(MainActivity.this).saveUri(uri.toString());
                                                                store();
                                                            }
                                                        }

                                                        private void store() {
                                                            FileOutputStream fos = null;
                                                            try {
                                                                fos = new FileOutputStream(dp);
                                                                picture.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            } finally {
                                                                try {
                                                                    Bitmap bitmap = BitmapFactory.decodeFile(dp.getAbsolutePath());
                                                                    dpView.setImageBitmap(bitmap);
                                                                    fos.close();
                                                                    Toast.makeText(MainActivity.this, "Profile photo updated", LENGTH_SHORT).show();
                                                                } catch (IOException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                dialog.dismiss();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                                });
                            }
                        }
                    }else{
                        Toast.makeText(MainActivity.this,"Profile picture not selected!",LENGTH_SHORT).show();
                    }

                }
        }
        });

        update_name.setOnClickListener(view -> {
            String name = name_edittext1.getText().toString();
            if (name.length() <= 2){
                name_edittext1.setError("Minimum 3 characters required!");
                name_edittext1.requestFocus();
            }else {
                Dialog dialog = new Dialog(MainActivity.this);
                try {
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.loading_layout);
                    dialog.setCancelable(false);
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.round_shape_card));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.show();
                }finally {
                    if (checkInternetConnection()) {
                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("USERS").child(uid).child("name");
                        myRef.setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();
                                if (task.isSuccessful()) {
                                    profile_dialog.dismiss();
                                    SharedPrefManager.getInstance(MainActivity.this).saveName(name);
                                    account_name.setText(name);
                                    Toast.makeText(MainActivity.this, "User name updated", LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(MainActivity.this, "Failed to update user name!", LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    profile_dialog.dismiss();
                    dialog.dismiss();
                }
            }
        });

        profile_dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (R.id.change_pin == item.getItemId()){

            Dialog myDialog = new Dialog(MainActivity.this);
            myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            myDialog.setContentView(R.layout.change_pin_layout);
            myDialog.setCancelable(false);
            myDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.round_shape_card));
            myDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Button cancel = myDialog.findViewById(R.id.cancel);
            Button update = myDialog.findViewById(R.id.update);
            TextInputEditText new_pin = myDialog.findViewById(R.id.new_pin);
            TextInputEditText confirm_pin = myDialog.findViewById(R.id.confirm_pin);
            cancel.setOnClickListener(view -> {
                new_pin.getText().clear();
                confirm_pin.getText().clear();
                myDialog.dismiss();
            });
            update.setOnClickListener(view -> {
                String newPIN = String.valueOf(new_pin.getText());
                String confirmPIN = String.valueOf(confirm_pin.getText());

                if (newPIN.length() <= 3){
                    new_pin.setError("Minimum/Maximum 4 characters required!");
                    new_pin.requestFocus();
                }else if(confirmPIN.length()<= 3){
                    confirm_pin.setError("Minimum/Maximum 4 characters required!");
                    confirm_pin.requestFocus();
                }else if(newPIN.equals(confirmPIN)) {
                    Dialog dialog = new Dialog(MainActivity.this);
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(confirm_pin.getWindowToken(), 0);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.loading_layout);
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.round_shape_card));
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.show();
                    }finally {
                        if (checkInternetConnection()) {
                            FirebaseDatabase db = FirebaseDatabase.getInstance();
                            DatabaseReference root = db.getReference().child("USERS")
                                    .child(SharedPrefManager.getInstance(MainActivity.this).getUid());
                            root.child("pin").setValue(newPIN)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            dialog.dismiss();
                                            if (task.isSuccessful()) {
                                                SharedPrefManager.getInstance(MainActivity.this).savePin(newPIN);
                                                myDialog.dismiss();
                                                drawer.closeDrawer(GravityCompat.START, true);
                                                Toast.makeText(getApplicationContext(), "Pin updated Successfully", LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Failed to update pin", LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            dialog.dismiss();
                        }
                    }

                }else{
                    confirm_pin.setError("Pin not matched!");
                    confirm_pin.requestFocus();
                }
            });
            myDialog.show();

        }else if (R.id.share_fikarnot == item.getItemId()){
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo;
            try {
                appInfo = pm.getApplicationInfo("com.fikarnot",
                        PackageManager.GET_META_DATA);
                Intent sendBt = new Intent(Intent.ACTION_SEND);
                sendBt.setType("*/*");
                sendBt.putExtra(Intent.EXTRA_STREAM,
                        Uri.parse("file://" + appInfo.publicSourceDir));
                startActivity(Intent.createChooser(sendBt,
                        "Share app via"));
            } catch (PackageManager.NameNotFoundException e1) {
                e1.printStackTrace();
            }
        }else if (R.id.feedback == item.getItemId()){
            Dialog myDialog = new Dialog(MainActivity.this);
            myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            myDialog.setContentView(R.layout.feedback_layout);
            myDialog.setCancelable(true);
            myDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.card_grey));
            myDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            AppCompatEditText messageET = myDialog.findViewById(R.id.message);
            messageET.setText(message);
            messageET.requestFocus();
            TextView name = myDialog.findViewById(R.id.myName);
            name.setText(SharedPrefManager.getInstance(MainActivity.this).getName());
            myDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    message = String.valueOf(messageET.getText());
                }
            });
            Button send = myDialog.findViewById(R.id.send);
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    message = String.valueOf(messageET.getText());
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(messageET.getWindowToken(), 0);
                    if (message.length() >= 1) {
                        Dialog dialog = new Dialog(MainActivity.this);
                        try {
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(messageET.getWindowToken(), 0);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.loading_layout);
                            dialog.setCancelable(false);
                            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.round_shape_card));
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.show();
                        }finally {
                            if (checkInternetConnection()) {
                                FirebaseDatabase db = FirebaseDatabase.getInstance();
                                DatabaseReference root = db.getReference().child("FEEDBACK")
                                        .child(SharedPrefManager.getInstance(MainActivity.this).getUid());
                                root.child("message").setValue(message)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                dialog.dismiss();
                                                if (task.isSuccessful()) {
                                                    message = "";
                                                    myDialog.dismiss();
                                                    drawer.closeDrawer(GravityCompat.START, true);
                                                    Toast.makeText(MainActivity.this, "Feedback Sent", LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(MainActivity.this, "Oops, something wrong.", LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                dialog.dismiss();
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Empty Message!", LENGTH_SHORT).show();
                    }
                }
            });
            myDialog.show();

        }else if (R.id.about == item.getItemId()){
            Dialog myDialog = new Dialog(MainActivity.this);
            myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            myDialog.setContentView(R.layout.about_layout);
            myDialog.setCancelable(true);
            myDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.card_white));
            myDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            myDialog.show();
            drawer.closeDrawer(GravityCompat.START,true);
        }
        return false;
    }

    private void exit() {
        finishAndRemoveTask();
        System.exit(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.log_out) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            AlertDialog dialog = builder.create();
            dialog.setTitle("Logout?");
            dialog.setMessage("Are you sure?");
            dialog.setCancelable(true);
            dialog.setButton(Dialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Dialog myDialog = new Dialog(MainActivity.this);
                    boolean flag = false;
                    try {
                        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        myDialog.setContentView(R.layout.loading_layout);
                        myDialog.setCancelable(false);
                        myDialog.getWindow().setBackgroundDrawable(MainActivity.this.getDrawable(R.drawable.card_white));
                        myDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        myDialog.show();
                    }finally {
                        try {
                            File dp = new File(new ContextWrapper(getApplicationContext()).getDir("imageDir", Context.MODE_PRIVATE), "dp" + ".jpeg");
                            if (dp.exists()) {
                                if (dp.delete()) {
                                    SharedPrefManager.getInstance(getApplicationContext()).clear();
                                    FirebaseAuth.getInstance().signOut();
                                    flag = true;
                                } else {
                                    flag = false;
                                }
                            } else {
                                SharedPrefManager.getInstance(getApplicationContext()).clear();
                                FirebaseAuth.getInstance().signOut();
                                flag = true;
                            }
                        } finally {
                            myDialog.dismiss();
                            dialog.dismiss();
                            if (flag) {
                                exit();
                            } else {
                                Toast.makeText(MainActivity.this, "Try Again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
            dialog.setButton(Dialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            });
            dialog.show();

        } /*else if (item.getItemId() == R.id.action_restore) {

        }*/

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {

        if (drawer.isDrawerOpen(Gravity.LEFT)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(runnable);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finishAndRemoveTask();
            }
        };
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.postDelayed(runnable,60000);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if (requestCode==1 && resultCode==RESULT_OK) {
            filepath = data.getData();
            if (filepath != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(filepath);
                    picture = BitmapFactory.decodeStream(inputStream);
                    profile_picture.setImageBitmap(picture);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean hasNetworkAvailable() {
        connectivityManager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void setOnNetworkChangeListener() {
        if (hasNetworkAvailable()){
            isNetworkAvailable = true;
        }else {
            noInternetDialog();
            isNetworkAvailable = false;
        }
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onLost(Network network){
                noInternetDialog();
                isNetworkAvailable = false;
            }
            @Override
            public void onAvailable(Network network){
                if (alertDialog != null){
                    alertDialog.dismiss();
                }
                isNetworkAvailable = true;
            }
        };
        NetworkRequest request = new NetworkRequest.Builder().build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    public boolean checkInternetConnection() {

        if (!isNetworkAvailable) {
            noInternetDialog();
            return false;
        } else {

            if (isConnected()){
                return true;
            }else{
                return poorConnectionDialog();
            }

        }
    }

    private void noInternetDialog() {
        if (alertDialog != null){
            alertDialog.dismiss();
        }
        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.network_dialog, null);
        TextView status = view.findViewById(R.id.status);
        Button actionBT = view.findViewById(R.id.actionBT);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String status_text = "No Internet Connection";
        String button_text = "Ok";
        status.setText(status_text);
        actionBT.setText(button_text);
        actionBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean poorConnectionDialog() {
        if (alertDialog != null){
            alertDialog.dismiss();
        }
        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.network_dialog, null);
        TextView status = view.findViewById(R.id.status);
        Button actionBT = view.findViewById(R.id.actionBT);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String status_text = "Poor Network Connection";
        String button_text = "Retry";
        boolean[] result = new boolean[1];
        status.setText(status_text);
        actionBT.setText(button_text);
        actionBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                result[0] = checkInternetConnection();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                alertDialog.dismiss();
                result[0] = false;
            }
        });
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();
        return result[0];
    }
    private boolean isConnected() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            HttpsURLConnection url_conn = (HttpsURLConnection) new URL("https://clients3.google.com/generate_204").openConnection();
            url_conn.setRequestProperty("User-Agent", "Android");
            url_conn.setRequestProperty("Connection", "close");
            url_conn.setConnectTimeout(1000);
            url_conn.connect();
            return url_conn.getResponseCode() == 204 && url_conn.getContentLength() == 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;

        }

    }


}