package com.naohman.language.transsiberian;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import java.io.IOException;
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
    private DBHelper dbHelper;
    private static DictionaryHandler instance;
    private static final String[] TRANSLATION_COLS = {"keyword", "definition"};

    /*
     * if there is a cyrillic character, assume that it's cyrillic
     */
    public static boolean isRussian(String s) {
        return s.matches("[ а-яА-Я]+");
    }

    public static boolean isEnglish(String s) {
        return s.matches("[ a-zA-Z]+");
    }

    private DictionaryHandler(Context ctx) {
        dbHelper = new DBHelper(ctx);
    }

    public static DictionaryHandler getInstance(Context appCtx){
        if (instance == null)
            synchronized (DictionaryHandler.class){
                if (instance == null)
                    instance = new DictionaryHandler(appCtx);
            }
        return instance;
    }

    //initialize DB connection
    public void open() {
        if (database == null)
            database = dbHelper.getReadableDatabase();
    }

    //close DB connection
    public void close() {
        if (database != null)
            database.close();
    }

    //Take DB entries and format them in android HTML
    private static List<DictHeading> parseDBEntry(List<String> responses) {
        List<DictHeading> entries = new ArrayList<>();
        for (String response : responses) {
            response = response.replaceAll("<rref>[^<]+</rref>", ""); //remove reference to external resources
            response = response.replaceAll("\\\\n", "<br>");    //turn newline into html linebreak
            entries.add(new DictHeading(response, 0)); //parse structure
        }
        return entries;
    }

    /*
     * returns the dictionary form of a word
     */
    public static List<String> getDictionaryForms(String keyword) {
        keyword = keyword.replaceAll("to\\s*", "");
        keyword = keyword.replaceAll("[^A-Za-zА-Яа-я]", "");
        String[] words = keyword.split("\\s");
        List<String> baseforms = new ArrayList<>();
        try {
            for (String word : words) {
                if (isRussian(word)) {
                    baseforms.addAll(RusMorph.getInstance(null).getNormalForms(word));
                } else if (isEnglish(word)) {
                    baseforms.addAll(EngMorph.getInstance(null).getNormalForms(word));
                }
            }
            return baseforms;
        } catch (Exception e){
            return null;
        }
    }

    /*
     * Queries the database to find translations for given words.
     * If it can't find a word, it tries to put word into 'dictionary form'
     * and tries again before giving up.
     */
    public List<DictHeading> getTranslations(String keyword) {
        keyword = keyword.toLowerCase();
        Cursor cursor = queryKeyword(keyword);
        if (cursor.getCount() == 0) {
            List<String> morphs = getDictionaryForms(keyword);
            List<String> response = new ArrayList<>();
            if (morphs == null) {
                return null;
            }
            for (String morph : morphs) {
                cursor = queryKeyword(morph);
                response.addAll(getColumns(cursor, 1));
            }
            return parseDBEntry(response);
        }
        return parseDBEntry(getColumns(cursor, 1));
    }

    /*
     * returns a cursor representing a DB query for the
     * specified keyword
     */
    private Cursor queryKeyword(String keyword) {
        String[] whereArgs = {keyword};
        Cursor cursor;
        if (isRussian(keyword)) {
            cursor = database.query(DBHelper.TABLE_RE,
                    TRANSLATION_COLS, "keyword=?", whereArgs,
                    null, null, null);
        } else {
            cursor = database.query(DBHelper.TABLE_ER,
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
}
