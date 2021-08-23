package com.fikarnot.ui.authentication;

public interface OTP {
    void getOTP(String phone);
    void verifyOTP(String otp);
    void prevFragment();
}
