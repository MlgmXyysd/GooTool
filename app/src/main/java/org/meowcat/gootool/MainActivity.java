/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package org.meowcat.gootool;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.util.ProgressListener;
import com.goofans.gootool.wog.WorldOfGoo;

import org.askerov.dynamicgrid.DynamicGridView;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

  public static final String TAG = "Gootool";
  private static final int FILE_SELECT_CODE = 0;

  public Button installApkBtn;
  public Button installModsBtn;
  public Button changeOrder;

  private Button addBtn, rmBtn;

  private ProgressBar pb;

  private DynamicGridView modsGrid;
  private ModListDynamicGridViewAdapter modListAdapter;

  private TextView text;

  private CountDownTimer timer = new CountDownTimer(5000, 10000) {
    @Override
    public void onTick(long millisUntilFinished) {}

    @Override
    public void onFinish() {
      modsGrid.stopEditMode();
      changeOrder.setEnabled(true);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    this.modsGrid = (DynamicGridView) findViewById(R.id.modsGrid);
    this.modsGrid.setAdapter(modListAdapter = new ModListDynamicGridViewAdapter(this, this.modsGrid));
    this.modsGrid.setEditModeEnabled(true);
    this.modsGrid.setWobbleInEditMode(false);
    this.modsGrid.setOnDragListener(new DynamicGridView.OnDragListener() {
      @Override
      public void onDragStarted(int position) {
        timer.cancel();
      }

      @Override
      public void onDragPositionsChanged(int oldPosition, int newPosition) {
      }
    });
    this.modsGrid.setOnDropListener(new DynamicGridView.OnDropListener() {
      @Override
      public void onActionDrop() {
        timer.start();
      }
    });

    this.pb = (ProgressBar) findViewById(R.id.installProgress);
    this.pb.setInterpolator(new LinearInterpolator());

    this.text = (TextView) findViewById(R.id.textView);

    this.installModsBtn = (Button) findViewById(R.id.installModsBtn);
    this.installModsBtn.setOnClickListener(new GoomodInstaller(this, pb, text, modsGrid));

    this.installApkBtn = (Button) findViewById(R.id.installApkBtn);
    this.installApkBtn.setOnClickListener(new ApkInstaller(this, pb, text));

    this.addBtn = (Button) findViewById(R.id.addBtn);
    this.addBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
          startActivityForResult(
                  Intent.createChooser(intent, getString(R.string.add)),
                  FILE_SELECT_CODE);//
        } catch (ActivityNotFoundException ex) {
          Toast.makeText(MainActivity.this, R.string.managernotfound, Toast.LENGTH_SHORT).show();
        }
      }
    });

    this.rmBtn = (Button) findViewById(R.id.rmBtn);
    this.rmBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ModListDynamicGridViewAdapter adapter = (ModListDynamicGridViewAdapter)modsGrid.getAdapter();
        if(adapter.isRemoveMode()) {
          adapter.onEndRemoveMode();
          ((Button)v).setText(R.string.btn_remove);
          Toast.makeText(MainActivity.this, R.string.deleted, Toast.LENGTH_LONG).show();
          enableButtons();
        } else {
          adapter.startRemoveMode();
          ((Button)v).setText(R.string.btn_delete);
          Toast.makeText(MainActivity.this, R.string.delete_select, Toast.LENGTH_LONG).show();
          disableButtons();
          v.setEnabled(true);
        }
      }
    });

    this.changeOrder = (Button) findViewById(R.id.changeOrderButton);
    this.changeOrder.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        modsGrid.startEditMode();
        timer.start();
        changeOrder.setEnabled(false);
      }
    });
    new InitGootoolTask().execute();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_about) {
      //TODO:About activity
      //Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
      //startActivity(aboutIntent);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case FILE_SELECT_CODE:
        if (resultCode == RESULT_OK) {
          // Get the Uri of the selected file
          Uri uri = data.getData();
          Log.d(TAG, "File Uri: " + uri.toString());

          File file = IOUtils.getFile(this, uri);

          if(file == null) {
            Toast.makeText(this, R.string.cantopen, Toast.LENGTH_SHORT);
            return;
          }

          new AddAddinAsyncTask(file, (ModListDynamicGridViewAdapter) this.modsGrid.getAdapter(), this).execute((Void[])null);
        }
        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }


  public void disableButtons() {
    setButtonsEnabled(false);
  }

  public void enableButtons() {
    setButtonsEnabled(true);
  }

  private void setButtonsEnabled(boolean value) {
    installModsBtn.setEnabled(value);
    installApkBtn.setEnabled(value);
    rmBtn.setEnabled(value);
    addBtn.setEnabled(value);
    changeOrder.setEnabled(value);
  }

  private class AddAddinAsyncTask extends AsyncTask<Void, Void, String>{
    private File file;
    private ModListDynamicGridViewAdapter adapter;
    private Context context;

    public AddAddinAsyncTask(File file, ModListDynamicGridViewAdapter adapter, Context context) {

      this.file = file;
      this.adapter = adapter;
      this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
      try {
        Addin a = AddinFactory.loadAddin(file);
        WorldOfGoo.getTheInstance().installAddin(file, a.getId(), false);
      } catch (AddinFormatException e) {
        Log.e(TAG, "Addin error", e);
        return getString(R.string.invaild);
      } catch (IOException e) {
        Log.e(TAG, "IO error", e);
        return getString(R.string.readerror);
      } catch(DuplicateAddinException ex) {
        Log.e(TAG, "Duplicate addin", ex);
        return getString(R.string.added);
      }
      return null;
    }

    @Override
    protected void onPostExecute(String result) {
      if(result != null) {
        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        return;
      }
      Set<String> alreadyInstalled = new HashSet<String>();
      for(int i = 0; i < adapter.getCount(); i++) {
        alreadyInstalled.add(((ModListDynamicGridViewAdapter.GoomodEntry) adapter.getItem(i)).getId());
      }
      for(Addin addin : WorldOfGoo.getAvailableAddins()) {
        if(!alreadyInstalled.contains(addin.getId())) {
          adapter.add(new ModListDynamicGridViewAdapter.GoomodEntry(addin, false));
        }
      }
      Toast.makeText(context, R.string.goomodadded, Toast.LENGTH_SHORT).show();
    }
  }

  private class InitGootoolTask extends AsyncTask<Void, ProgressData, Void> {

    @Override
    protected Void doInBackground(Void... params) {
      WorldOfGoo.getTheInstance().init();
      return null;
    }

    @Override
    protected void onPostExecute(Void nothing) {
      enableButtons();

      pb.setVisibility(View.INVISIBLE);
      pb.setProgress(0);
      text.setText("");

      //TODO: DO IT IN BACKGROUND
      try {
        WorldOfGoo wog = WorldOfGoo.getTheInstance();

        wog.updateInstalledAddins();
        Configuration cfg = WorldOfGoo.getTheInstance().readConfiguration();

        for(Addin addin : WorldOfGoo.getAvailableAddins()) {
          boolean enabled = cfg.isEnabledAdddin(addin.getId());

          modListAdapter.add(new ModListDynamicGridViewAdapter.GoomodEntry(addin, enabled));
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    protected void onPreExecute() {
      pb.setVisibility(View.VISIBLE);
      WoGInitData.setPackageManager(getPackageManager());
      WoGInitData.setContext(getApplicationContext());

      WoGInitData.setProgressListener(new ProgressListener() {
        private String stepName;

        @Override
        public void beginStep(String taskDescription, boolean progressAvailable) {
          stepName = taskDescription;
          if(!progressAvailable) {
            progressStep(0.5f);
          } else {
            progressStep(0);
          }
        }

        @Override
        public void progressStep(float percent) {
          publishProgress(new ProgressData(stepName, percent));
        }
      });
    }

    @Override
    protected void onProgressUpdate(ProgressData... i) {
      ProgressData pd = i[i.length - 1];
      pb.setProgress((int) (pd.progress * 100));
      text.setText(pd.name);

    }
  }
}