package com.naohman.language.transsiberian;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
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
public class TranslationService {
    private static TranslationService instance = null;
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private EnglishLuceneMorphology engMorph;
    private RussianLuceneMorphology rusMorph;
    private static final String[] TRANSLATION_COLS = {"keyword", "definition"};
    private Thread tRus;
    private Thread tEng;

    /*
     * if there is a cyrillic character, assume that it's cyrillic
     */
    public static boolean isRussian(String s){
        boolean isR = s.matches("[ а-яА-Я]+");
        if (isR)
            Log.d("isRussian " + s, "Yes");
        else
            Log.d("isRussian " + s, "No");
        return isR;
    }

    private TranslationService(){
        Runnable rusTask = new makeRus();
        Runnable engTask = new makeEng();
        tRus = new Thread(rusTask);
        tEng = new Thread(engTask);
        tRus.start();
        tEng.start();
    }

    public static TranslationService getInstance(){
        if (instance == null)
            instance = new TranslationService();
        return instance;
    }

    public void initDB(Context ctx){
        dbHelper = new DBHelper(ctx);
    }

    //initialize DB connection
    public void open(){
        database = dbHelper.getReadableDatabase();
    }
    //close DB connection
    public void close(){
        database.close();
    }

    //Take DB entries and format them in android HTML
    private Spannable formatResponse(List<String> responses, Html.TagHandler handler){
        SpannableStringBuilder s = new SpannableStringBuilder();
        for (String response : responses) {
            response = response.replaceAll("<rref>[^<]+</rref>", ""); //remove reference to external resources
            response = response.replaceAll("\\\\n", "<br>");    //turn newline into html linebreak
            DictHeading h = new DictHeading(response, 0, handler); //parse structure
            s = s.append(h.toSpan()).append("\n");
        }
        return s;
    }

    /*
     * returns the dictionary form of a word
     */
    public List<String> getDictionaryForms(String keyword) {
        keyword = keyword.replaceAll("to\\s*", "");
        String[] words = keyword.split("\\s");
        List<String> baseforms = new ArrayList<>();
        for (String word : words) {
            if (isRussian(word)) {
                if (tRus.isAlive())
                    throw new IllegalAccessError("Russian");
                baseforms.addAll(rusMorph.getNormalForms(word));
            } else {
                if (tEng.isAlive())
                    throw new IllegalAccessError("English");
                baseforms.addAll(engMorph.getNormalForms(word));
            }
        }
        return baseforms;
    }

    /*
     * Queries the database to find translations for given words.
     * If it can't find a word, it tries to put word into 'dictionary form'
     * and tries again before giving up.
     */
    public Spannable getTranslations(String keyword, Html.TagHandler h){
        keyword = keyword.toLowerCase();
        Cursor cursor = queryKeyword(keyword);
        if (cursor.getCount() == 0) {
            List<String> morphs = getDictionaryForms(keyword);
            List<String> response = new ArrayList<>();
            if (morphs == null){
                return null;
            }
            for(String morph: morphs){
                cursor = queryKeyword(morph);
                response.addAll(getColumns(cursor, 1));
            }
            return formatResponse(response, h);
        }
        return formatResponse(getColumns(cursor, 1), h);

    }

    /*
     * returns a cursor representing a DB query for the
     * specified keyword
     */
    private Cursor queryKeyword(String keyword){
        String [] whereArgs = {keyword};
        Cursor cursor;
        if (isRussian(keyword)){
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
    private List<String> getColumns(Cursor c, int col){
        List<String> entries = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String response = c.getString(col);
            entries.add(response);
            int maxLogSize = 1000;
            for(int i = 0; i <= response.length() / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i+1) * maxLogSize;
                end = end > response.length() ? response.length() : end;
                Log.v("Dict Entry", response.substring(start, end));
            }
            c.moveToNext();
        }
        return entries;
    }

    private class makeEng implements Runnable {
        @Override
        public void run(){
            try {
                TranslationService.this.engMorph = new EnglishLuceneMorphology();
            }catch (IOException e) {
                Log.e("ENGLISH MORPHOLOGY", e.getMessage());
            }
        }
    }
    private class makeRus implements Runnable {
        @Override
        public void run(){
            try {
                TranslationService.this.rusMorph = new RussianLuceneMorphology();
            }catch (IOException e) {
                Log.e("ENGLISH MORPHOLOGY", e.getMessage());
            }
        }
    }

}
