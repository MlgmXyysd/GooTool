package org.meowcat.gootool;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.zzhoujay.richtext.RichText;

import java.io.IOException;
import java.io.InputStream;

public class OpenSourceLicencesActivity extends AppCompatActivity {

    public Context mContext = this;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source_licences);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView mTextView = findViewById(R.id.textView3);
        String markdown = readAssetsTxt(mContext,"Licences.md");
        RichText.fromMarkdown(markdown).into(mTextView);
    }
    public static String readAssetsTxt(Context context, String fileName){
        try {
            //Return an AssetManager instance for your application's package
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // Convert the buffer into a string.
            String text = new String(buffer, "utf-8");
            // Finally stick the string into the text view.
            return text;
        } catch (IOException e) {
            // Should never happen!
            // throw new RuntimeException(e);
            e.printStackTrace();
        }
        return "File not found.";
    }
}
