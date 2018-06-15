/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package org.meowcat.gootool;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * TODO: About Activity
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View licencesView = findViewById(R.id.licencesView);
        licencesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent OSLIntent = new Intent(AboutActivity.this, OpenSourceLicencesActivity.class);
                startActivity(OSLIntent);
            }
        });
        View changesLogView = findViewById(R.id.changeslogView);
        changesLogView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("https://github.com/MeowCat-Studio/GooTool/blob/master/changes.log");
                intent.setData(content_url);
                startActivity(intent);
            }
        });
        View sourceCodeView = findViewById(R.id.sourceCodeView);
        sourceCodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setAction("android.intent.action.VIEW");
                Uri content_url1 = Uri.parse("https://github.com/MeowCat-Studio/GooTool/");
                intent1.setData(content_url1);
                startActivity(intent1);
            }
        });
        View authorView = findViewById(R.id.authorView);
        authorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setAction("android.intent.action.VIEW");
                Uri content_url1 = Uri.parse("http://www.meowcat.org/");
                intent1.setData(content_url1);
                startActivity(intent1);
            }
        });
    }
}
