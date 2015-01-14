package com.naohman.language.transsiberian;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;

import org.apache.lucene.morphology.russian.RussianAnalyzer;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianMorphology;

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

    public TranslationService(Context context){
        this.ctx = context;
        try {
            this.engMorph = new EnglishLuceneMorphology();
            this.rusMorph = new RussianLuceneMorphology();
        } catch (IOException e){
            Log.e("MORPHOLOGY", e.getMessage());
        }
        dbHelper = new DBHelper(context);
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
    public List<String> getDictionaryForms(String keyword){
        keyword = keyword.replaceAll("to\\s*","");
        try {
            if (keyword.matches("[a-z]*")) {
                Log.e("Dict Form", "English Word");
                return engMorph.getNormalForms(keyword);
            } else {
                Log.e("Dict Form", "Russian Word");
                return rusMorph.getNormalForms(keyword);
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
        keyword.toLowerCase();
        Cursor cursor = queryKeyword(keyword);
        if (cursor.getCount() == 0) {
            List<String> morphs = getDictionaryForms(keyword);
            List<Spannable> response = new ArrayList<>();
            if (morphs == null){
                return null;
            }
            for(String morph: morphs){
                cursor = queryKeyword(morph);
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
    private Cursor queryKeyword(String keyword){
        String [] whereArgs = {keyword};
        Cursor cursor = database.query(DBHelper.TABLE_DICT,
                TRANSLATION_COLS, "keyword=?", whereArgs,
                null, null, null);
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
