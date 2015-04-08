package com.naohman.transsiberian.translation.dictionary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.naohman.transsiberian.translation.morphology.EngMorph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
     * Returns a list of Dictionary entries that correspond to a given morpheme
     * @param morpheme expected to be a single word
     * @return the list of dictEntries corresponding to that word
     */
    private List<DictEntry> translationsFromMorpheme(String morpheme){
        List<String> roots = getDictionaryForms(morpheme);
        //No roots were found, try splitting the phrase
        if (roots == null || roots.size() == 0){
            Log.d("No Morphs", morpheme);
            return null;
        }
        //turn the query cursor into a list of DictEntries
        List<DictEntry> entries = new ArrayList<>();
        for (String root : roots) {
            entries.addAll(cursorToDictEntries(queryKeyword(root)));
        }
        return entries;
    }
    /**
     * Queries the database to find translations for given words.
     * If it can't find a word, it tries to put words into split the word
     * into different words, and if that doesn't work, it tries to find the
     * root word before it gives up.
     * Note that this is an expensive call and could possibly trigger other
     * expensive operations
     * @param keyword the keyword to search
     * @return a list of Dictionary Entries representing the translations
     */
    public List<DictEntry> getTranslations(String keyword) {
        keyword = keyword.toLowerCase();
        Cursor cursor = queryKeyword(keyword);
        if (cursor.getCount() > 0)
            return cursorToDictEntries(cursor);

        //No entries found, damage control time
        String[] words = keyword.split("\\s+");
        //Look for a root word
        if (words.length == 1)
            return translationsFromMorpheme(keyword);

        //split up the words and search individually
        List<DictEntry> entries = new ArrayList<>();
        for (String word : words) {
            List<DictEntry> wordEntries = getTranslations(word);
            if (wordEntries != null)
                entries.addAll(wordEntries);
        }
        return entries;
    }

    private static List<DictEntry> cursorToDictEntries(Cursor cursor){
        List<String> rawEntries = getColumns(cursor, 1);
        List<DictEntry> entries = new ArrayList<>();
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
     * TODO handle mixed latin/cyrillic queries
     * @param keyword a search query
     * @return a cursor representing a database search for that keyword
     */
    private Cursor queryKeyword(String keyword) {
        keyword = keyword.replaceAll("ё", "е");
        String[] whereArgs = {keyword};
        Cursor cursor;
        if (isRussian(keyword)) {
            cursor = database.query(DictionaryDBHelper.TABLE_RE,
                    DictionaryDBHelper.TRANSLATION_COLS, "keyword=?", 
                    whereArgs, null, null, null);
        } else {
            cursor = database.query(DictionaryDBHelper.TABLE_ER,
                    DictionaryDBHelper.TRANSLATION_COLS, "keyword=? COLLATE NOCASE", 
                    whereArgs, null, null, null);
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

///////////////////RUSSIAN MORPHOLOGY STUFF////////////////////

    /**
     * @param keyword a declined word
     * @return a list of the possible root words
     */
    private Set<String> getNormalForms(String keyword){
        //Hyphenated may appear as word-otherword
        //or as word-word, we must handle both instances
        Set<String> normalForms = new HashSet<>();
        normalForms.addAll(pullNormalForms(keyword));

        if (keyword.contains("-")) {
            Set<String> escapedForms = pullNormalForms(escapeDashes(keyword));
            for (String escapedForm : escapedForms) {
                normalForms.add(escapedForm.replaceAll("()",""));
            }
        }
        return normalForms;
    }

    private Set<String> pullNormalForms(String keyword) {
        Set<String> normalForms = new HashSet<>();
        for (int i=0; i<=keyword.length(); i++){
            normalForms.addAll(queryStemPair(keyword.substring(0,i), keyword.substring(i)));
        }
        return normalForms;
    }

    private Set<String> queryStemPair(String root, String flexia){
        Set<String> normalForms = new HashSet<>();
        Cursor c = database.rawQuery(RusMorphDB.ROOT_SELECTOR, new String[] {root});
        if (c.getCount() == 0)
            return normalForms;
        c.moveToFirst();
        while(!c.isAfterLast()){
            String flexiaList = c.getString(0);
            String[] flexiaArray = flexiaList.split(",");
            if (contains(flexiaArray, flexia))
                normalForms.add(root + flexiaArray[0]);
            c.moveToNext();
        }
        return normalForms;
    }

    private static boolean contains(String[] flexiaArray, String flexia){
        for (String f : flexiaArray){
            if (f.equals(flexia))
                return true;
        }
        return false;
    }

//    public List<String> getMorphInfo(String keyword){
//        return morphology.getMorphInfo(keyword);
//    }

    public List<String> getConjugations(String keyword){
        return null;
    }

    private String escapeDashes(String keyword){
        String[] chunks = keyword.split("-");
        String escaped = chunks[0];
        for (int i=1; i<chunks.length; i++){
            escaped += "(" +chunks[i]+")";
        }
        return escaped;
    }
}
