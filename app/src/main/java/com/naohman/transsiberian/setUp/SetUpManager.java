package com.naohman.transsiberian.setUp;

import android.content.Context;

import com.naohman.transsiberian.quizlet.Quizlet;
import com.naohman.transsiberian.translation.util.DictionaryHandler;
import com.naohman.transsiberian.translation.util.EngMorph;
import com.naohman.transsiberian.translation.util.MyTTS;
import com.naohman.transsiberian.translation.util.RusMorph;

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
        executor.shutdown();
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
     */
    public void loadRusMorphology(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                RusMorph.getInstance();
            }
        });
    }

    /**
     * Asynchronously create an English Lucene Morphology
     */
    public void loadEngMorphology(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                EngMorph.getInstance();
            }
        });
    }

    /**
     * Asynchronously create a TextToSpeech handler
     */
    public void loadTTS(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                MyTTS.getInstance();
            }
        });
    }

    /**
     * asynchronously load the translation dictionary
     */
    public void loadDictionary(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DictionaryHandler.getInstance();
            }
        });
    }

    /**
     * asynchronously load the Quizlet database
     */
    public void loadQuizlet(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Quizlet.getInstance();
            }
        });
    }

}
