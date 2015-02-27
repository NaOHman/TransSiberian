package com.naohman.transsiberian.study;

import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.naohman.transsiberian.quizlet.QuizletSet;
import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.quizlet.Quizlet;
import com.naohman.transsiberian.quizlet.SetFragment;
import com.naohman.transsiberian.quizlet.Term;

import java.util.List;

/**
 * Created By Jeffrey Lyman
 * an activity that displays all available sets and allows users to pick one to study
 */
public class SetListActivity extends ActionBarActivity implements SetFragment.NewSetListener{
    private ListView sets;
    private Quizlet quizlet;
    private FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_list);
        sets = (ListView) findViewById(R.id.set_listview);
        quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Make sure we refresh the set list as it may have changed
     */
    @Override
    public void onResume(){
        super.onResume();
        reloadSetList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.new_set) {
            SetFragment setFragment = SetFragment.newInstance();
            setFragment.show(fm, "new set");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add a set to the database and reload the set list
     * @param title the title of the set
     * @param description the description of the set
     * @param termLang the language of the terms in the set
     * @param defLang the language of the definitions in the set
     */
    @Override
    public void addSet(String title, String description, String termLang, String defLang) {
        QuizletSet set = quizlet.createSet(title, description, termLang, defLang);
        Intent intent = new Intent(this, SetActivity.class);
        intent.putExtra(SetActivity.SET, set);
        intent.putExtra(SetActivity.NEW, true);
        startActivity(intent);
    }

    /**
     * Edit a set and reload the set list
     * @param oldSet the set being edited
     * @param title the title of the set
     * @param description the description of the set
     * @param termLang the language of the terms in the set
     * @param defLang the language of the definitions in the set
     */
    @Override
    public void editSet(QuizletSet oldSet, String title, String description, String termLang, String defLang) {
        final List<Term> terms = quizlet.getSetTerms(oldSet.get_id());
        quizlet.deleteSet(oldSet);
        final QuizletSet newSet = quizlet.createSet(title, description, termLang, defLang);

        //if the user switched the term and definition languages, offer to switch the
        //terms and the definitions in the set
        if (!termLang.equals(oldSet.getLang_terms()) && !defLang.equals(oldSet.getLang_definitions())){
            new AlertDialog.Builder(this)
                    .setMessage(R.string.switch_languages)
                    .setCancelable(false)
                    .setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (Term term : terms)
                                quizlet.createTerm(newSet.get_id(), term.getDefinition(), term.getTerm());
                        }
                    })
                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            for (Term term : terms)
                                quizlet.createTerm(newSet.get_id(), term.getTerm(), term.getDefinition());
                        }
                    }).show();
        } else {
            for (Term term : terms)
                quizlet.createTerm(newSet.get_id(), term.getTerm(), term.getDefinition());
        }
        reloadSetList();
    }

    /**
     * Pulls sets from database and displays them in the list view
     */
    private void reloadSetList(){
        List<QuizletSet> setList = quizlet.getAllSets();
        SetAdapter adapter = new SetAdapter(this, R.layout.set_list_item, setList);
        sets.setAdapter(adapter);
    }

    /**
     * an ArrayAdapter that uses closures to handle clicks from multiple items
     * without the need for messy tags
     */
    private class SetAdapter extends ArrayAdapter<QuizletSet> {
        public SetAdapter(Context context, int resource, List<QuizletSet> sets) {
            super(context, resource, sets);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            final QuizletSet mySet = getItem(position);
            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.set_list_item, parent, false);
            TextView title = (TextView) myView.findViewById(R.id.title_tv);
            TextView description = (TextView) myView.findViewById(R.id.description_tv);
            ImageButton btn = (ImageButton) myView.findViewById(R.id.remove_button);
            View text_section = myView.findViewById(R.id.text_section);
            /**
             * remove the set if the remove button is clicked
             */
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    SetListActivity.this.remove(mySet);
                }
            });
            /**
             * On click open the study page, or the set page if there are no terms
             */
            text_section.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quizlet.open();
                    Intent intent;
                    //if the set has no terms, take the user to the edit page
                    if (quizlet.getSetTerms(mySet.get_id()).isEmpty())
                        intent = new Intent(SetListActivity.this, SetActivity.class);
                    else
                        intent = new Intent(SetListActivity.this, Study.class);
                    quizlet.close();
                    intent.putExtra(SetActivity.SET, mySet);
                    startActivity(intent);
                }
            });

            /**
             * On a long press, edit the current set
             */
            text_section.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    SetFragment setFragment = SetFragment.newInstance(mySet);
                    setFragment.show(fm, "Edit set");
                    return true;
                }
            });
            title.setText(mySet.getTitle());
            description.setText(mySet.getDescription());
            return myView;
        }
    }

    /**
     * make sure the user is prepared to delete a set then remove it
     * @param mySet the set to be removed
     */
    public void remove(final QuizletSet mySet) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_set)
                .setMessage(R.string.delete_set_prompt +" "+ mySet.getTitle() + "?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        quizlet.deleteSet(mySet);
                        List<QuizletSet> setList = quizlet.getAllSets();
                        SetAdapter adapter = new SetAdapter(SetListActivity.this, R.layout.set_list_item, setList);
                        sets.setAdapter(adapter);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false)
                .show();
    }
}
