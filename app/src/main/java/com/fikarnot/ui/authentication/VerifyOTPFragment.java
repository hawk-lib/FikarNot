package com.fikarnot.ui.authentication;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fikarnot.R;
import com.fikarnot.SharedPrefManager;


public class VerifyOTPFragment extends Fragment {

    EditText verification_codeET;
    Button submitBT, backBT;
    TextView phone_number;
    OTP OTP;

    public VerifyOTPFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_verify_otp, container, false);

        verification_codeET = view.findViewById(R.id.verification_codeET);

        verification_codeET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO){
                    performCheck();
                    return true;
                }
                return false;
            }
        });
        phone_number = view.findViewById(R.id.phone_number);
        phone_number.setText(SharedPrefManager.getInstance(getContext()).getMobile());
        submitBT = view.findViewById(R.id.submitBT);
        backBT = view.findViewById(R.id.change_mobile_number);


        backBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OTP.prevFragment();
            }
        });

        submitBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCheck();
            }
        });
        return view;
    }

    private void performCheck() {
        String otp = verification_codeET.getText().toString();
        if (otp.length() <= 3){
            verification_codeET.setError("Enter OTP!");
            verification_codeET.requestFocus();
        }else {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(verification_codeET.getWindowToken(),0);
            OTP.verifyOTP(otp);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OTP = (OTP) context;
    }
}