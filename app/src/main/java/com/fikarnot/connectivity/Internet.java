package com.fikarnot.connectivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.fikarnot.R;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Internet {

    private Context context;
    public ConnectivityManager connectivityManager;
    public ConnectivityManager.NetworkCallback networkCallback;
    public boolean isNetworkAvailable;
    private Dialog myDialog;
    NetworkRequest request;
    AlertDialog alertDialog;
    View view;

    public Internet(Context context) {
        this.context = context;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (hasNetworkAvailable()){
            isNetworkAvailable = true;
        }else {
            noInternetDialog();
            isNetworkAvailable = false;
        }
    }

    private boolean hasNetworkAvailable() {
        connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void setOnNetworkChangeListener() {
        networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onLost(Network network){
                isNetworkAvailable = false;
                noInternetDialog();
                context.sendBroadcast(new Intent("com.fikarnot.NOT_CONNECTED"));

            }
            @Override
            public void onAvailable(Network network){
                if (alertDialog != null){
                    alertDialog.dismiss();
                }
                isNetworkAvailable = true;
                context.sendBroadcast(new Intent("com.fikarnot.CONNECTED"));
            }
        };
        request = new NetworkRequest.Builder().build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    private void noInternetDialog() {

        if (alertDialog != null){
            alertDialog.dismiss();
        }
        view = LayoutInflater.from(context).inflate(R.layout.network_dialog, null);
        TextView status = view.findViewById(R.id.status);
        Button actionBT = view.findViewById(R.id.actionBT);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

    private boolean poorConnectionDialog() {
        if (alertDialog != null){
            alertDialog.dismiss();
        }
        view = LayoutInflater.from(context).inflate(R.layout.network_dialog, null);
        TextView status = view.findViewById(R.id.status);
        Button actionBT = view.findViewById(R.id.actionBT);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
