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



public class GetPhoneFragment extends Fragment {


    private EditText phone_numberET, country_codeET;
    OTP OTP;

    public GetPhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_get_phone, container, false);

        String prefix = "+91";
        country_codeET = view.findViewById(R.id.country_codeET);
        country_codeET.setText(prefix);
        country_codeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                country_codeET.setSelection(country_codeET.getText().length());
            }
        });
        phone_numberET = view.findViewById(R.id.numberET);
        phone_numberET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone_numberET.setSelection(phone_numberET.getText().length());
            }
        });

        phone_numberET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO){
                    performCheck();
                    return true;
                }
                return false;
            }
        });
        Button get_otpBT = view.findViewById(R.id.get_otpBT);

        get_otpBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCheck();
            }
        });
        return view;
    }

    private void performCheck() {
        String phone;
        String number = phone_numberET.getText().toString();
        String code = country_codeET.getText().toString();
        if (number.length() <= 9) {
            phone_numberET.setError("Enter valid mobile number!");
            phone_numberET.requestFocus();
        } else if (code.length() <= 1) {
            country_codeET.setError("Enter valid country code!");
            country_codeET.requestFocus();
        } else {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(phone_numberET.getWindowToken(),0);
            phone = code+number;
            OTP.getOTP(phone);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            OTP = (OTP) context;
        } catch (ClassCastException e){
            throw  new ClassCastException(context.toString()+e.getMessage());
        }
    }
}