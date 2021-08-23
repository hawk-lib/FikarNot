package com.fikarnot.ui.authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fikarnot.MainActivity;
import com.fikarnot.R;
import com.fikarnot.SharedPrefManager;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

public class LockScreenActivity extends AppCompatActivity implements View.OnClickListener{

    private Button b0, b1, b2, b3, b4, b5, b6, b7, b8, b9;
    private ImageView backspace, logout;
    private TextView pin1;
    private TextView pin2;
    private TextView pin3;
    private TextView pin4;
    private static String name, pin, real_pin;
    private static int count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        real_pin = SharedPrefManager.getInstance(LockScreenActivity.this).getPin();
        String[] get_name = SharedPrefManager.getInstance(LockScreenActivity.this).getName().split(" ");
        name = "Hello, " + get_name[0];
        init();


    }
    private void init() {
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LockScreenActivity.this);
                AlertDialog dialog = builder.create();
                dialog.setTitle("Logout?");
                dialog.setMessage("Are you sure?");
                dialog.setCancelable(true);
                dialog.setButton(Dialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Dialog myDialog = new Dialog(LockScreenActivity.this);
                        boolean flag = false;
                        try {
                            myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            myDialog.setContentView(R.layout.loading_layout);
                            myDialog.setCancelable(false);
                            myDialog.getWindow().setBackgroundDrawable(LockScreenActivity.this.getDrawable(R.drawable.card_white));
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
                                    finishAndRemoveTask();
                                    System.exit(0);
                                } else {
                                    Toast.makeText(LockScreenActivity.this, "Try Again!", Toast.LENGTH_SHORT).show();
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
            }
        });
        backspace = findViewById(R.id.backspace);
        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (count > 1) {
                    pin = pin.substring(0, count - 1);

                    if (count == 4) {
                        pin4.setText("K");
                        pin4.setBackground(getResources().getDrawable(R.drawable.pin_indicator_transparent));
                    } else if (count == 3) {
                        pin3.setText("C");
                        pin3.setBackground(getResources().getDrawable(R.drawable.pin_indicator_transparent));
                    } else {
                        pin2.setText("O");
                        pin2.setBackground(getResources().getDrawable(R.drawable.pin_indicator_transparent));
                    }
                    count--;
                } else {
                    count = 0;
                    pin = "";
                    pin1.setText("L");
                    pin1.setBackground(getResources().getDrawable(R.drawable.pin_indicator_transparent));
                }

            }
        });

        TextView textview = findViewById(R.id.textView);
        textview.setText(name);

        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);

        b0 = findViewById(R.id.b0);
        b1 = findViewById(R.id.b1);
        b2 = findViewById(R.id.b2);
        b3 = findViewById(R.id.b3);
        b4 = findViewById(R.id.b4);
        b5 = findViewById(R.id.b5);
        b6 = findViewById(R.id.b6);
        b7 = findViewById(R.id.b7);
        b8 = findViewById(R.id.b8);
        b9 = findViewById(R.id.b9);

        b0.setOnClickListener(this);
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);
        b4.setOnClickListener(this);
        b5.setOnClickListener(this);
        b6.setOnClickListener(this);
        b7.setOnClickListener(this);
        b8.setOnClickListener(this);
        b9.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (count == 0) {

            count++;
            if (id == R.id.b0) {
                pin = "0";
            } else if (id == R.id.b1) {
                pin = "1";
            } else if (id == R.id.b2) {
                pin = "2";
            } else if (id == R.id.b3) {
                pin = "3";
            } else if (id == R.id.b4) {
                pin = "4";
            } else if (id == R.id.b5) {
                pin = "5";
            } else if (id == R.id.b6) {
                pin = "6";
            } else if (id == R.id.b7) {
                pin = "7";
            } else if (id == R.id.b8) {
                pin = "8";
            } else if (id == R.id.b9) {
                pin = "9";
            }



            pin1.setText("");
            pin1.setBackground(getResources().getDrawable(R.drawable.pin_indicator_black));

        } else {
            count++;
            if (id == R.id.b0) {
                pin += "0";
            } else if (id == R.id.b1) {
                pin += "1";
            } else if (id == R.id.b2) {
                pin += "2";
            } else if (id == R.id.b3) {
                pin += "3";
            } else if (id == R.id.b4) {
                pin += "4";
            } else if (id == R.id.b5) {
                pin += "5";
            } else if (id == R.id.b6) {
                pin += "6";
            } else if (id == R.id.b7) {
                pin += "7";
            } else if (id == R.id.b8) {
                pin += "8";
            } else if (id == R.id.b9) {
                pin += "9";
            }


            if (count == 2) {
                pin2.setText("");
                pin2.setBackground(getResources().getDrawable(R.drawable.pin_indicator_black));
            } else if (count == 3) {
                pin3.setText("");
                pin3.setBackground(getResources().getDrawable(R.drawable.pin_indicator_black));
            } else {
                pin4.setText("");
                pin4.setBackground(getResources().getDrawable(R.drawable.pin_indicator_black));
                b0.setEnabled(false);
                b1.setEnabled(false);
                b2.setEnabled(false);
                b3.setEnabled(false);
                b4.setEnabled(false);
                b5.setEnabled(false);
                b6.setEnabled(false);
                b7.setEnabled(false);
                b8.setEnabled(false);
                b9.setEnabled(false);
                backspace.setEnabled(false);
                logout.setEnabled(false);

                if (pin.equals(real_pin)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pin = "";
                            count = 0;
                            pin1.setText("O");
                            pin2.setText("P");
                            pin3.setText("E");
                            pin4.setText("N");
                            pin1.setBackground(getResources().getDrawable(R.drawable.pin_indicator_yellow));
                            pin2.setBackground(getResources().getDrawable(R.drawable.pin_indicator_yellow));
                            pin3.setBackground(getResources().getDrawable(R.drawable.pin_indicator_yellow));
                            pin4.setBackground(getResources().getDrawable(R.drawable.pin_indicator_yellow));

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                    startActivity(new Intent(LockScreenActivity.this, MainActivity.class));
                                }
                            }, 1000);
                        }
                    }, 200);
                }else{
                    new Handler().postDelayed(new Runnable() {
                        @SuppressLint("UseCompatLoadingForDrawables")
                        @Override
                        public void run() {
                            pin = "";
                            count = 0;
                            pin1.setBackground(getResources().getDrawable(R.drawable.pin_indicator_red));
                            pin2.setBackground(getResources().getDrawable(R.drawable.pin_indicator_red));
                            pin3.setBackground(getResources().getDrawable(R.drawable.pin_indicator_red));
                            pin4.setBackground(getResources().getDrawable(R.drawable.pin_indicator_red));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pin1.setText("L");
                                    pin2.setText("O");
                                    pin3.setText("C");
                                    pin4.setText("K");
                                    pin1.setBackground(getResources().getDrawable(R.drawable.pin_indicator_transparent));
                                    pin2.setBackground(getResources().getDrawable(R.drawable.pin_indicator_transparent));
                                    pin3.setBackground(getResources().getDrawable(R.drawable.pin_indicator_transparent));
                                    pin4.setBackground(getResources().getDrawable(R.drawable.pin_indicator_transparent));
                                    b0.setEnabled(true);
                                    b1.setEnabled(true);
                                    b2.setEnabled(true);
                                    b3.setEnabled(true);
                                    b4.setEnabled(true);
                                    b5.setEnabled(true);
                                    b6.setEnabled(true);
                                    b7.setEnabled(true);
                                    b8.setEnabled(true);
                                    b9.setEnabled(true);
                                    backspace.setEnabled(true);
                                    logout.setEnabled(true);
                                }
                            }, 1000);
                        }
                    }, 200);

                }


            }

        }
    }

}