package com.naohman.transsiberian.quizlet;

import android.content.Context;
import android.net.ConnectivityManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * A Singleton class for interacting with Quizlet's api
 */
public class QuizletApi {
    //TODO basically everything
    private HttpClient client;
    private String access_token;
    private ConnectivityManager connMgr;
    private static QuizletApi instance;

    private QuizletApi(String token, Context appCtx){
        access_token = token;
        client = new DefaultHttpClient();
        connMgr =  (ConnectivityManager) appCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public QuizletApi getInstance(String token, Context appCtx){
        if (instance == null)
            instance = new QuizletApi(token, appCtx);
        return instance;
    }

    /**
     * Makes sure that the user is a part of the TransSiberian class
     */
    public void makeClass(){

    }

    /**
     * gets the given set from quizlet
     * returns null if it cannot find the set
     */
    public QuizletSet getSet(int setId){
        return null;
    }

    /**
     * deletes a set on quizlet
     * @param setId the id number of the set
     * @return whether this action was successful
     */
    public boolean deleteSet(int setId){
        return false;
    }

    /**
     * creates a new set on quizlet
     * @param set the set to be created
     */
    public boolean createSet(QuizletSet set){
        return false;
    }

    /**
     * Adds a term to the specified set
     * @param term term being added
     * @param setId the id number of the set
     * @return whether the term was successfully added
     */
    public boolean addTerm(Term term, int setId){
        return false;
    }

    /**
     * Removes a term from the set it is a part of
     * @param term the term to delete
     * @param setID the set the term belongs to
     * @return whether the term was successfully deleted
     */
    public boolean deleteTerm(Term term, int setID){
        return false;
    }

    /**
     * pulls changes from quizlet and pushes cached changes made by the use offline
     */
    public void sync(){

    }
}
