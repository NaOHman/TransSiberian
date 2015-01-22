package com.naohman.language.transsiberian.Singletons;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by jeffrey on 1/19/15.
 */
public class MyTTS implements  TextToSpeech.OnInitListener{
    private TextToSpeech tts;
    private boolean isTts;
    private static MyTTS instance;

    private MyTTS(Context c) {
        tts = new TextToSpeech(c, this);
    }

    /*
     * Potentially costly, avoid running on UI thread
     */
    public static MyTTS getInstance(Context appCtx){
        if (instance == null)
            synchronized (MyTTS.class){
                if (instance == null)
                    instance = new MyTTS(appCtx);
            }
        return instance;
    }

    /*
     * Returns true if TTS is available, false if it is not
     */
    public boolean say(String word){
        if(isTts){
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            return true;
        }
        return false;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("ru", "RU"));
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This language is not supported");
            } else {
                Log.d("TTS", "Language is supported");
                isTts = true;
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }
}
