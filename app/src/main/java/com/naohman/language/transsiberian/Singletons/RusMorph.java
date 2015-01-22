package com.naohman.language.transsiberian.Singletons;

import android.content.Context;
import android.util.Log;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by jeffrey on 1/21/15.
 */
public class RusMorph {
    private RussianLuceneMorphology morphology;
    private static RusMorph instance;

    /*
     * Creating Morphology objects is very expensive, do not do it
     * on the UI thread. Objects are saved in serialized form to
     * improve loading time
     */
    private RusMorph(final Context appCtx){
        final File suspend_f = new File(appCtx.getFilesDir(), "rusMorph");
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
                saveRusMorph(appCtx);
            } catch (Exception e) {
                Log.e("Problem creating Morphology", e.getMessage());
            }
        }
    }

    public static RusMorph getInstance(final Context appCtx){
        if (instance == null)
            synchronized (RusMorph.class){
                if (instance == null)
                    instance = new RusMorph(appCtx);
            }
        return instance;
    }

    private void saveRusMorph(Context c){
        final File savedFile = new File(c.getFilesDir(), "rusMorph");
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

    public List<String> getNormalForms(String keyword){
        return morphology.getNormalForms(keyword);
    }
}
