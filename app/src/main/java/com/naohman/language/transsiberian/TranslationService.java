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
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private EnglishLuceneMorphology engMorph;
    private RussianLuceneMorphology rusMorph;
    private static final String[] TRANSLATION_COLS = {"keyword", "definition"};
    private Context ctx;
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

    public TranslationService(Context context){
        this.ctx = context;
        Runnable rusTask = new makeRus();
        Runnable engTask = new makeEng();
        tRus = new Thread(rusTask);
        tEng = new Thread(engTask);
        tRus.start();
        tEng.start();
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

    public void initDB(){
        dbHelper = new DBHelper(this.ctx);
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
    private Spannable formatResponse(String response){
        response = response.replaceAll("<rref>.+</rref>", ""); //remove reference to external resources
        response = response.replaceAll("\\\\n", "<br>");    //turn newline into html linebreak
        DictHeading h = new DictHeading(response, 0, (Translate) ctx); //parse structure
        return h.toSpan();
    }

    /*
     * returns the dictionary form of a word
     */
    public List<String> getDictionaryForms(String keyword, boolean isRussian){
        keyword = keyword.replaceAll("to\\s*","");
        try {
            if (isRussian) {
                if (tRus.isAlive()) {
                    ((Translate)ctx).apologize();
                    return null;
                }else {
                    return rusMorph.getNormalForms(keyword);
                }
            } else {
                if (tEng.isAlive()){
                    ((Translate)ctx).apologize();
                    return null;
                }else {
                    return engMorph.getNormalForms(keyword);
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * Queries the database to find translations for given words.
     * If it can't find a word, it tries to put word into 'dictionary form'
     * and tries again before giving up.
     */
    public Spannable getTranslations(String keyword){
        keyword = keyword.toLowerCase();
        boolean isRussian = isRussian(keyword);
        Cursor cursor = queryKeyword(keyword, isRussian);
        if (cursor.getCount() == 0) {
            List<String> morphs = getDictionaryForms(keyword, isRussian);
            List<Spannable> response = new ArrayList<>();
            if (morphs == null){
                return null;
            }
            for(String morph: morphs){
                cursor = queryKeyword(morph, isRussian);
                response.addAll(getColumns(cursor, 1));
            }
            return concatSpans(response);
        }
        return concatSpans(getColumns(cursor, 1));

    }

    /*
     * returns a cursor representing a DB query for the
     * specified keyword
     */
    private Cursor queryKeyword(String keyword, boolean isRussian){
        String [] whereArgs = {keyword};
        Cursor cursor;
        if (isRussian){
            cursor = database.query(DBHelper.TABLE_RE,
                    TRANSLATION_COLS, "keyword=?", whereArgs,
                    null, null, null);
        } else {
            cursor = database.query(DBHelper.TABLE_ER,
                    TRANSLATION_COLS, "keyword=?", whereArgs,
                    null, null, null);
        }
        return cursor;
    }

    /*
     * returns all the entries in a column that a cursor points to
     */
    private List<Spannable> getColumns(Cursor c, int col){
        List<Spannable> entries = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String response = c.getString(col);
            entries.add(formatResponse(response));
            c.moveToNext();
        }
        return entries;
    }

    private Spannable concatSpans(List<Spannable> spans){
        SpannableStringBuilder s = new SpannableStringBuilder();
        for (Spannable span : spans){
            s.append(span).append(Html.fromHtml("<br>"));
        }
        return s;
    }
}
