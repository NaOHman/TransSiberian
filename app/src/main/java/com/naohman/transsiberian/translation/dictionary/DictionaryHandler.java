package com.naohman.transsiberian.translation.dictionary;

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


    private DictionaryHandler() {
        dbHelper = new DictionaryDBHelper();
    }

    /**
     * Potentially very costly, do not run on UI thread
     */
    public static DictionaryHandler getInstance(){
        if (instance == null)
            synchronized (DictionaryHandler.class){
                if (instance == null)
                    instance = new DictionaryHandler();
            }
        return instance;
    }

    /**
     * Initialize DB connection call this the first time you need the Dictionary
     */
    public void open() {
        if (database == null)
            database = dbHelper.getReadableDatabase();
    }

    /**
     * close DB connection call this when the DB is no longer active
     */
    public void close() {
        if (database != null)
            database.close();
    }

    /**
     * Queries the database to find translations for given words.
     * If it can't find a word, it tries to put words into 'dictionary form'
     * and tries again before giving up.
     * Note that this is an expensive call and could possibly trigger other
     * expensive operations
     * @param keyword the keyword to search
     * @return a list of Dictionary Entries representing the translations
     */
    public List<DictEntry> getTranslations(String keyword) {
        keyword = keyword.toLowerCase();
        Cursor cursor = queryKeyword(keyword);
        List<DictEntry> entries = new ArrayList<>();
        List<String> rawEntries = new ArrayList<>();
        //No entries found, damage control time
        if (cursor.getCount() == 0) {
            //Try to find the root word of the query
            List<String> morphs = getDictionaryForms(keyword);
            //No roots were found, try splitting the phrase
            if (morphs == null || morphs.size() == 0){
                Log.d("No Morphs", keyword);
                String[] words = keyword.split("\\s");
                //If there's only one word give up
                if (words.length > 1){
                    Log.d("Multiple words", ""+ words.length);
                    //Query each word and add them to the list of return values
                    for (String word : words){
                        List<DictEntry> entryList = getTranslations(word);
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
            //turn the query cursor into a list of DictEntries
            for (String morph : morphs) {
                cursor = queryKeyword(morph);
                rawEntries.addAll(getColumns(cursor, 1));
            }
        }
        //turn the query cursor into a list of DictEntries
        rawEntries = getColumns(cursor, 1);
        for (String entry : rawEntries)
            entries.add(new DictEntry(entry));
        return entries;
    }

    /**
     * @param keyword the search query
     * @return the possible root words of the keyword
     */
    private static List<String> getDictionaryForms(String keyword) {
        keyword = keyword.replaceAll("to\\s", "");
        keyword = keyword.replaceAll("[^A-Za-zА-Яа-я ]", "");
        List<String> baseforms = new ArrayList<>();
        try {
            if (isRussian(keyword)) {
                baseforms.addAll(RusMorph.getInstance().getNormalForms(keyword));
            } else if (isEnglish(keyword)) {
                baseforms.addAll(EngMorph.getInstance().getNormalForms(keyword));
            }
            return baseforms;
        } catch (Exception e){
            return null;
        }
    }

    /**
     * @param keyword a search query
     * @return a cursor representing a database search for that keyword
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

    /**
     * @param c a cursor into the database
     * @param col the column we want to return
     * @return all the entries in a column that a cursor points to
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

    /**
     * @param s a string of characters
     * @return whether that string could be a russian word or phrase
     */
    private static boolean isRussian(String s) {
        return s.matches("[ а-яА-Я]+");
    }

    /**
     * @param s a string of characters
     * @return whether that string could be an english word or phase
     */
    private static boolean isEnglish(String s) {
        return s.matches("[ a-zA-Z]+");
    }
}
