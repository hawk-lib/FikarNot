package com.fikarnot;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    //Preference Name
    private static final String shared_pref_name = "FikarNot_Preferences";
    //Preference keys
    private static final String name = "Name";
    private static final String pin = "Pin";
    private static final String uri = "Uri";
    private static final String uid = "Uid";
    private static final String mobile = "Mobile";
    //Instance
    private static SharedPrefManager mInstance;
    //Context
    private static Context mCtx;
    private  static SharedPreferences sharedPreferences;


    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context){
        if(mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    public String getPin(){
        sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(pin, "");
    }

    public String getName(){
        sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(name, "");
    }


    public String getUid() {
        sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(uid, "");
    }

    public String getUri() {
        sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(uri, "null");
    }

    public String getMobile() {
        sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(mobile, "");
    }


    public void saveName(String _name) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, _name);
        editor.apply();
    }

    public void savePin(String _pin) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(pin, _pin);
        editor.apply();
    }

    public void saveUri(String _uri) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(uri, _uri);
        editor.apply();
    }
    public void saveUid(String _uid) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(uid, _uid);
        editor.apply();
    }

    public void saveUser(String _uid, String _name, String _pin, String _uri) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(uid, _uid);
        editor.putString(name, _name);
        editor.putString(pin, _pin);
        editor.putString(uri, _uri);
        editor.apply();
    }

    public void saveMobile(String _mobile) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(mobile, _mobile);
        editor.apply();
    }


    public void clear() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(uid, "");
        editor.putString(name, "");
        editor.putString(pin, "");
        editor.putString(uri, "");
        editor.putString(mobile,"");
        editor.apply();
    }
}
