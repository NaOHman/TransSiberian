package com.naohman.transsiberian.Translation.Util;
import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;

/**
 * Created by jeffrey on 1/10/15.
 * SQLiteAssetHelper allows me to ship a prepackaged, zipped db
 * int the assets folder
 */
public class DictionaryDBHelper extends SQLiteAssetHelper {

    private static final String DB_NAME = "translation.db";
    public static final String TABLE_RE = "re";
    public static final String TABLE_ER = "er";
    private static final int DB_VERSION = 2;

    /**
     * Note this method may be very expensive if the database
     * does not already exist
     * @param context the application context
     */
    public DictionaryDBHelper(Context context) {
        super(context, DB_NAME,getDir(context), null, DB_VERSION);
        setForcedUpgrade();
    }

    //Todo make this more robust perhaps allow the use of preferences
    private static String getDir(Context c){
        File dir = c.getExternalFilesDir(null);
        if (dir == null)
            return null;
        else
            return dir.getAbsolutePath();
    }
}
