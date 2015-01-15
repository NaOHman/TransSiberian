package com.naohman.language.transsiberian;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;

/**
 * Created by jeffrey on 1/10/15.
 * SQLiteAssetHelper allows me to ship a prepackaged, zipped db
 * int the assets folder
 */
public class DBHelper extends SQLiteAssetHelper {

    private static final String DB_NAME = "translation.db";
    public static final String TABLE_RE = "re";
    public static final String TABLE_ER = "er";
    private static final int DB_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DB_NAME,getDir(context), null, DB_VERSION);
        setForcedUpgrade();
    }

    private static String getDir(Context c){
        File dir = c.getExternalFilesDir(null);
        if (dir == null)
            return null;
        else
            return dir.getAbsolutePath();
    }
}
