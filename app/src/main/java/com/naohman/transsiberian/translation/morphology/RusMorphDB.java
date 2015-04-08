package com.naohman.transsiberian.translation.morphology;

import android.content.Context;
import com.naohman.transsiberian.setUp.App;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import java.io.File;

/**
 * Created by jeffrey on 3/16/15.
 * SQLiteAssetHelper allows me to ship a prepackaged, zipped db
 * int the assets folder
 */
public class RusMorphDB extends SQLiteAssetHelper {

    private static final String DB_NAME = "rusmorph.db";
    public static final String TABLE_STATEMENTS = "statements";
    public static final String TABLE_SPECIFIERS = "specifiers";
    public static final String TABLE_TRANSFORMERS = "transformers";
    public static final String TABLE_ATTRIBUTES = "attributes";

    public static final String COL_ROOT = "root";
    public static final String COL_TRANS_ID = "trans_id";
    public static final String COL_SPEC_ID = "spec_id";
    public static final String COL_ATTR_IDS = "attr_ids";
    public static final String COL_ATTR_ID = "attr_id";
    public static final String COL_NAME = "name";
    public static final String COL_TYPE = "type";
    public static final String COL_FLEXIA = "flexia";
    public static final String COL_ABBR = "abbr";

    public static final String[] STATEMENT_COLS = {COL_ROOT, COL_TRANS_ID};
    public static final String[] SPECIFIER_COLS = {COL_SPEC_ID, COL_ATTR_IDS, COL_NAME};
    public static final String[] TRANSFORMER_COLS = {COL_TRANS_ID, COL_TYPE, COL_SPEC_ID, COL_FLEXIA};
    public static final String[] ATTRIBUTE_COLS = {COL_ATTR_ID, COL_ABBR, COL_NAME};


    private static final int DB_VERSION = 0;

    /**
     * Note this method may be very expensive if the database
     * does not already exist
     */
    public RusMorphDB() {
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
