package com.naohman.transsiberian.Quizlet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.naohman.language.transsiberian.R;


/**
 * Created by Jeffrey Lyman
 * A fragment that let's users enter new sets or edit existing ones
 */
public class SetFragment extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {
    private static final String OLD_SET = "param1";
    private NewSetListener setListener;
    private EditText titleET, descriptionET;
    private QuizletSet oldSet;
    private boolean edit;
    private Button termLangBtn, defLangBtn;
    private AlertDialog myDialog;

    /**
     * @param oldSet the old Set to edit
     * @return A new instance of fragment NewTermFragment.
     */
    public static SetFragment newInstance(QuizletSet oldSet) {
        SetFragment fragment = new SetFragment();
        Bundle args = new Bundle();
        args.putSerializable(OLD_SET, oldSet);
        fragment.setArguments(args);
        return fragment;
    }

    public static SetFragment newInstance() {
        return new SetFragment();
    }

    /**
     * Inflate the SetFragment layout and preform necessary setup
     * @return the inflated layout
     */
    private View makeView(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View myView = inflater.inflate(R.layout.fragment_new_set, null, false);
        descriptionET = (EditText) myView.findViewById(R.id.set_description);
        titleET = (EditText) myView.findViewById(R.id.set_name);
        termLangBtn = (Button) myView.findViewById(R.id.term_lang_btn);
        defLangBtn = (Button) myView.findViewById(R.id.def_lang_btn);
        descriptionET.setOnEditorActionListener(this);
        defLangBtn.setOnClickListener(this);
        termLangBtn.setOnClickListener(this);
        termLangBtn.setText(R.string.russian_terms);
        defLangBtn.setText(R.string.english_defs);
        if (edit){
            descriptionET.setText(oldSet.getDescription());
            titleET.setText(oldSet.getTitle());
            termLangBtn.setText(oldSet.getLang_termsPretty());
            defLangBtn.setText(oldSet.getLang_definitionsPretty());
        }
        return myView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        Bundle args = getArguments();
        if (args != null && args.containsKey(OLD_SET)) {
            oldSet = (QuizletSet) args.getSerializable(OLD_SET);
            edit = true;
        } else {
            edit = false;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setView(makeView())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null);
        myDialog = dialog.show();
        myDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        focusKeyboard(titleET);
        //Button's onClick must be overridden so that the dialog can stay open after they're pressed
        myDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this);
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
            myDialog.getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();
        }
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            setListener = (NewSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NewTermListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        setListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.def_lang_btn:
                if (defLangBtn.getText().toString() == getString(R.string.russian_defs))
                    defLangBtn.setText(R.string.english_defs);
                else
                    defLangBtn.setText(R.string.russian_defs);
                return;
            case R.id.term_lang_btn:
                if (termLangBtn.getText().toString() == getString(R.string.russian_terms))
                    termLangBtn.setText(R.string.english_terms);
                else
                    termLangBtn.setText(R.string.russian_terms);
                return;
            default:  //click came from dialog button
                Log.d("Registered default Click", "Onclick LIstener");
                String title = titleET.getText().toString();
                String description = descriptionET.getText().toString();
                boolean valid = checkInput(title);
                if (!valid)
                    return;
                String termLang, defLang;
                if (termLangBtn.getText().toString() == getString(R.string.russian_terms))
                    termLang = Quizlet.RUSSIAN;
                else
                    termLang = Quizlet.ENGLISH;
                if (defLangBtn.getText().toString() == getString(R.string.russian_defs))
                    defLang = Quizlet.RUSSIAN;
                else
                    defLang = Quizlet.ENGLISH;
                dismiss();
                if (edit) {
                    Log.d("Registered default Click", "Edit");
                    setListener.editSet(oldSet, title, description, termLang, defLang);
                } else {
                    Log.d("Registered default Click", "Add");
                    setListener.addSet(title, description, termLang, defLang);
                }
        }
    }

    /**
     * Checks to see if input represents a valid set and alerts the user if it isn't
     * @param title the title of the set
     * @return whether the input is not empty or whitespace
     */
    public boolean checkInput(String title) {
        if (title.matches("\\s*")) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Invalid input")
                    .setMessage("Please enter a title")
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
     * Created by jeffrey on 2/26/15.
     * An interface that allows an object to respond to requests to create or edit a set
     */
    public abstract interface NewSetListener {

     /**
     * Called when a new set is created
     * @param title the title of the set
     * @param description the description of the set
     * @param termLang the language of the terms in the set
     * @param defLang the language of the definitions in the set
     */
    public void addSet(String title, String description, String termLang, String defLang);
     /**
     * Called when a set is editted
     * @param oldSet the set being edited
     * @param title the title of the set
     * @param description the description of the set
     * @param termLang the language of the terms in the set
     * @param defLang the language of the definitions in the set
     */
    public void editSet(QuizletSet oldSet, String title, String description, String termLang, String defLang);
    }
}
