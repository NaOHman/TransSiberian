package com.naohman.language.transsiberian.Singletons;

import android.content.Context;

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

    private SetUpManager(){
        threadCount = Runtime.getRuntime().availableProcessors();
        workQueue = new LinkedBlockingQueue<>();
        executor = new ThreadPoolExecutor(threadCount, threadCount,
                KEEP_ALIVE_TIME, KEEP_ALIVE_UNITS, workQueue);
    }

    public static SetUpManager getInstance(){
        if (instance == null)
            instance = new SetUpManager();
        return instance;
    }

    public void shutDown(){
        //TODO cancel all threads
    }

    /*
     * Add a new Runnable to the pool
     */
    public void post(Runnable r){
        executor.execute(r);
    }


    /*
     * start-up tasks
     */
    public void loadRusMorphology(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                RusMorph.getInstance(appCtx);
            }
        });
    }

    public void loadEngMorphology(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                EngMorph.getInstance(appCtx);
            }
        });
    }

    public void loadTTS(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                MyTTS.getInstance(appCtx);
            }
        });
    }

    public void loadDictionary(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DictionaryHandler.getInstance(appCtx);
            }
        });
    }

    public void loadQuizlet(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Quizlet.getInstance(appCtx);
            }
        });
    }

}
