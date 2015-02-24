package com.naohman.transsiberian.Translation.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.naohman.transsiberian.Quizlet.QuizletSet;
import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.Quizlet.Quizlet;
import com.naohman.transsiberian.Study.NewSet;

import java.util.List;

public class NewTermOrSet extends ActionBarActivity implements AdapterView.OnItemClickListener, Dialog.OnClickListener {
    private ListView setView;
    private EditText term, def;
    private QuizletSet selectedSet;
    private List<QuizletSet> sets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_term_or_set);
        setView = (ListView) findViewById(R.id.set_lv);
        term = (EditText) findViewById(R.id.enter_term);
        def = (EditText) findViewById(R.id.enter_definition);
        Intent intent = getIntent();
        term.setText(intent.getStringExtra("term"));
        def.setText(intent.getStringExtra("definition"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        sets = quizlet.getAllSets();
        setView.setAdapter(new SetListAdapter(this, R.layout.set_list_item, sets));
        setView.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_term_or_set, menu);
        return true;
    }

    public void swap(View v){
        CharSequence termEntry = term.getText();
        term.setText(def.getText());
        def.setText(termEntry);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void newSet(View v){
        Intent intent = new Intent(this, NewSet.class);
        intent.putExtra("finish", true);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedSet = (QuizletSet) setView.getItemAtPosition(position);
        new AlertDialog.Builder(this).setTitle("Flashcard Created")
                .setMessage("Add term to set " + selectedSet.getTitle())
                .setNegativeButton("Cancel", this)
                .setPositiveButton("Okay", this).show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == Dialog.BUTTON_POSITIVE) {
            dialog.dismiss();
            if (selectedSet != null) {
                String t = term.getText().toString();
                String d = def.getText().toString();
                Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
                quizlet.open();
                quizlet.createTerm(selectedSet.get_id(), t, d);
                finish();
            }
        } else {
            selectedSet = null;
            dialog.dismiss();
        }
    }

    private class SetListAdapter extends ArrayAdapter<QuizletSet> {
        public SetListAdapter(Context context, int resource, List<QuizletSet> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(R.layout.set_selector, parent, false);
            QuizletSet set = getItem(position);
            ((TextView) v.findViewById(R.id.title_tv)).setText(set.getTitle());
            ((TextView) v.findViewById(R.id.description_tv)).setText(set.getDescription());
            return v;
        }
    }
}
