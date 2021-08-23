package com.fikarnot.ui.main;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fikarnot.MainActivity;
import com.fikarnot.R;
import com.fikarnot.SharedPrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountsFragment extends Fragment {

    final String version = "1";
    List<AccountsModel> accountsModelList;
    AccountsRecyclerAdapter myAdapter;
    RecyclerView recyclerView;
    LinearLayout loading;
    LinearLayout clear_screen;
    SearchView searchView;

    private boolean isNetworkAvailable;
    private View view;
    private AlertDialog alertDialog;
    private ConnectivityManager connectivityManager;

    public AccountsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        setOnNetworkChangeListener();
        recyclerView = view.findViewById(R.id.recyclerView);
        loading = view.findViewById(R.id.loading);
        clear_screen = view.findViewById(R.id.clear_screen);
        searchView = view.findViewById(R.id.search_view);


        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference alert = db.getReference("ALERT");
        alert.child("message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String message = snapshot.getValue().toString();
                if (!message.equals("null")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    AlertDialog dialog = builder.create();
                    dialog.setTitle("Note");
                    dialog.setMessage(message);
                    dialog.setCancelable(true);
                    dialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        alert.child("version").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String latest_version = snapshot.getValue().toString();
                if (!version.equals(latest_version)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    AlertDialog dialog = builder.create();
                    dialog.setTitle("Note");
                    dialog.setMessage("Download latest version of this app..");
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference root = db.getReference("USERS")
                .child(SharedPrefManager.getInstance(getContext()).getUid()).child("accounts");

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    accountsModelList = new ArrayList<>();
                    clear_screen.setVisibility(View.GONE);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String title = dataSnapshot.getKey();
                        String username = String.valueOf(dataSnapshot.child("username").getValue());
                        String password = String.valueOf(dataSnapshot.child("password").getValue());
                        AccountsModel model = new AccountsModel(title, username, password);
                        accountsModelList.add(model);

                    }
                    myAdapter = new AccountsRecyclerAdapter(getContext(), accountsModelList, new AccountsRecyclerAdapter.SelectedAccount() {
                        @Override
                        public void selectedAccount(AccountsModel model) {
                            Dialog myDialog = new Dialog(getContext());
                            myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            myDialog.setContentView(R.layout.view_account_dialog);
                            myDialog.setCancelable(false);
                            myDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.round_corners));
                            myDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            TextView title = myDialog.findViewById(R.id.titleTV);
                            title.setText(model.getTitle());
                            ImageView dismiss = myDialog.findViewById(R.id.dismiss);
                            dismiss.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    myDialog.dismiss();
                                }
                            });
                            TextView username = myDialog.findViewById(R.id.usernameTV);
                            TextView password = myDialog.findViewById(R.id.passwordTV);
                            username.setText(model.getUsername());
                            password.setText(model.getPassword());
                            ImageView copy_username = myDialog.findViewById(R.id.copy_username);
                            ImageView copy_password = myDialog.findViewById(R.id.copy_password);

                            copy_username.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("username", model.getUsername());
                                    clipboardManager.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "Copied!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            copy_password.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("password", model.getPassword());
                                    clipboardManager.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "Copied!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            myDialog.show();
                        }

                        @Override
                        public void selectedAccountLongClick(AccountsModel model) {

                        }
                    });
                    searchView.setVisibility(View.VISIBLE);
                    searchView.setMaxWidth(Integer.MAX_VALUE);
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            myAdapter.getFilter().filter(newText);
                            return true;
                        }
                    });
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
                    recyclerView.setAdapter(myAdapter);
                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                    itemTouchHelper.attachToRecyclerView(recyclerView);
                }else{
                    clear_screen.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.GONE);
                }
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                }else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/


        return view;
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAbsoluteAdapterPosition();
            AccountsModel model = accountsModelList.get(position);
            switch (direction){
                case ItemTouchHelper.LEFT:

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    AlertDialog dialog = builder.create();
                    dialog.setTitle("Delete data?");
                    dialog.setMessage(model.getTitle() + " account login credentials");
                    dialog.setCancelable(true);
                    dialog.setButton(Dialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (checkInternetConnection()) {
                                FirebaseDatabase db = FirebaseDatabase.getInstance();
                                DatabaseReference root = db.getReference("USERS")
                                        .child(SharedPrefManager.getInstance(getContext()).getUid())
                                        .child("accounts");
                                root.child(model.getTitle()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            dialog.dismiss();
                                            myAdapter.notifyDataSetChanged();
                                            Snackbar.make(recyclerView, "Deleted Successfully!", BaseTransientBottomBar.LENGTH_LONG)
                                                    .setAction("Undo", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            root.child(model.getTitle())
                                                                    .setValue(new AccountsModel(model.getUsername(), model.getPassword()))
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                myAdapter.notifyDataSetChanged();
                                                                                Snackbar.make(recyclerView, "Undo Successfully!", BaseTransientBottomBar.LENGTH_SHORT).show();
                                                                            } else {
                                                                                Snackbar.make(recyclerView, "Undo Failed!", BaseTransientBottomBar.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .setActionTextColor(Color.WHITE).show();
                                        } else {
                                            myAdapter.notifyDataSetChanged();
                                            Snackbar.make(recyclerView, "Failed to delete!", BaseTransientBottomBar.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                    dialog.setButton(Dialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            myAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    break;
                case ItemTouchHelper.RIGHT:

                    myAdapter.notifyDataSetChanged();
                    Dialog myDialog = new Dialog(getContext());
                    myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    myDialog.setContentView(R.layout.edit_account_dialog);
                    myDialog.setCancelable(false);
                    myDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.round_corners));
                    myDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView title = myDialog.findViewById(R.id.titleTV);
                    title.setText(model.getTitle());
                    ImageView dismiss = myDialog.findViewById(R.id.dismiss2);
                    dismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myDialog.dismiss();
                        }
                    });
                    TextInputEditText username = myDialog.findViewById(R.id.usernameET);
                    username.setText(model.getUsername());
                    username.setSelection(username.getText().length());
                    TextInputEditText password = myDialog.findViewById(R.id.passwordET);
                    password.setText(model.getPassword());
                    password.setSelection(password.getText().length());
                    Button update = myDialog.findViewById(R.id.update);
                    update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String uname = username.getText().toString();
                            String pass = password.getText().toString();
                            if (uname.length() == 0) {
                                username.setError("Enter username...");
                                username.requestFocus();
                            } else if (pass.length() <= 3) {
                                password.setError("Enter your password...");
                                password.requestFocus();
                            } else {
                                Dialog dialog_new = new Dialog(getContext());
                                try {
                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
                                    dialog_new.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog_new.setContentView(R.layout.loading_layout);
                                    dialog_new.setCancelable(false);
                                    dialog_new.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.round_shape_card));
                                    dialog_new.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    dialog_new.show();
                                }finally {
                                    if (checkInternetConnection()) {
                                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                                        DatabaseReference root = db.getReference("USERS")
                                                .child(SharedPrefManager.getInstance(getContext()).getUid())
                                                .child("accounts").child(model.getTitle());
                                        root.setValue(new AccountsModel(uname, pass))
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        dialog_new.dismiss();
                                                        if (task.isSuccessful()) {
                                                            myDialog.dismiss();
                                                            Snackbar.make(recyclerView, "Updated Successfully.", BaseTransientBottomBar.LENGTH_SHORT).show();
                                                        } else {
                                                            Snackbar.make(recyclerView, "Failed to Update!", BaseTransientBottomBar.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        dialog_new.dismiss();
                                    }
                                }
                            }
                        }
                    });
                    myDialog.show();
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(getContext(),c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorGreen))
                    .addSwipeRightActionIcon(R.drawable.ic_edit)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorRed))
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    private boolean hasNetworkAvailable() {
        connectivityManager= (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void setOnNetworkChangeListener() {
        if (hasNetworkAvailable()){
            isNetworkAvailable = true;
        }else {
            isNetworkAvailable = false;
        }
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onLost(Network network){
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
        view = LayoutInflater.from(getContext()).inflate(R.layout.network_dialog, null);
        TextView status = view.findViewById(R.id.status);
        Button actionBT = view.findViewById(R.id.actionBT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        view = LayoutInflater.from(getContext()).inflate(R.layout.network_dialog, null);
        TextView status = view.findViewById(R.id.status);
        Button actionBT = view.findViewById(R.id.actionBT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
