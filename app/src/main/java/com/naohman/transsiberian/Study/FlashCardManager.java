package com.naohman.transsiberian.Study;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.naohman.transsiberian.Quizlet.Term;
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
    private Term current;

    /**
     * create a flashcard manager
     * @param terms the terms for the session
     * @param frontFirst whether to show the front first
     */
    public FlashCardManager(List<Term> terms, boolean frontFirst) {
        this.terms = terms;
        this.frontFirst = frontFirst;
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
     * @param listener a listener for the view
     * @param parent the view parent of the flashcard
     * @param inflater the inflater
     * @param <T> a class that implemenets onClickListener and onTouchListener
     * @return the current flashcard view
     */
    private <T extends View.OnClickListener & View.OnTouchListener>
        View getView(boolean front, T listener,  ViewGroup parent, LayoutInflater inflater){
        this.frontVisible = front;
        TextView v;
        if (frontVisible) {
            v = (TextView) inflater.inflate(R.layout.flashcard_front, parent, false);
            v.setText(current.getTerm());
        } else {
            v = (TextView) inflater.inflate(R.layout.flashcard_back, parent, false);
            v.setText(current.getDefinition());
        }
        v.setOnClickListener(listener);
        v.setOnTouchListener(listener);
        return v;
    }

    /**
     * public method for getting the current view
     * @param listener the listener to attach to the card
     * @param parent the card's view parent
     * @param inflater the inflater to inflate the card
     * @param <T> a class that implements onClickListener and onTouchListener
     * @return the resulting view
     */
    public <T extends View.OnClickListener & View.OnTouchListener>
        View getView(T listener, ViewGroup parent, LayoutInflater inflater){
        return getView(frontFirst, listener, parent, inflater);
    }

    /**
     * Flips the card over and produces a new view
     * @param listener the listener to attach to the card
     * @param parent the card's view parent
     * @param inflater the inflater to inflate the card
     * @param <T> a class that implementents OnClickListener and OnTouchListener
     * @return the resulting view
     */
    public <T extends View.OnClickListener & View.OnTouchListener>
        View flipView(T listener, ViewGroup parent, LayoutInflater inflater){
        frontVisible = !frontVisible;
        return getView(frontVisible, listener, parent, inflater);
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
