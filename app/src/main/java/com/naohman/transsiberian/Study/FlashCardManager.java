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
 */
public class FlashCardManager {
    private int roundScore=0, roundSize, totalScore=0;
    private boolean frontFirst, frontVisible, roundComplete;
    private List<Term> terms, saved;
    private Stack<Term> active;
    private Term current;

    public FlashCardManager(List<Term> terms, boolean frontFirst) {
        this.terms = terms;
        this.frontFirst = frontFirst;
        frontVisible = frontFirst;
        setTerms(terms);
    }

    public void setTerms(List<Term> newTerms){
        Collections.shuffle(newTerms);
        roundComplete = false;
        roundSize = newTerms.size();
        active = new Stack<>();
        active.addAll(newTerms);
        saved = new ArrayList<>();
        current = active.pop();
    }

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

    public <T extends View.OnClickListener & View.OnTouchListener>
        View getView(T listener, ViewGroup parent, LayoutInflater inflater){
        return getView(frontFirst, listener, parent, inflater);
    }

    public <T extends View.OnClickListener & View.OnTouchListener>
        View flipView(T listener, ViewGroup parent, LayoutInflater inflater){
        frontVisible = !frontVisible;
        return getView(frontVisible, listener, parent, inflater);
    }

    public void save(){
        saved.add(current);
        setNext();
    }

    public String getRoundScore(){
        return roundScore + " / " + roundSize;
    }

    public String getTotalScore(){
        return totalScore + " / " + terms.size();
    }

    public boolean isFinished(){
        return totalScore == terms.size();
    }

    public void changeFirst(){
        frontFirst = !frontFirst;
        reset();

    }
    public void next(){
        roundScore++;
        totalScore++;
        setNext();
    }

    private void setNext(){
        if (active.isEmpty())
            roundComplete = true;
        else
            current = active.pop();
    }

    public boolean roundComplete(){
        return roundComplete;
    }

    public void reset(){
        roundScore = 0;
        totalScore = 0;
        setTerms(terms);
    }

    public void nextRound(){
        roundScore = 0;
        setTerms(saved);
    }
}
