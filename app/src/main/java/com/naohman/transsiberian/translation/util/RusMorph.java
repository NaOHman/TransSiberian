package com.naohman.transsiberian.translation.util;

import android.content.Context;
import android.util.Log;

import com.naohman.transsiberian.setUp.App;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by jeffrey on 1/21/15.
 * a singleton wrapper for Russian Lucene morphology to streamline loading
 */
public class RusMorph {
    private RussianLuceneMorphology morphology;
    private static RusMorph instance;

    /**
     * Creating Morphology objects is very expensive, do not do it
     * on the UI thread. Objects are saved in serialized form to
     * improve loading time
     */
    private RusMorph(){
        final File suspend_f = new File(App.context().getFilesDir(), "rusMorph");
        if (suspend_f.exists()) {
            FileInputStream fis = null;
            ObjectInputStream is = null;
            try {
                fis = new FileInputStream(suspend_f);
                is = new ObjectInputStream(fis);
                morphology = (RussianLuceneMorphology) is.readObject();
            } catch (Exception e) {
                Log.e("READ ERROR", e.getMessage());
            } finally {
                try {
                    if (fis != null) fis.close();
                    if (is != null) is.close();
                } catch (Exception e) {
                    Log.e("Error Closing Streams", e.getMessage());
                }
            }
        } else {
            try {
                morphology = new RussianLuceneMorphology();
                saveRusMorph();
            } catch (Exception e) {
                Log.e("Problem creating Morphology", e.getMessage());
            }
        }
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
     * Serialize and save to file
     */
    private void saveRusMorph(){
        final File savedFile = new File(App.context().getFilesDir(), "rusMorph");
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        boolean keep = true;
        try {
            fos = new FileOutputStream(savedFile);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(morphology);
        } catch (Exception e){
            keep = false;
        } finally {
            try {
                if (fos != null) fos.close();
                if (oos != null) oos.close();
                if (!keep) savedFile.delete();
            } catch (Exception e){
                Log.e("Error closing streams", e.getMessage());
            }
        }
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
}
