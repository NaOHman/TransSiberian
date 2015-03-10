package com.naohman.transsiberian.quizlet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffrey on 1/26/15.
 * A singleton that handles the quizlet business
 */
public class Quizlet {
    public static String RUSSIAN = "ru";
    public static String ENGLISH = "en";
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

    private Quizlet(){
        dbHelper = new QuizletDBHelper();
    }

    public static Quizlet getInstance(){
        if (instance == null)
            instance = new Quizlet();
        return instance;
    }

    /**
     * Open the Quizlet database for reading
     */
    public void open(){
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * Close the Quizlet database
     */
    public void close(){
        db.close();
    }

    /**
     * Create a new Quizlet set, it will automatically be added to the
     * TransSiberian Quizlet class
     * @param name the name of the set
     * @param description the set's description
     * @param termLang the language of the terms, the definition language is infered
     * @return the resulting quizlet set
     */
    public QuizletSet createSet(String name, String description, String termLang, String defLang){
        if (db == null)
            open();
        ContentValues values = new ContentValues();
        values.put(QuizletDBHelper.SET_NAME, name);
        values.put(QuizletDBHelper.SET_DESCRIPTION, description);
        values.put(QuizletDBHelper.SET_LANG_TERM, termLang);
        values.put(QuizletDBHelper.SET_LANG_DEF, defLang);
        long id = db.insert(QuizletDBHelper.TABLE_SETS, null, values);
        Cursor cursor = db.query(QuizletDBHelper.TABLE_SETS, set_columns,
                QuizletDBHelper.COLUMN_ID + "=" + id, null, null, null, null);
        cursor.moveToFirst();
        //TODO create job
        return cursorToSet(cursor);
    }

    /**
     * Create a Quizlet Term
     * @param set_id the database id of the parent set
     * @param term the term to be created
     * @param defintion the definition of the term
     */
    public void createTerm(long set_id, String term, String defintion){
        if (db == null)
            open();
        ContentValues values = new ContentValues();
        values.put(QuizletDBHelper.TERM_SET_ID, set_id);
        values.put(QuizletDBHelper.TERM_TERM, term);
        values.put(QuizletDBHelper.TERM_DEFINITION, defintion);
        db.insert(QuizletDBHelper.TABLE_TERMS, null, values);
        //TODO create job
    }

    /**
     * @return a list of all Quizlet sets in the database
     */
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

    /**
     * @param setId the database id of the quizlet set
     * @return a list of that set's terms
     */
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

    /**
     * removes a term from the database and quizlet
     * @param t the term to be deleted
     */
    public void removeTerm(Term t){
        db.delete(QuizletDBHelper.TABLE_TERMS,
                QuizletDBHelper.COLUMN_ID+ "=" + t.get_id(), null);
        //TODO create job
    }

    /**
     * removes a set from the database and quizlet
     * @param set the set to be deleted
     */
    public void deleteSet(QuizletSet set) {
        db.delete(QuizletDBHelper.TABLE_TERMS,
                QuizletDBHelper.TERM_SET_ID + "=" + set.get_id(), null);
        db.delete(QuizletDBHelper.TABLE_SETS,
                QuizletDBHelper.COLUMN_ID + "=" + set.get_id(), null);
        //TODO create job
    }


    /**
     * Internal method for turning a cursor into a set
     * @param cursor the cursor
     * @return the set the cursor points too
     */
    private QuizletSet cursorToSet(Cursor cursor){
        long _id = cursor.getLong(0);
        String title = cursor.getString(1);
        String description = cursor.getString(2);
        String term_lang = cursor.getString(3);
        String def_lang = cursor.getString(4);
        long setId = cursor.getInt(5);
        return new QuizletSet(_id, title, description, term_lang,def_lang,setId);
    }

    /**
     * Internal method for turning a cursor into a term
     * @param cursor the cursor
     * @return the Term the cursor points too
     */
    private Term cursorToTerm(Cursor cursor){
        long _id = cursor.getLong(0);
        String term = cursor.getString(1);
        String definition = cursor.getString(2);
        long setId = cursor.getLong(3);
        long quizlet_id = cursor.getLong(4);
        return new Term(_id, term, definition, setId, quizlet_id);
    }
}
