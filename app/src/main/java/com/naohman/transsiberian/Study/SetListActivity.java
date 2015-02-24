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

public class SetListActivity extends ActionBarActivity {
    private ListView sets;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_list);
        sets = (ListView) findViewById(R.id.set_listview);
        Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        List<QuizletSet> setList = quizlet.getAllSets();
        SetAdapter adapter = new SetAdapter(this, R.layout.set_list_item, setList);
        sets.setAdapter(adapter);
    }

    public void createSet(View v){
        Intent intent = new Intent(this, NewSet.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set, menu);
        return true;
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

    public void itemSelected(View view) {
        QuizletSet mySet = (QuizletSet) view.findViewById(R.id.title_tv).getTag();
        Intent intent = new Intent(this, Study.class);
        intent.putExtra("set", mySet);
        startActivity(intent);
    }

    public void remove(View view) {
        final QuizletSet mySet = (QuizletSet) view.findViewById(R.id.remove_button).getTag();
        new AlertDialog.Builder(this)
                .setTitle("Delete set")
                .setMessage("Are you sure you want to delete " + mySet.getTitle())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
                        quizlet.deleteSet(mySet);
                        List<QuizletSet> setList = quizlet.getAllSets();
                        SetAdapter adapter = new SetAdapter(SetListActivity.this, R.layout.set_list_item, setList);
                        sets.setAdapter(adapter);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false)
                .show();
    }

    private class SetAdapter extends ArrayAdapter<QuizletSet> {
        public SetAdapter(Context context, int resource, List<QuizletSet> sets) {
            super(context, resource, sets);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.set_list_item, parent, false);
            TextView title = (TextView) myView.findViewById(R.id.title_tv);
            TextView description = (TextView) myView.findViewById(R.id.description_tv);
            ImageButton btn = (ImageButton) myView.findViewById(R.id.remove_button);
            QuizletSet mySet = getItem(position);
            title.setText(mySet.getTitle());
            title.setTag(mySet);
            description.setText(mySet.getDescription());
            description.setTag(mySet);
            btn.setTag(mySet);
            return myView;
        }
    }
}
