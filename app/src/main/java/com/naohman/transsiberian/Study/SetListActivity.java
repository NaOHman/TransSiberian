package com.naohman.transsiberian.Study;

import android.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.naohman.transsiberian.Quizlet.QuizletSet;
import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.Quizlet.Quizlet;

import java.util.List;

/**
 * Created By Jeffrey Lyman
 * an activity that displays all available sets and allows users to pick one to study
 */
public class SetListActivity extends ActionBarActivity {
    private ListView sets;
    private Quizlet quizlet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume(){
        super.onResume();
        sets = (ListView) findViewById(R.id.set_listview);
        quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        List<QuizletSet> setList = quizlet.getAllSets();
        SetAdapter adapter = new SetAdapter(this, R.layout.set_list_item, setList);
        sets.setAdapter(adapter);
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
            Intent intent = new Intent(this, NewSet.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    SetListActivity.this.remove(mySet);
                }
            });
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
                    intent.putExtra("set", mySet);
                    startActivity(intent);
                }
            });
            title.setText(mySet.getTitle());
            description.setText(mySet.getDescription());
            return myView;
        }
    }

    public void remove(final QuizletSet mySet) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_set))
                .setMessage(getString(R.string.delete_set_prompt) +" "+ mySet.getTitle())
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        quizlet.deleteSet(mySet);
                        List<QuizletSet> setList = quizlet.getAllSets();
                        SetAdapter adapter = new SetAdapter(SetListActivity.this, R.layout.set_list_item, setList);
                        sets.setAdapter(adapter);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false)
                .show();
    }
}
