package com.naohman.transsiberian.Quizlet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jeffrey on 1/26/15.
 * Handles interactions with the Quizlet database
 */
public class QuizletDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "quizlet.db";
    public static final String TABLE_SETS = "sets";
    public static final String TABLE_TERMS = "terms";
    public static final String TABLE_JOBS = "jobs";
    public static final String COLUMN_ID = "_id";
    private static final String CREATE_TABLE = "CREATE TABLE %s (" + COLUMN_ID
            + " integer primary key autoincrement, %s);";

    public static final String SET_NAME = "title";
    public static final String SET_QUIZLET_ID = "quizlet_id";
    public static final String SET_DESCRIPTION = "description";
    public static final String SET_LANG_TERM = "lang_term";
    public static final String SET_LANG_DEF = "lang_def";

    public static final String TERM_TERM = "term";
    public static final String TERM_DEFINITION = "definition";
    public static final String TERM_SET_ID = "set_id";
    public static final String TERM_QUIZLET_ID = "quizlet_id";

    private static final String JOB_COLUMNS = "type text not null, data text";

    private static final String SET_COLUMNS = SET_QUIZLET_ID + " int, "
            + SET_NAME + " text not null, "+ SET_DESCRIPTION + " text not null, "
            + SET_LANG_DEF + " text not null, " + SET_LANG_TERM + " text not null";

    private static final String TERM_COLUMNS = TERM_QUIZLET_ID  + " int, "
            + TERM_TERM + " text not null, " + TERM_DEFINITION + " text not null, "
            + TERM_SET_ID + " integer not null";

    private static final int DB_VERSION = 2;

    public QuizletDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(CREATE_TABLE, TABLE_JOBS, JOB_COLUMNS));
        db.execSQL(String.format(CREATE_TABLE, TABLE_SETS, SET_COLUMNS));
        db.execSQL(String.format(CREATE_TABLE, TABLE_TERMS, TERM_COLUMNS));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOBS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMS);
        onCreate(db);
    }
}
