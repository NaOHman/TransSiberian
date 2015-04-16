package com.naohman.transsiberian.translation.dictionary;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.setUp.App;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Created by jeffrey on 1/10/15.
 * This class handles all of the translation for the app
 * because it interacts with the SQLiteDB, it is necessary to call
 * open before using it and close when one is done with it
 */
public class DictionaryHandler {
    private static DictionaryHandler instance;
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    public static int RU = 1;
    public static int EN = 2;

    private DictionaryHandler() {
        dbHelper = new DBHelper();
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
     * close DB connection call this when the DB is no in.readLine(longer active
     */
    public void close() {
        if (database != null)
            database.close();
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
        int language = getLanguage(keyword);
        List<DictEntry> entries = queryKeyword(keyword, language);
        if (entries != null){
            return entries;
        }
        //No entries found, damage control time
        String[] words = keyword.split("\\s+");
        //Look for a root word
        if (words.length == 1)
            return translationsFromMorpheme(keyword, language);

        //split up the words and search individually
        entries = new ArrayList<>();
        for (String word : words) {
            List<DictEntry> wordEntries = getTranslations(word);
            if (wordEntries != null)
                entries.addAll(wordEntries);
        }
        return entries;
    }


    /**
     * Returns a list of Dictionary entries that correspond to a given morpheme
     * @param morpheme expected to be a single word
     * @return the list of dictEntries corresponding to that word
     */
    private List<DictEntry> translationsFromMorpheme(String morpheme, int language){
        List<String> roots = getDictionaryForms(morpheme);
        //No roots were found, try splitting the phrase
        if (roots == null || roots.size() == 0){
            Log.d("No Morphs", morpheme);
            return null;
        }
        //turn the query cursor into a list of DictEntries
        List<DictEntry> entries = new ArrayList<>();
        for (String root : roots) {
            entries.addAll(queryKeyword(root, language));
        }
        return entries;
    }

    /**
     * TODO handle mixed latin/cyrillic queries
     * @param keyword a search query
     * @return a cursor representing a database search for that keyword
     */
    private List<DictEntry> queryKeyword(String keyword, int language) {
        keyword = keyword.replaceAll("ё", "е");
        String[] whereArgs = {keyword};
        Cursor cursor;
        if (language == RU){
            cursor = database.query(DBHelper.TABLE_RE,
                    DBHelper.TRANSLATION_COLS, "keyword=?",
                    whereArgs, null, null, null);
        } else if (language == EN) {
            cursor = database.query(DBHelper.TABLE_ER,
                    DBHelper.TRANSLATION_COLS, "keyword=? COLLATE NOCASE",
                    whereArgs, null, null, null);
        } else {
            return null;
        }
        List<DictEntry> entries = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long pos = cursor.getLong(1);
            String definition = lookup(pos, language);
            entries.add(new DictEntry(definition));
            cursor.moveToNext();
        }
        return entries;
    }


    /**
     * todo keep file open?
     * @param position the position in the dict file with the needed entry
     * @param language the language of the query
     * @return a string representing the value of the query
     */
    private static String lookup(long position, int language){
        int file;
        if (language == EN) {
            file =  R.raw.er;
        } else if (language == RU){
            file = R.raw.re;
        } else {
            return null;
        }
        byte[] buffer = new byte[1024];
        String def = "";
        try{
            GZIPInputStream gzis =
                new GZIPInputStream(App.context().getResources().openRawResource(file));
            while (position > 0) {
                position -= gzis.skip(position);
            }
            int len = 0;
            while ((def.split("<k>")).length < 3 && len >= 0){
                len = gzis.read(buffer);
                if (len > 0)
                    def += new String(buffer, 0, len);
            }
            gzis.close();
            return "<k>" + def.split("<k>")[1];
        }catch(IOException ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * todo smarter string parsing
     * @param keyword the search query
     * @return the possible root words of the keyword
     */
    private List<String> getDictionaryForms(String keyword) {
        keyword = keyword.replaceAll("to\\s", "");
        keyword = keyword.replaceAll("[^A-Za-zА-Яа-я ]", "");
        List<String> baseforms = new ArrayList<>();
        try {
            if (isRussian(keyword)) {
                baseforms.addAll(getNormalForms(keyword));
            } else if (isEnglish(keyword)) {
//                baseforms.addAll(EngMorph.getInstance().getNormalForms(keyword));
            }
            return baseforms;
        } catch (Exception e){
            return null;
        }
    }

    private static int getLanguage(String keyword){
        if (isRussian(keyword))
            return RU;
        if (isEnglish(keyword))
            return EN;
        return 0;
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

    /**
     * @param keyword a declined word
     * @return a list of the possible root words
     */
    private Set<String> getNormalForms(String keyword){
        //Hyphenated may appear as word-otherword
        //or as word-word, we must handle both instances
        Set<String> normalForms = pullNormalForms(keyword);

        if (keyword.contains("-")) {
            Set<String> escapedForms = pullNormalForms(escapeDashes(keyword));
            for (String escapedForm : escapedForms) {
                normalForms.add(escapedForm.replaceAll("()",""));
            }
        }
        return normalForms;
    }

    private Set<String> pullNormalForms(String keyword) {
        String [] whereArgs = new String[keyword.length() - 1];
        StringBuilder sb = new StringBuilder().append(DBHelper.ROOT_SELECTOR);
        for (int i=1; i<keyword.length(); i++){
            whereArgs[i - 1] = keyword.substring(0,i);
            sb.append(DBHelper.ROOT_OR_CLAUSE);
        }
        String sql = sb.toString();
        Log.d("My query", sql);
        Cursor c = database.rawQuery(sb.toString(), whereArgs);
        return baseForms(keyword, c);
    }

    private Set<String> baseForms(String keyword, Cursor c){
        Set<String> normalForms = new HashSet<>();
        if (c.getCount() == 0)
            return normalForms;
        c.moveToFirst();
        while (!c.isAfterLast()){
            String stem = c.getString(0);
            String[] flexia = c.getString(1).split(",");
            if (contains(flexia, keyword.substring(stem.length())))
                normalForms.add(stem + flexia[0]);
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


    private String escapeDashes(String keyword){
        String[] chunks = keyword.split("-");
        String escaped = chunks[0];
        for (int i=1; i<chunks.length; i++){
            escaped += "(" +chunks[i]+")";
        }
        return escaped;
    }
}
