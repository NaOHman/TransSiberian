package com.naohman.transsiberian.translation.morphology;

import android.database.sqlite.SQLiteDatabase;

import com.naohman.transsiberian.translation.dictionary.RusMorphDB;

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
    public List<String> getNormalForms(String keyword){
        return morphology.getNormalForms(keyword);
    }

    public List<String> getMorphInfo(String keyword){
        return morphology.getMorphInfo(keyword);
    }

    public List<String> getConjugations(String keyword){
        return null;
    }
}
