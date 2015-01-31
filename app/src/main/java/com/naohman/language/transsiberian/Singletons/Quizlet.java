package com.naohman.language.transsiberian.Singletons;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Html;

import com.naohman.language.transsiberian.Helpers.QuizletDBHelper;
import com.naohman.language.transsiberian.Helpers.QuizletSet;
import com.naohman.language.transsiberian.Helpers.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffrey on 1/26/15.
 */
public class Quizlet {
    public static String RUSSIAN = "";
    public static String ENGLISH = "";
    private static Quizlet instance;
    private static String[] set_columns = {QuizletDBHelper.COLUMN_ID,
            QuizletDBHelper.SET_NAME, QuizletDBHelper.SET_DESCRIPTION,
            QuizletDBHelper.SET_LANG_TERM, QuizletDBHelper.SET_LANG_DEF,
            QuizletDBHelper.SET_QUIZLET_ID};
    private static String[] term_columns = {QuizletDBHelper.COLUMN_ID,
            QuizletDBHelper.TERM_TERM, QuizletDBHelper.TERM_DEFINITION,
            QuizletDBHelper.TERM_SET_ID, QuizletDBHelper.TERM_QUIZLET_ID};
    private SQLiteDatabase db;
    private SQLiteOpenHelper dbHelper;

    private Quizlet(Context appCtx){
        dbHelper = new QuizletDBHelper(appCtx);
    }

    public static Quizlet getInstance(Context appCtx){
        if (instance == null)
            instance = new Quizlet(appCtx);
        return instance;
    }

    public void open(){
        this.db = dbHelper.getWritableDatabase();
    }

    public void close(){
        db.close();
    }

    public QuizletSet createSet(String name, String description, String termLang){
        if (db == null)
            open();
        ContentValues values = new ContentValues();
        values.put(QuizletDBHelper.SET_NAME, name);
        values.put(QuizletDBHelper.SET_DESCRIPTION, description);
        if (termLang == RUSSIAN){
            values.put(QuizletDBHelper.SET_LANG_TERM, RUSSIAN);
            values.put(QuizletDBHelper.SET_LANG_DEF, ENGLISH);
        } else {
            values.put(QuizletDBHelper.SET_LANG_TERM, ENGLISH);
            values.put(QuizletDBHelper.SET_LANG_DEF, RUSSIAN);
        }
        long id = db.insert(QuizletDBHelper.TABLE_SETS, null, values);
        Cursor cursor = db.query(QuizletDBHelper.TABLE_SETS, set_columns,
                QuizletDBHelper.COLUMN_ID + "=" + id, null, null, null, null);
        cursor.moveToFirst();
        return cursorToSet(cursor);
    }

    public void createTerm(long set_id, String term, String defintion){
        if (db == null)
            open();
        ContentValues values = new ContentValues();
        values.put(QuizletDBHelper.TERM_SET_ID, set_id);
        values.put(QuizletDBHelper.TERM_TERM, term);
        values.put(QuizletDBHelper.TERM_DEFINITION, defintion);
        db.insert(QuizletDBHelper.TABLE_TERMS, null, values);
    }

    public List<QuizletSet> getAllSets(){
        List<QuizletSet> sets = new ArrayList<>();
        Cursor cursor = db.query(QuizletDBHelper.TABLE_SETS,
                set_columns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            QuizletSet set = cursorToSet(cursor);
            sets.add(set);
            cursor.moveToNext();
        }
        cursor.close();
        return sets;
    }

    public List<Term> getSetTerms(long setId){
        List<Term> terms = new ArrayList<>();
        Cursor cursor = db.query(QuizletDBHelper.TABLE_TERMS,
                term_columns, QuizletDBHelper.TERM_SET_ID + "=?", new String[] {"" + setId},
                null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Term term = cursorToTerm(cursor);
            terms.add(term);
            cursor.moveToNext();
        }
        cursor.close();
        return terms;
    }

    public void removeTerm(Term t){
        db.delete(QuizletDBHelper.TABLE_TERMS,
                QuizletDBHelper.COLUMN_ID+ "=" + t.get_id(), null);
    }

    private QuizletSet cursorToSet(Cursor cursor){
        long _id = cursor.getLong(0);
        String title = cursor.getString(1);
        String description = cursor.getString(2);
        String term_lang = cursor.getString(3);
        String def_lang = cursor.getString(4);
        long setId = cursor.getInt(5);
        return new QuizletSet(_id, title, description, term_lang,def_lang,setId);
    }

    private Term cursorToTerm(Cursor cursor){
        long _id = cursor.getLong(0);
        String term = cursor.getString(1);
        String definition = cursor.getString(2);
        long setId = cursor.getLong(3);
        long quizlet_id = cursor.getLong(4);
        return new Term(_id, term, definition, setId, quizlet_id);
    }

}
