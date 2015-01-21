package com.naohman.language.transsiberian;

import android.content.Context;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeffrey on 1/21/15.
 */
public class SetUpManager {
    private int threadCount;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_UNITS = TimeUnit.SECONDS;
    private ThreadPoolExecutor executor;
    private BlockingQueue<Runnable> workQueue;
    private static SetUpManager instance;

    private SetUpManager(Context appCtx){
        threadCount = Runtime.getRuntime().availableProcessors();
        workQueue = new LinkedBlockingQueue<>();
        executor = new ThreadPoolExecutor(threadCount, threadCount,
                KEEP_ALIVE_TIME, KEEP_ALIVE_UNITS, workQueue);
        setUpTasks(appCtx);
    }

    public void shutDown(){

    }

    public void post(Runnable r){
        executor.execute(r);
    }

    public static SetUpManager getInstance(Context appCtx){
        if (instance == null)
            instance = new SetUpManager(appCtx);
        return instance;
    }

    private void setUpTasks(final Context appCtx){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DictionaryHandler.getInstance(appCtx);
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                RusMorph.getInstance(appCtx);
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                EngMorph.getInstance(appCtx);
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                MyTTS.getInstance(appCtx);
            }
        });
    }
}
