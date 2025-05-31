package com.termux.app.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.termux.shared.termux.TermuxConstants;
import com.termux.R;

/**
 * Basic embedded browser for viewing help pages.
 */
public final class HelpActivity extends AppCompatActivity {

    WebView mWebView;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setStatusBarColor(0xff010409);

    MaterialToolbar toolbar = new MaterialToolbar(this);
    toolbar.setTitle("Juliocj7");
    toolbar.setSubtitle("0xsimplythebest");
    toolbar.setTitleTextAppearance(this, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
    toolbar.setSubtitleTextAppearance(this, com.google.android.material.R.style.TextAppearance_Material3_BodySmall);
    toolbar.setTitleTextColor(Color.parseColor("#bababa"));
    toolbar.setSubtitleTextColor(Color.parseColor("#404040"));
    toolbar.setBackgroundColor(Color.parseColor("#010409"));
    toolbar.setNavigationIcon(R.drawable.ic_shield_sword);
    toolbar.setNavigationIconTint(Color.parseColor("#505050"));
    toolbar.setNavigationOnClickListener(v -> finish());

    ProgressBar progressBar = new ProgressBar(this);
    RelativeLayout.LayoutParams pBarParams = new RelativeLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    );
    pBarParams.addRule(RelativeLayout.CENTER_IN_PARENT);
    RelativeLayout progressLayout = new RelativeLayout(this);
    progressLayout.addView(progressBar, pBarParams);

    mWebView = new WebView(this);
    WebSettings settings = mWebView.getSettings();
    settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    mWebView.clearCache(true);

    FrameLayout contentContainer = new FrameLayout(this);
    contentContainer.addView(progressLayout);
    contentContainer.addView(mWebView);
    mWebView.setVisibility(View.GONE); // Oculta WebView al inicio

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.addView(toolbar, new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ));
    root.addView(contentContainer, new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        0, 1f
    ));

    setContentView(root);

    mWebView.setWebViewClient(new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            progressLayout.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            //if (url.toString().startsWith(TermuxConstants.TERMUX_WIKI_URL)) {
            if (url.toString().equals(TermuxConstants.TERMUX_WIKI_URL) || url.toString().startsWith(TermuxConstants.TERMUX_WIKI_URL + "/")) {
                mWebView.loadUrl(url.toString());
                return true;
            }
            try {
                //startActivity(new Intent(Intent.ACTION_VIEW, url));
                startActivity(new Intent(Intent.ACTION_VIEW, url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            } catch (ActivityNotFoundException e) {
                // Android TV does not have a system browser.
                return false;
            }
        }
    });

    mWebView.loadUrl(TermuxConstants.TERMUX_WIKI_URL);
}

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
