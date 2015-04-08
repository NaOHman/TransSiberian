package com.naohman.transsiberian.translation.morphology;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by jeffrey on 1/21/15.
 * a singleton wrapper for Russian Lucene morphology to streamline loading
 */
public class RusMorph {
    private SQLiteDatabase database;
    private RusMorphDB dbHelper;
    private static RusMorph instance;

    private RusMorph() {
        dbHelper = new RusMorphDB();
        database = dbHelper.getReadableDatabase();
    }

    /**
     * Note this is potentially very costly, do not run on the UI thread
     * @return the Russian morphology
     */
    public static RusMorph getInstance(){
        if (instance == null)
            synchronized (RusMorph.class){
                if (instance == null)
                    instance = new RusMorph();
            }
        return instance;
    }

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
