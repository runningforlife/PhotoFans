package com.github.runningforlife.photosniffer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.github.runningforlife.photosniffer.R;

/**
 * usage guide to use app
 */

public class UsageGuideActivity extends BaseActivity {

    private static final String HELP_HTML_URL = "file:///android_asset/html/user_guide.html";

    @Override
    public void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        setContentView(R.layout.activity_usage_guide);

        initWebView();
    }

    private void initWebView() {
        WebView helpHtmlView = findViewById(R.id.wv_help_html);
        WebSettings settings = helpHtmlView.getSettings();
        settings.setDisplayZoomControls(true);

        helpHtmlView.loadUrl(HELP_HTML_URL);
        helpHtmlView.requestFocus();
    }

}
