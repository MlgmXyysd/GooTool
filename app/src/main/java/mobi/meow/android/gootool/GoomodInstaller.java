/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package mobi.meow.android.gootool;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.util.ProgressListener;
import com.goofans.gootool.wog.ConfigurationWriterTask;
import com.goofans.gootool.wog.WorldOfGoo;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import mobi.meow.android.gootool.adapter.ModulesAdapter;

class GoomodInstaller {

    @SuppressLint("StaticFieldLeak")
    GoomodInstaller(final ModulesFragment mainActivity, final ProgressBar pb, final TextView text, final RecyclerView modsGrid) {
        new AsyncTask<Void, ProgressData, Void>() {
            private Configuration cfg;

            @Override
            protected void onPreExecute() {
                mainActivity.disableButtons();
                pb.setVisibility(View.VISIBLE);
                text.setVisibility(View.VISIBLE);

                try {
                    cfg = WorldOfGoo.getTheInstance().readConfiguration();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                ModulesAdapter a = (ModulesAdapter) modsGrid.getAdapter();

                List<String> enabled = cfg.getEnabledAddins();
                enabled.clear();

                for (int i = 0; i < Objects.requireNonNull(a).getCount(); i++) {
                    ModulesAdapter.GoomodEntry entry = (ModulesAdapter.GoomodEntry) a.getItem(i);
                    if (entry.isEnabled())
                        enabled.add(entry.getId());
                }

                cfg.setWatermark("GooTool for Android by NekoYuzu. https://www.neko.ink/");
            }

            @Override
            protected void onPostExecute(Void nothing) {
                mainActivity.enableButtons();
                pb.setVisibility(View.INVISIBLE);
                text.setText("");
            }

            @Override
            protected Void doInBackground(Void... params) {
                ConfigurationWriterTask cwt = new ConfigurationWriterTask(cfg);
                cwt.addListener(new ProgressListener() {
                    String task = "";

                    @Override
                    public void beginStep(String taskDescription, boolean progressAvailable) {
                        publishProgress(new ProgressData(task = taskDescription, progressAvailable ? 0 : 0.5f));
                    }

                    @Override
                    public void progressStep(float percent) {
                        publishProgress(new ProgressData(task, percent));
                    }
                });
                try {
                    cwt.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(ProgressData... i) {
                ProgressData pd = i[i.length - 1];
                pb.setProgress((int) (pd.progress * 100));
                text.setText(pd.name);
            }
        }.execute();
    }
}
