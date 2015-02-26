package com.naohman.transsiberian.Study;

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
import com.naohman.transsiberian.Quizlet.Term;


/**
 * Created by Jeffrey Lyman
 * A fragment that let's users enter new terms or edit existing ones
 */
public class TermFragment extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {
    private static final String OLD_TERM = "param1";
    private NewTermListener termListener;
    private EditText termET, defET;
    private Term oldTerm;
    private boolean edit;
    private AlertDialog myDialog;

    /**
     * @param oldTerm the old term to edit, if null create a new term.
     * @return A new instance of fragment NewTermFragment.
     */
    public static TermFragment newInstance(Term oldTerm) {
        TermFragment fragment = new TermFragment();
        Bundle args = new Bundle();
        args.putSerializable(OLD_TERM, oldTerm);
        fragment.setArguments(args);
        return fragment;
    }

    public static TermFragment newInstance() {
        return new TermFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        Bundle args = getArguments();
        if (args != null) {
            oldTerm = (Term) args.getSerializable(OLD_TERM);
            edit = true;
        } else {
            edit = false;
        }
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View myView = inflater.inflate(R.layout.fragment_add_term, null, false);
        defET = (EditText) myView.findViewById(R.id.term_definition);
        termET = (EditText) myView.findViewById(R.id.term_term);
        defET.setOnEditorActionListener(this);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setView(myView)
                .setNegativeButton(getString(R.string.cancel), null);
        if (edit) {
            defET.setText(oldTerm.getDefinition());
            termET.setText(oldTerm.getTerm());
            dialog.setPositiveButton(getString(R.string.save), null);
        } else {
            dialog.setPositiveButton(getString(R.string.save_and_exit), null);
            dialog.setNeutralButton(getString(R.string.save), null);
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
        if (edit){
            termListener.editTerm(oldTerm, term, definition);
            dismiss();
        } else {
            termListener.addTerm(term, definition);
            defET.setText("");
            termET.setText("");
            focusKeyboard(termET);
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

}
