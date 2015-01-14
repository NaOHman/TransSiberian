package com.naohman.language.transsiberian;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by jeffrey on 1/10/15.
 * SQLiteAssetHelper allows me to ship a prepackaged, zipped db
 * int the assets folder
 */
public class DBHelper extends SQLiteAssetHelper {

    private static final String DB_NAME = "translation.db";
    public static final String TABLE_DICT = "dict";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, null, DB_VERSION);
    }
}
