package com.naohman.transsiberian.setUp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Copied from zeetoobiker on SO, a singleton that holds an application context
 * so you don't have to pass it everywhere.
 */
public class App extends android.app.Application {
    private static App mApp = null;
    /* (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }

    /**
     * called by non-activities that need an app context to load files/colors etc.
     * @return the default application context
     */
    public static Context context() {
        return mApp.getApplicationContext();
    }

    public static AssetFileDescriptor openAsset(String file) throws IOException {
        AssetManager aManager = mApp.getAssets();
        return aManager.openFd(file);
    }

    public static int getColor(int id){
        return context().getResources().getColor(id);
    }
}
