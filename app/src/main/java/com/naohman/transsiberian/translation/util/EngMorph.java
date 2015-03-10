package com.naohman.transsiberian.translation.util;

import android.util.Log;

import com.naohman.transsiberian.setUp.App;

import org.apache.lucene.morphology.english.EnglishLuceneMorphology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by jeffrey on 1/21/15.
 * a singleton wrapper of Enlish morphology to streamline loading
 */
public class EngMorph {
    private EnglishLuceneMorphology morphology;
    private static EngMorph instance;

    /**
     * Note that construction a morphology is incredibly expensive.
     * The object is serialized in a file to save on loading time
     */
    private EngMorph(){
        final File suspend_f = new File(App.context().getFilesDir(), "rusMorph");
        if (suspend_f.exists()) {
            FileInputStream fis = null;
            ObjectInputStream is = null;
            try {
                fis = new FileInputStream(suspend_f);
                is = new ObjectInputStream(fis);
                morphology = (EnglishLuceneMorphology) is.readObject();
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
                morphology = new EnglishLuceneMorphology();
                saveMorph();
            } catch (Exception e) {
                Log.e("Problem creating Morphology", e.getMessage());
            }
        }
    }

    /**
     * Potentially very expensive, do not run on UI thread
     */
    public static EngMorph getInstance(){
        if (instance == null)
            synchronized (EngMorph.class){
                if (instance == null)
                    instance = new EngMorph();
            }
        return instance;
    }

    /**
     * serialize the morphology and save it to a file
     */
    private void saveMorph(){
        final File savedFile = new File(App.context().getFilesDir(), "engMorph");
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
     * @param keyword a conjugated word
     * @return the possible root words
     */
    public List<String> getNormalForms(String keyword){
        return morphology.getNormalForms(keyword);
    }
}
