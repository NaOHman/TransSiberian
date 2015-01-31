package com.naohman.language.transsiberian.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.List;

/**
 * Created by jeffrey on 1/26/15.
 */
public class QuizletApi {
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

    /*
     * Makes sure that the user is a part of the TransSiberian class
     */
    public void makeClass(){

    }

    /*
     * gets a set from quizlet
     */
    public List<Term> getSet(int setId){
        return null;
    }

    /**
     * deletes a set on quizlet
     * @param setId the id number of the set
     */
    public void deleteSet(int setId){

    }

    /**
     * creates a new set on quizlet
     * @param setName name of the set
     * @param description a description of the set
     */
    public void createSet(String setName, String description){

    }

    /**
     * Adds a term to the specified set
     * @param term term being added
     * @param setId the id number of the set
     */
    public void addTerm(Term term, int setId){

    }

    /**
     * Removes a term from the set it is a part of
     * @param term the term to delete
     * @param setID the set the term belongs to
     */
    public void deleteTerm(Term term, int setID){

    }

    /**
     * pulls changes from quizlet and pushes cached changes made by the use offline
     */
    public void sync(){

    }
}
