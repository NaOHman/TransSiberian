package com.naohman.transsiberian.translation.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.naohman.transsiberian.setUp.App;

import java.util.Locale;

/**
 * Created by jeffrey on 1/19/15.
 * a Singleton wrapper for Text to speech which streamlines loading
 */
public class MyTTS implements  TextToSpeech.OnInitListener{
    private TextToSpeech tts;
    private boolean isTts;
    private static MyTTS instance;

    private MyTTS() {
        tts = new TextToSpeech(App.context(), this);
    }

    /**
     * Potentially costly, avoid running on UI thread
     */
    public static MyTTS getInstance(){
        if (instance == null)
            synchronized (MyTTS.class){
                if (instance == null)
                    instance = new MyTTS();
            }
        return instance;
    }

    /**
     * @param word the word to say
     * @return true if TTS is available, false if it is not
     */
    public boolean say(String word){
        if(isTts){
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            return true;
        }
        return false;
    }

    /**
     * Load russian language when tts is initialized
     * @param status the initialization status code
     */
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
