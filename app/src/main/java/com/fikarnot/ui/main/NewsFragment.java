package com.fikarnot.ui.main;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fikarnot.MainActivity;
import com.fikarnot.R;

import java.net.URL;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {


    final String url_1 = "https://zeenews.india.com/rss/technology-news.xml";
    private List<NewsModel> newsModelList;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View view;
    private LinearLayout loading1, clear_screen1;


    //web-view
    ProgressBar progressbar;
    WebView webview;
    ImageView stop;
    TextView progress_textview;
    TextView web_result;
    String state;
    Dialog webDialog;
    boolean loading = false;
    int count_back = 0;

    //web-view


    public NewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_news, container, false);
        loading1 = view.findViewById(R.id.loading1);
        recyclerView = view.findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        clear_screen1 = view.findViewById(R.id.clear_screen1);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        new BackgroundTask().execute();
        return view;
    }

    public void pressed() {
        new BackgroundTask().execute();
    }


    class BackgroundTask extends AsyncTask<String, Integer, Boolean> {
        boolean internet = false;

        @Override
        protected void onPreExecute() {
            recyclerView.setVisibility(View.GONE);
            loading1.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (checkInternetConnection()) {
                internet = true;
                HandleXML handleXML = new HandleXML(url_1);
                if (handleXML.fetchXML()) {
                    newsModelList = handleXML.getNews();
                    return true;
                } else {
                    return false;
                }
            } else {
                internet = false;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            swipeRefreshLayout.setRefreshing(false);
            if (result && newsModelList.size() >= 1) {
                @SuppressLint("SetJavaScriptEnabled") NewsAdapter adapter = new NewsAdapter(newsModelList, news_link -> {

                    webDialog = new Dialog(view.getContext());
                    webDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    webDialog.setContentView(R.layout.view_news_dialog);
                    webDialog.setCancelable(true);
                    Objects.requireNonNull(webDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    progressbar = webDialog.findViewById(R.id.progressbar);
                    progress_textview = webDialog.findViewById(R.id.progress);
                    web_result = webDialog.findViewById(R.id.result);
                    webview = webDialog.findViewById(R.id.web_view);
                    stop = webDialog.findViewById(R.id.stop);
                    stop.setOnClickListener(view -> webDialog.dismiss());
                    progressbar.setMax(100);
                    webview.loadUrl(news_link);
                    webview.setWebViewClient(new MyWebViewClient());
                    WebSettings webSettings = webview.getSettings();
                    webview.setWebChromeClient(new MyWebChromeClient());
                    webSettings.setJavaScriptEnabled(true);
                    webDialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {


                        if (i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP && !keyEvent.isCanceled()) {
                            if (count_back == 0) {
                                count_back = 1;
                                if (loading) {
                                    webview.stopLoading();
                                } else {
                                    if (webview.canGoBack()) {
                                        webview.goBack();
                                    }
                                }
                                new Handler().postDelayed(() -> count_back = 0, 1000);
                            } else {
                                webDialog.dismiss();
                                count_back = 0;
                            }
                            return true;
                        }

                        return false;
                    });
                    Toast.makeText(getContext(), "Double press Back to exit", Toast.LENGTH_SHORT).show();
                    webDialog.show();

                });
                clear_screen1.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(adapter);
            } else {
                if (!internet) {
                    Toast.makeText(getContext(), "No internet!!", Toast.LENGTH_SHORT).show();
                }
                recyclerView.setVisibility(View.GONE);
                clear_screen1.setVisibility(View.VISIBLE);
            }
            swipeRefreshLayout.setOnRefreshListener(() -> new BackgroundTask().execute());
            loading1.setVisibility(View.GONE);
        }


        public class MyWebViewClient extends WebViewClient {

            @Override
            public void onPageStarted(final WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                web_result.setVisibility(View.GONE);
                progressbar.setVisibility(View.VISIBLE);
                stop.setOnClickListener(v -> webDialog.dismiss());


            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                view.loadUrl(url);
                return true;

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                stop.setOnClickListener(view1 -> webDialog.dismiss());
            }
        }


        class MyWebChromeClient extends WebChromeClient {
            public void onProgressChanged(WebView view, int progress) {
                if (progress <= 100 ){
                    loading = true;
                    web_result.setVisibility(View.GONE);
                    progressbar.setVisibility(View.VISIBLE);
                    progressbar.setProgress(progress);
                    String percent = progress + "%";
                    progress_textview.setText(percent);
                    if (progress == 100){
                        new Handler().postDelayed(() -> {
                            loading = false;
                            progress_textview.setText("");
                            progressbar.setVisibility(View.GONE);
                            state = "Done";
                            web_result.setText(state);
                            web_result.setVisibility(View.VISIBLE);
                        }, 300);
                    }
                }

            }
        }

    }


    private boolean checkInternetConnection() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            HttpsURLConnection url_con = (HttpsURLConnection) new URL("https://clients3.google.com/generate_204").openConnection();
            url_con.setRequestProperty("User-Agent","Android");
            url_con.setRequestProperty("Connection","close");
            url_con.connect();
            return url_con.getResponseCode() == 204 && url_con.getContentLength() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Interface SelectedProduct STARTS
    public interface SelectedNews {
        void selectedNews(String news_link);
    }
    //Interface SelectedProduct ENDS

}