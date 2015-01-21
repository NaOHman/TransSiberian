package com.naohman.language.transsiberian;

import android.content.Context;
import android.util.Log;

import org.apache.lucene.morphology.english.EnglishLuceneMorphology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by jeffrey on 1/21/15.
 */
public class EngMorph {
    private EnglishLuceneMorphology morphology;
    private static EngMorph instance;

    private EngMorph(final Context appCtx){
        final File suspend_f = new File(appCtx.getFilesDir(), "rusMorph");
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
                saveMorph(appCtx, morphology);
            } catch (Exception e) {
                Log.e("Problem creating Morphology", e.getMessage());
            }
        }
    }

    public static EngMorph getInstance(final Context appCtx){
        if (instance == null)
            synchronized (EngMorph.class){
                if (instance == null)
                    instance = new EngMorph(appCtx);
            }
        return instance;
    }

    private static void saveMorph(Context c, EnglishLuceneMorphology morph){
        final File savedFile = new File(c.getFilesDir(), "engMorph");
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        boolean keep = true;
        try {
            fos = new FileOutputStream(savedFile);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(morph);
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

    public List<String> getNormalForms(String keyword){
        return morphology.getNormalForms(keyword);
    }
}
