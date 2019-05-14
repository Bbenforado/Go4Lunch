package com.example.blanche.go4lunch.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import com.example.blanche.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebviewActivity extends AppCompatActivity {

    @BindView(R.id.webview)
    WebView webView;
    Bundle bundle;
    public static final String RESTAURANT_WEBSITE_URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);
        bundle = getIntent().getExtras();
        String url = bundle.getString(RESTAURANT_WEBSITE_URL);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        configureToolbar();
        /*String finalString = null;
        *//*if (url != null && url.startsWith("http://")) {
            String newUrl = url.substring(4);
            finalString = "https" + newUrl;
            System.out.println("new url = " + finalString);

        }*/
        webView.loadUrl(url);
    }

    private void configureToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
