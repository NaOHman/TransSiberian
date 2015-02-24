package com.naohman.transsiberian.Translation.Util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffrey on 1/10/15.
 * This class handles all of the translation for the app
 * because it interacts with the SQLiteDB, it is necessary to call
 * open before using it and close when one is done with it
 */
public class DictionaryHandler {
    private SQLiteDatabase database;
    private DictionaryDBHelper dbHelper;
    private static DictionaryHandler instance;
    private static final String[] TRANSLATION_COLS = {"keyword", "definition"};


    private DictionaryHandler(Context ctx) {
        dbHelper = new DictionaryDBHelper(ctx);
    }

    /*
     * Potentially very costly, do not run on UI thread
     */
    public static DictionaryHandler getInstance(Context appCtx){
        if (instance == null)
            synchronized (DictionaryHandler.class){
                if (instance == null)
                    instance = new DictionaryHandler(appCtx);
            }
        return instance;
    }

    /*
     * Initialize DB connection call this the first time you need the Dictionary
     */
    public void open() {
        if (database == null)
            database = dbHelper.getReadableDatabase();
    }

    /*
     * close DB connection call this when the DB is no longer active
     */
    public void close() {
        if (database != null)
            database.close();
    }

    /*
     * Queries the database to find translations for given words.
     * If it can't find a word, it tries to put words into 'dictionary form'
     * and tries again before giving up.
     * Note that this is an expensive call and could possibly trigger other
     * expensive operations
     */
    public List<DictEntry> getTranslations(String keyword, Context appCtx) {
        keyword = keyword.toLowerCase();
        Cursor cursor = queryKeyword(keyword);
        List<DictEntry> entries = new ArrayList<>();
        List<String> rawEntries = new ArrayList<>();
        if (cursor.getCount() == 0) {
            List<String> morphs = getDictionaryForms(keyword, appCtx);
            if (morphs == null || morphs.size() == 0){
                Log.d("No Morphs", keyword);
                String[] words = keyword.split("\\s");
                if (words.length > 1){
                    Log.d("Multiple words", ""+ words.length);
                    for (String word : words){
                        List<DictEntry> entryList = getTranslations(word, appCtx);
                        Log.d("Looking for", word);
                        if (entryList != null && entryList.size() > 0)
                            entries.addAll(entryList);
                    }
                    Log.d("Sending", entries.size() + " Entries");
                    return entries;
                } else {
                    return null;
                }
            }
            for (String morph : morphs) {
                cursor = queryKeyword(morph);
                rawEntries.addAll(getColumns(cursor, 1));
            }
        }
        rawEntries = getColumns(cursor, 1);
        for (String entry : rawEntries)
            entries.add(new DictEntry(entry));
        return entries;
    }

    /*
     * returns the dictionary form of a word
     */
    public static List<String> getDictionaryForms(String keyword, Context appCtx) {
        keyword = keyword.replaceAll("to\\s", "");
        keyword = keyword.replaceAll("[^A-Za-zА-Яа-я ]", "");
        List<String> baseforms = new ArrayList<>();
        try {
            if (isRussian(keyword)) {
                baseforms.addAll(RusMorph.getInstance(appCtx).getNormalForms(keyword));
            } else if (isEnglish(keyword)) {
                baseforms.addAll(EngMorph.getInstance(appCtx).getNormalForms(keyword));
            }
            return baseforms;
        } catch (Exception e){
            return null;
        }
    }

    /*
     * returns a cursor representing a DB query for the
     * specified keyword
     */
    private Cursor queryKeyword(String keyword) {
        String[] whereArgs = {keyword};
        Cursor cursor;
        if (isRussian(keyword)) {
            cursor = database.query(DictionaryDBHelper.TABLE_RE,
                    TRANSLATION_COLS, "keyword=?", whereArgs,
                    null, null, null);
        } else {
            cursor = database.query(DictionaryDBHelper.TABLE_ER,
                    TRANSLATION_COLS, "keyword=? COLLATE NOCASE", whereArgs,
                    null, null, null);
        }
        return cursor;
    }

    /*
     * returns all the entries in a column that a cursor points to
     */
    private static List<String> getColumns(Cursor c, int col) {
        List<String> entries = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String response = c.getString(col);
            entries.add(response);
            c.moveToNext();
        }
        return entries;
    }

    /*
     * matches Cyrillic characters and spaces
     */
    public static boolean isRussian(String s) {
        return s.matches("[ а-яА-Я]+");
    }

    /*
     * matches Roman characters and spaces
     */
    public static boolean isEnglish(String s) {
        return s.matches("[ a-zA-Z]+");
    }
}
