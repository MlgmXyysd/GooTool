package mobi.meow.android.gootool;

import static mobi.meow.android.gootool.MeowCatApplication.TAG;

import android.util.Log;

import java.util.logging.Level;

public class Logger {

    public static Logger getLogger(String name) {
        return new Logger();
    }

    public void severe(String msg) {
        Log.e(TAG, msg);
    }

    public void warning(String msg) {
        Log.w(TAG, msg);
    }

    public void info(String msg) {
        Log.i(TAG, msg);
    }

    public void config(String msg) {
        Log.d(TAG, msg);
    }

    public void fine(String msg) {
        Log.v(TAG, msg);
    }

    public void finer(String msg) {
        Log.v(TAG, msg);
    }

    public void finest(String msg) {
        Log.v(TAG, msg);
    }

    public void log(Level finer, String msg) {
        Log.d(TAG, msg);
    }

    public void log(Level finer, String msg, Exception e) {
        Log.e(TAG, msg);
    }

    public void setLevel(Level all) {
    }

    public Logger[] getHandlers() {
        return new Logger[]{getLogger("")};
    }
}
