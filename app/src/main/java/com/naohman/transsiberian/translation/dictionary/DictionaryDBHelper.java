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
public class DictionaryDBHelper extends SQLiteAssetHelper {

    private static final int DB_VERSION = 3;
    private static final String DB_NAME = "translation.db";

    public static final String TABLE_RE = "re";
        public static final String COL_KEYWORD = "keyword";
        public static final String COL_DEFINITION = "definition";
    public static final String [] TRANSLATION_COLS =
            {COL_KEYWORD, COL_DEFINITION}

    public static final String TABLE_ER = "er";
        //public static final String COL_KEYWORD = "keyword";
        //public static final String COL_DEFINITION = "definition";
    //public static final String [] TRANSLATION_COLS =
            //{COL_KEYWORD, COL_DEFINITION}


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

    public static final String ROOT_SELECTOR = "SELECT "+COL_FLEXIA_LIST+ " FROM " +
            TABLE_STATEMENTS + " JOIN " + TABLE_FLEXIA + " USING (" + COL_FLEXIA_ID +
            ") WHERE " + COL_ROOT + " = ?";

    /**
     * Note this method may be very expensive if the database
     * does not already exist
     */
    public DictionaryDBHelper() {
        super(App.context(), DB_NAME,getDir(App.context()), null, DB_VERSION);
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
