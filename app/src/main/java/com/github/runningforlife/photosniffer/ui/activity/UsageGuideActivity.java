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

public class UsageGuideActivity extends AppCompatActivity {

    private static final String HELP_HTML_URL = "file:///android_asset/html/user_guide.html";

    @Override
    public void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        setContentView(R.layout.activity_usage_guide);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }

        initWebView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if(id == android.R.id.home){
            Intent intent = new Intent(this, GalleryActivity.class);
            NavUtils.navigateUpTo(this,intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void initWebView() {
        WebView helpHtmlView = findViewById(R.id.wv_help_html);
        WebSettings settings = helpHtmlView.getSettings();
        settings.setDisplayZoomControls(true);

        helpHtmlView.loadUrl(HELP_HTML_URL);
        helpHtmlView.requestFocus();
    }

}