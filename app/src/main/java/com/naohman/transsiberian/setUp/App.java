package com.naohman.transsiberian.setUp;

import android.content.Context;

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
}
