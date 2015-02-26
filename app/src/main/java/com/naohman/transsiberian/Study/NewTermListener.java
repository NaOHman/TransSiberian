package com.naohman.transsiberian.Study;

import com.naohman.transsiberian.Quizlet.Term;

/**
 * Created by jeffrey on 2/25/15.
 * An interface that allows an object to respond to requests to create or edit an object
 */
public abstract interface NewTermListener {
    /**
     * Called when a new term is created
     * @param term the new term
     * @param definition the new definition
     */
    public void addTerm(String term, String definition);

    /**
     * Called when a term is edited
     * @param oldTerm the old term object
     * @param term the new term
     * @param definition the new definition
     */
    public void editTerm(Term oldTerm, String term, String definition);
}
