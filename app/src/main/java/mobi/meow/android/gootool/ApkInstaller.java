/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package mobi.meow.android.gootool;

import static mobi.meow.android.gootool.MeowCatApplication.TAG;
import static mobi.meow.android.gootool.MeowCatApplication.context;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.goofans.gootool.wog.WorldOfGooAndroid;

import java.io.File;
import java.io.IOException;

import kellinwood.security.zipsigner.ZipSigner;

class ApkInstaller {

    @SuppressLint("StaticFieldLeak")
    ApkInstaller(ModulesFragment a, ProgressBar progress, TextView text) {
        a.disableButtons();
        new InstallModsTask(progress, a, text).execute();
    }

    private static final class InstallModsTask extends AsyncTask<Void, ProgressData, Boolean> {

        @SuppressLint("StaticFieldLeak")
        private final ProgressBar progress;
        @SuppressLint("StaticFieldLeak")
        private final TextView text;
        @SuppressLint("StaticFieldLeak")
        private final ModulesFragment a;

        private int taskNum = -1;

        InstallModsTask(ProgressBar progress, ModulesFragment act, TextView text) {
            this.progress = progress;
            this.text = text;
            this.a = act;
        }

        @Override
        protected void onPreExecute() {
            this.progress.setVisibility(View.VISIBLE);
            text.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            this.progress.setVisibility(View.INVISIBLE);
            text.setText("");
            a.enableButtons();
            if (!b) {
                return;
            }

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                if (!context.getPackageManager().canRequestPackageInstalls()) {
//                    context.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + context.getPackageName())));
//                }
//            }

            File file = new File(WorldOfGooAndroid.get().DATA_DIR, "WorldOfGoo.apk");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = FileProvider.getUriForFile(a.requireContext(), a.requireContext().getPackageName() + ".apkinstaller.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(data, "application/vnd.android.package-archive");
            a.startActivity(intent);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        protected Boolean doInBackground(Void... nothing) {
            taskNum++;
            setTaskProgress(context.getString(R.string.generating), 50);
            File srcDir = WorldOfGooAndroid.get().TEMP_MODDED_DIR;
            File zipFile = new File(WorldOfGooAndroid.get().DATA_DIR, "modded.apk");
            if (!this.putIntoApk(srcDir, zipFile)) return false;

            taskNum++;
            setTaskProgress(context.getString(R.string.signing), 50);
            File signed = new File(WorldOfGooAndroid.get().DATA_DIR, "WorldOfGoo.apk");
            this.signApk(zipFile, signed);
            zipFile.delete();
            return true;
        }

        private boolean putIntoApk(File dir, File apkLoc) {
            try {
                IOUtils.zipDirContentWithZipBase(WorldOfGooAndroid.get().WOG_APK_FILE, dir, apkLoc);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private void signApk(File apk, File signed) {
            try {
                ZipSigner signer = new ZipSigner();
                signer.addProgressListener(event -> setTaskProgress(context.getString(R.string.signing), event.getPercentDone() / 100.0d));
                signer.setKeymode(ZipSigner.MODE_AUTO);
                signer.loadKeys(ZipSigner.KEY_TESTKEY);
                signer.signZip(apk.getPath(), signed.getPath());
            } catch (Exception e) {
                Log.wtf(TAG, e);
            }
        }

        @Override
        protected void onProgressUpdate(ProgressData... i) {
            ProgressData pd = i[i.length - 1];
            int maxTask = 2;
            this.progress.setProgress((int) ((pd.progress + taskNum) * 100 / maxTask));
            text.setText(pd.name);
        }

        private void setTaskProgress(String name, double p) {
            this.publishProgress(new ProgressData(name, p));
        }
    }
}
