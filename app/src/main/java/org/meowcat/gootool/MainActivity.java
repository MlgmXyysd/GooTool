/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package org.meowcat.gootool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_PERMISSION_REQUEST_CODE = 10000;
    public static final String TAG = "GooTool";
    private static final int FILE_SELECT_CODE = 0;
    public Boolean ischangeorder = false;
    public Boolean isInited = false;
    public Button installApkBtn;
    public Button installModsBtn;
    public Button changeOrder;

    private Button addBtn, rmBtn;
    private long exitTime = 0;
    private ProgressBar pb;

    private DynamicGridView modsGrid;
    private ModListDynamicGridViewAdapter modListAdapter;

    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.modsGrid = findViewById(R.id.modsGrid);
        this.modsGrid.setAdapter(modListAdapter = new ModListDynamicGridViewAdapter(this, this.modsGrid));
        this.modsGrid.setEditModeEnabled(true);
        this.modsGrid.setWobbleInEditMode(false);
        this.modsGrid.setOnDragListener(new DynamicGridView.OnDragListener() {
            @Override
            public void onDragStarted(int position) {
            }

            @Override
            public void onDragPositionsChanged(int oldPosition, int newPosition) {
            }
        });

        this.pb = findViewById(R.id.installProgress);
        this.pb.setInterpolator(new LinearInterpolator());

        this.text = findViewById(R.id.textView);

        //this.installModsBtn = findViewById(R.id.installModsBtn);
        //this.installModsBtn.setOnClickListener(new GoomodInstaller(this, pb, text, modsGrid));

        //this.installApkBtn = findViewById(R.id.installApkBtn);
        //this.installApkBtn.setOnClickListener(new ApkInstaller(this, pb, text));

        this.addBtn = findViewById(R.id.addBtn);
        this.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(
                            Intent.createChooser(intent, getString(R.string.add)),
                            FILE_SELECT_CODE);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, R.string.managernotfound, Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.rmBtn = findViewById(R.id.rmBtn);
        this.rmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModListDynamicGridViewAdapter adapter = (ModListDynamicGridViewAdapter) modsGrid.getAdapter();
                if (adapter.isRemoveMode()) {
                    adapter.onEndRemoveMode();
                    ((Button) v).setText(R.string.btn_remove);
                    Toast.makeText(MainActivity.this, R.string.deleted, Toast.LENGTH_LONG).show();
                    enableButtons();
                } else {
                    adapter.startRemoveMode();
                    ((Button) v).setText(R.string.btn_delete);
                    Toast.makeText(MainActivity.this, R.string.delete_select, Toast.LENGTH_LONG).show();
                    disableButtons();
                    v.setEnabled(true);
                }
            }
        });
        this.changeOrder = findViewById(R.id.changeOrderButton);
        this.changeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ischangeorder) {
                    ((Button) v).setText(R.string.btn_changeorder);
                    modsGrid.stopEditMode();
                    enableButtons();
                    ischangeorder = false;
                } else {
                    ((Button) v).setText(R.string.btn_done);
                    modsGrid.startEditMode();
                    disableButtons();
                    v.setEnabled(true);
                    ischangeorder = true;
                }
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        if (Build.VERSION.SDK_INT >= 23) {
            boolean isAllGranted = checkPermissionAllGranted(
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }
            );
            if (isAllGranted) {
                new InitGootoolTask().execute();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.grantpermissions_1);
                builder.setPositiveButton(R.string.grantpermissions_btn_2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission();
                    }
                });
                builder.setNegativeButton(R.string.grantpermissions_btn_1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        askTwice();
                    }
                });
                builder.show();
            }
        } else {
            new InitGootoolTask().execute();
        }
    }

    public void askTwice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.grantpermissions_2);
        builder.setPositiveButton(R.string.grantpermissions_btn_2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermission();
            }
        });
        builder.setNegativeButton(R.string.grantpermissions_btn_1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    public void requestPermission() {
        boolean isAllGranted = checkPermissionAllGranted(
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
        );
        if (isAllGranted) {
            new InitGootoolTask().execute();
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                MY_PERMISSION_REQUEST_CODE
        );
    }

    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                new InitGootoolTask().execute();

            } else {
                openAppDetails();
            }
        }
    }

    private void openAppDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.grantpermissions_3);
        builder.setPositiveButton(R.string.grantpermissions_btn_2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.grantpermissions_btn_1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), R.string.exit, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_mod) {
            //TODO:Something code.
        } else if (id == R.id.nav_settings) {
            //Intent SettingIntent = new Intent(MainActivity.this, OpenSourceLicencesActivity.class);
            //startActivity(SettingIntent);
        } else if (id == R.id.nav_donate) {
            //Intent DonateIntent = new Intent(MainActivity.this, OpenSourceLicencesActivity.class);
            //startActivity(DonateIntent);
        } else if (id == R.id.nav_about) {
            Intent AboutIntent = new Intent(MainActivity.this, OpenSourceLicencesActivity.class);
            startActivity(AboutIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if (isInited){
                new GoomodInstaller(this, pb, text, modsGrid);
            }
        } else if (id == R.id.action_install) {
            if (isInited){
                new ApkInstaller(this, pb, text);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    assert uri != null;
                    Log.d(TAG, "File Uri: " + uri.toString());

                    File file = IOUtils.getFile(this, uri);

                    if (file == null) {
                        Toast.makeText(this, R.string.cantopen, Toast.LENGTH_SHORT);
                        return;
                    }

                    new AddAddinAsyncTask(file, (ModListDynamicGridViewAdapter) this.modsGrid.getAdapter(), this).execute((Void[]) null);
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

    @SuppressLint("StaticFieldLeak")
    private class AddAddinAsyncTask extends AsyncTask<Void, Void, String> {
        private File file;
        private ModListDynamicGridViewAdapter adapter;
        private Context context;

        AddAddinAsyncTask(File file, ModListDynamicGridViewAdapter adapter, Context context) {

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
            } catch (DuplicateAddinException ex) {
                Log.e(TAG, "Duplicate addin", ex);
                return getString(R.string.added);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                return;
            }
            Set<String> alreadyInstalled = new HashSet<>();
            for (int i = 0; i < adapter.getCount(); i++) {
                alreadyInstalled.add(((ModListDynamicGridViewAdapter.GoomodEntry) adapter.getItem(i)).getId());
            }
            for (Addin addin : WorldOfGoo.getAvailableAddins()) {
                if (!alreadyInstalled.contains(addin.getId())) {
                    adapter.add(new ModListDynamicGridViewAdapter.GoomodEntry(addin, false));
                }
            }
            Toast.makeText(context, R.string.goomodadded, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class InitGootoolTask extends AsyncTask<Void, ProgressData, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            WorldOfGoo.getTheInstance().init();
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            enableButtons();
            isInited = true;
            pb.setVisibility(View.INVISIBLE);
            pb.setProgress(0);
            text.setText("");

            try {
                WorldOfGoo wog = WorldOfGoo.getTheInstance();

                wog.updateInstalledAddins();
                Configuration cfg = WorldOfGoo.getTheInstance().readConfiguration();

                for (Addin addin : WorldOfGoo.getAvailableAddins()) {
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
                    if (!progressAvailable) {
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