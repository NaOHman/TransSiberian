package com.naohman.transsiberian.study;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.naohman.transsiberian.quizlet.Term;
import com.naohman.language.transsiberian.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Created by jeffrey on 1/29/15.
 * A class that tracks a flashcard study session
 */
public class FlashCardManager {
    private int roundScore=0, roundSize, totalScore=0;
    private boolean frontFirst, frontVisible, roundComplete;
    private List<Term> terms, saved;
    private Stack<Term> active;
    private ViewGroup parent;
    private Term current;

    /**
     * create a flashcard manager
     * @param terms the terms for the session
     * @param frontFirst whether to show the front first
     */
    public FlashCardManager(List<Term> terms, boolean frontFirst, ViewGroup holder) {
        this.terms = terms;
        this.frontFirst = frontFirst;
        this.parent = holder;
        frontVisible = frontFirst;
        setTerms(terms);
    }

    /**
     * set the terms for the current round
     * @param newTerms the terms for the round
     */
    private void setTerms(List<Term> newTerms){
        Collections.shuffle(newTerms);
        roundComplete = false;
        roundSize = newTerms.size();
        active = new Stack<>();
        active.addAll(newTerms);
        saved = new ArrayList<>();
        current = active.pop();
    }

    /**
     * turn the current flashcard into a view
     * @param front whether we want the front or back
     * @param activity the activity for this session, it must implement OnTouchListener
     * @return the current flashcard view
     */
    private View getView(boolean front, Activity activity){
        this.frontVisible = front;
        LayoutInflater inflater = activity.getLayoutInflater();
        TextView v;
        if (frontVisible) {
            v = (TextView) inflater.inflate(R.layout.flashcard_front, parent, false);
            v.setText(current.getTerm());
        } else {
            v = (TextView) inflater.inflate(R.layout.flashcard_back, parent, false);
            v.setText(current.getDefinition());
        }
        try {
            v.setOnTouchListener((View.OnTouchListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTouchListener");
        }
        return v;
    }

    /**
     * public method for getting the current view
     * @param activity the activity for this session, it must implement OnTouchListener
     * @return the resulting view
     */
    public View getView(Activity activity ){
        return getView(frontFirst, activity);
    }

    /**
     * Flips the card over and produces a new view
     * @param activity the activity for this session, it must implement OnTouchListener
     * @return the resulting view
     */
    public View flipView(Activity activity){
        frontVisible = !frontVisible;
        return getView(frontVisible, activity);
    }

    /**
     * save the current card for the next round
     */
    public void save(){
        saved.add(current);
        setNext();
    }

    /**
     * @return a string that shows the score for the round
     */
    public String getRoundScore(){
        return roundScore + " / " + roundSize;
    }

    /**
     * @return a string that shows the score for the session
     */
    public String getTotalScore(){
        return totalScore + " / " + terms.size();
    }

    /**
     * @return whether the session is complete
     */
    public boolean isFinished(){
        return totalScore == terms.size();
    }

    /**
     * Switch which side is shown first and reset the session
     */
    public void changeFirst(){
        frontFirst = !frontFirst;
        reset();

    }

    /**
     * load the next card, don't show the current one next round
     */
    public void next(){
        roundScore++;
        totalScore++;
        setNext();
    }

    /**
     * load the next card
     */
    private void setNext(){
        if (active.isEmpty())
            roundComplete = true;
        else
            current = active.pop();
    }

    /**
     * @return whether there are careds left in the round
     */
    public boolean roundComplete(){
        return roundComplete;
    }

    /**
     * restart the session
     */
    public void reset(){
        roundScore = 0;
        totalScore = 0;
        setTerms(terms);
    }

    /**
     * start the next round
     */
    public void nextRound(){
        roundScore = 0;
        setTerms(saved);
    }
}
