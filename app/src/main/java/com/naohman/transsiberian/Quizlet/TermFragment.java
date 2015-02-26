package com.naohman.transsiberian.Quizlet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.naohman.language.transsiberian.R;


/**
 * Created by Jeffrey Lyman
 * A fragment that let's users enter new terms or edit existing ones
 */
public class TermFragment extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {
    private static final String OLD_TERM = "old term";
    private static final String TERM = "term";
    private static final String DEF = "def";
    private NewTermListener termListener;
    private String startDef = "", startTerm = "";
    private EditText termET, defET;
    private Term oldTerm;
    private boolean edit, multiple;
    private AlertDialog myDialog;

    /**
     * @param oldTerm the old term to edit, if null create a new term.
     * @return A new instance of TermFragment with the proper arguments
     */
    public static TermFragment newInstance(Term oldTerm) {
        TermFragment fragment = new TermFragment();
        Bundle args = new Bundle();
        args.putSerializable(OLD_TERM, oldTerm);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Make a new term with the given values for term and def
     * @param term the starting value for term
     * @param def the starting value for def
     * @return a new instance of TermFragment with the proper arugments
     */
    public static TermFragment newInstance(String term, String def) {
        TermFragment fragment = new TermFragment();
        Bundle args = new Bundle();
        args.putString(TERM, term);
        args.putString(DEF, def);
        fragment.setArguments(args);
        return fragment;
    }

    public static TermFragment newInstance() {
        return new TermFragment();
    }

    public void parseArgs(){
        Bundle args = getArguments();
        if (args != null) {
            multiple = false;
            if (args.containsKey(OLD_TERM)) {
                oldTerm = (Term) args.getSerializable(OLD_TERM);
                edit = true;
            } else {
                startDef = args.getString(TERM, "");
                startTerm = args.getString(DEF, "");
            }
        } else {
            edit = false;
            multiple = true;
        }
    }

    /**
     * Inflate the TermFragment layout and preform necessary setup
     * @return the inflated layout
     */
    private View makeView(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View myView = inflater.inflate(R.layout.fragment_add_term, null, false);
        defET = (EditText) myView.findViewById(R.id.term_definition);
        termET = (EditText) myView.findViewById(R.id.term_term);
        myView.findViewById(R.id.swap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence def = defET.getText();
                defET.setText(termET.getText());
                termET.setText(def);
            }
        });
        defET.setOnEditorActionListener(this);
        if (edit){
            defET.setText(oldTerm.getDefinition());
            termET.setText(oldTerm.getTerm());
        } else {
            defET.setText(startDef);
            termET.setText(startTerm);
        }
        return myView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        parseArgs();
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setView(makeView())
                .setNegativeButton(R.string.cancel, null);
        if (multiple) {
            dialog.setPositiveButton(R.string.save_and_exit, null);
            dialog.setNeutralButton(R.string.save, null);
        } else {
            dialog.setPositiveButton(R.string.save, null);
        }
        myDialog = dialog.show();
        myDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        focusKeyboard(termET);
        //Button's onClick must be overridden so that the dialog can stay open after the're pressed
        myDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this);
        myDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(this);
        return myDialog;
    }

    /**
     * forcus the keyboard on a given view
     * @param v the view to be focused
     */
    public void focusKeyboard(View v) {
        if (v.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            if (edit)
                myDialog.getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();
            else
                myDialog.getButton(DialogInterface.BUTTON_NEUTRAL).callOnClick();
            return true;
        }
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            termListener = (NewTermListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NewTermListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        termListener = null;
    }

    @Override
    public void onClick(View v) {
        String term = termET.getText().toString();
        String definition = defET.getText().toString();
        boolean valid = checkInput(term, definition);
        if (!valid)
            return;
        if (multiple){
            termListener.addTerm(term, definition);
            defET.setText("");
            termET.setText("");
            focusKeyboard(termET);
        } else {
            if (edit)
                termListener.editTerm(oldTerm, term, definition);
            else
                termListener.addTerm(term, definition);
            dismiss();
        }
    }

    /**
     * Checks to see if input represents a valid input, if not, it alerts the user
     * @param term the term
     * @param definition the definition
     * @return whether the input is not empty or whitespace
     */
    public boolean checkInput(String term, String definition) {
        if (term.matches("\\s*") || definition.matches("\\s*")) {
            String errMsg = "Please enter a Definition";
            if (term.matches("\\s*"))
                errMsg = "Please Enter a Term";
            new AlertDialog.Builder(getActivity())
                    .setTitle("Invalid input")
                    .setMessage(errMsg)
                    .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            return false;
        }
        return true;
    }

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
}
