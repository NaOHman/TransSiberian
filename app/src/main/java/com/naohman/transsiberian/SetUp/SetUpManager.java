package com.naohman.transsiberian.SetUp;

import android.content.Context;

import com.naohman.transsiberian.Quizlet.Quizlet;
import com.naohman.transsiberian.Translation.Util.DictionaryHandler;
import com.naohman.transsiberian.Translation.Util.EngMorph;
import com.naohman.transsiberian.Translation.Util.MyTTS;
import com.naohman.transsiberian.Translation.Util.RusMorph;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeffrey on 1/21/15.
 * A thread pool that runs setup tasks on initialization;
 */
public class SetUpManager {
    private int threadCount;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_UNITS = TimeUnit.SECONDS;
    private ThreadPoolExecutor executor;
    private BlockingQueue<Runnable> workQueue;
    private static SetUpManager instance;

    public SetUpManager(){
        threadCount = Runtime.getRuntime().availableProcessors();
        workQueue = new LinkedBlockingQueue<>();
        executor = new ThreadPoolExecutor(threadCount, threadCount,
                KEEP_ALIVE_TIME, KEEP_ALIVE_UNITS, workQueue);
    }

    public void shutDown(){
        //TODO cancel all threads
    }

    /**
     * Adds a new runnable to the pool
     * @param r the runnable to add
     */
    public void post(Runnable r){
        executor.execute(r);
    }


    /**
     * Asynchronously create a Russian Lucene Morphology
     * @param appCtx the Application context
     */
    public void loadRusMorphology(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                RusMorph.getInstance(appCtx);
            }
        });
    }

    /**
     * Asynchronously create an English Lucene Morphology
     * @param appCtx the Application context
     */
    public void loadEngMorphology(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                EngMorph.getInstance(appCtx);
            }
        });
    }

    /**
     * Asynchronously create a TextToSpeech handler
     * @param appCtx the Application context
     */
    public void loadTTS(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                MyTTS.getInstance(appCtx);
            }
        });
    }

    /**
     * asynchronously load the translation dictionary
     * @param appCtx the Application context
     */
    public void loadDictionary(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DictionaryHandler.getInstance(appCtx);
            }
        });
    }

    /**
     * asynchronously load the Quizlet database
     * @param appCtx the Application context
     */
    public void loadQuizlet(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Quizlet.getInstance(appCtx);
            }
        });
    }

}
