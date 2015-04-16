package com.naohman.transsiberian.translation.dictionary;
import android.content.Context;

import com.naohman.transsiberian.setUp.App;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;

/**
 * Created by jeffrey on 1/10/15.
 * SQLiteAssetHelper allows me to ship a prepackaged, zipped db
 * int the assets folder
 */
public class DBHelper extends SQLiteAssetHelper {

    private static final int DB_VERSION = 7;
    private static final String DB_NAME = "translation.db";

    public static final String TABLE_ER = "er";
    public static final String TABLE_RE = "re";

    public static final String COL_KEYWORD = "keyword";
    public static final String COL_POSITION = "position";

    public static final String [] TRANSLATION_COLS = {COL_KEYWORD, COL_POSITION};

    public static final String TABLE_STATEMENTS = "statements";
        public static final String COL_ROOT = "root";
        public static final String COL_FLEXIA_ID = "flexiaId";
    public static final String[] STATEMENT_COLS =
            {COL_ROOT, COL_FLEXIA_ID};

    public static final String TABLE_FLEXIA = "flexia";
        //public static final String COL_FLEXIA_ID = "flexiaId";
        public static final String COL_FLEXIA_LIST = "flexiaList";
        public static final String COL_SPEC_LIST = "specifierList";
    public static final String[] FLEXIA_COLS =
        {COL_FLEXIA_ID, COL_FLEXIA_LIST, COL_SPEC_LIST};

    public static final String [] ROOT_COLS =
            {COL_ROOT, COL_FLEXIA_LIST};
    public static final String ROOT_SELECTOR = "SELECT " + COL_ROOT + " , "+
            COL_FLEXIA_LIST + " FROM " + TABLE_STATEMENTS + " JOIN " + TABLE_FLEXIA
            + " USING (" + COL_FLEXIA_ID + ") WHERE " + COL_ROOT + " IS null ";
    public static final String ROOT_OR_CLAUSE = " OR " + COL_ROOT + "=?";

    /**
     * Note this method may be very expensive if the database
     * does not already exist
     */
    public DBHelper() {
        super(App.context(), DB_NAME, null, DB_VERSION);
        setForcedUpgrade();
    }
}
