package com.naohman.transsiberian.translation.activity;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.setUp.SetUpManager;
import com.naohman.transsiberian.translation.util.DictEntry;
import com.naohman.transsiberian.translation.util.DictionaryHandler;
import com.naohman.transsiberian.translation.util.SpanListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Jeffrey Lyman
 * An activity that allows users translate words between english and russian
 */
public class Translate extends ActionBarActivity implements
        SpanListener, SearchView.OnQueryTextListener {
    private ListView lv_translation;
    private ProgressBar pb_loading;
    private Stack<TranslationListAdapter> previous = new Stack<>();
    private TranslationListAdapter current = null;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_translate);
        SetUpManager sMgr = SetUpManager.getInstance();
        sMgr.loadDictionary();
        sMgr.loadRusMorphology();
        sMgr.loadEngMorphology();
        sMgr.loadTTS();
        pb_loading = (ProgressBar) findViewById(R.id.translations_loading);
        pb_loading.setVisibility(View.INVISIBLE);
        lv_translation = (ListView) findViewById(R.id.lv_translation);
    }

    /**
     * Find the translations of a given word and display to the user
     * This can take a long time depending on which objects have been loaded
     * so it is executed asynchronously
     * @param keyword the search query
     */
    public void makeTranslations(String keyword) {
        new AsyncTask<String,Void,List<DictEntry>>(){
            /**
             * Display a loading spinner and hide previous translations
             */
            @Override
            protected void onPreExecute(){
                pb_loading.setVisibility(View.VISIBLE);
                lv_translation.setVisibility(View.GONE);
            }

            /**
             * @param params The search query
             * @return a list of results for the query
             */
            @Override
            protected List<DictEntry> doInBackground(String... params) {
                List<DictEntry> entries;
                DictionaryHandler dictionary = DictionaryHandler.getInstance();
                dictionary.open();
                entries = dictionary.getTranslations(params[0]);
                for (DictEntry entry: entries){
                    entry.setSpanListener(Translate.this);
                }
                return entries;
            }

            /**
             * Remove the loading Spinner display results
             * @param entries the results of the query
             */
            @Override
            protected void onPostExecute(List<DictEntry> entries){
                pb_loading.setVisibility(View.GONE);
                lv_translation.setVisibility(View.VISIBLE);
                if (current != null)
                    previous.push(current);
                setTranslation(entries);
            }
        }.execute(keyword);
    }

    /**
     * @param translations a list of query results to be shown to the user
     */
    public void setTranslation(List<DictEntry> translations){
        if (translations == null || translations.size() == 0){
            List<DictEntry> entries = new ArrayList<>();
            entries.add(new DictEntry());
            current = new TranslationListAdapter(this, entries);
        } else {
            current = new TranslationListAdapter(this, translations);
        }
        lv_translation.setAdapter(current);
        lv_translation.setSelectionAfterHeaderView();
    }

    /**
     * called when a user clicks on a reference link
     * @param v the view holding the link
     * @param entry the dict entry containing the word
     * @param word the word itself
     */
    @Override
    public void onReferenceClick(View v, DictEntry entry, String word){
        makeTranslations(word);
    }

    /**
     * called when a user clicks a definition link
     * @param v the view holding the link
     * @param entry the dict entry containing the word
     * @param word the word itself
     */
    @Override
    public void onDefinitionClick(View v, DictEntry entry, String word){
        Intent startDef = new Intent(this, Definition.class);
        startDef.putExtra("keyword", word);
        startDef.putExtra("meanings", new String[] {entry.getKeyword()});
        startActivity(startDef);
    }

    /**
     * called when a user clicks a keyword
     * @param v the view holding the link
     * @param entry the entry containing the word
     */
    @Override
    public void onKeywordClick(View v, DictEntry entry){
        Intent startDef = new Intent(this, Definition.class);
        startDef.putExtra("keyword", entry.getKeyword());
        startDef.putExtra("meanings", entry.getDefinitions());
        startActivity(startDef);
    }

    /**
     * show previous translations or finish the activity
     */
    @Override
    public void onBackPressed(){
        if (previous.isEmpty()){
            this.finish();
        } else {
            current = previous.pop();
            lv_translation.setAdapter(current);
            lv_translation.setSelectionAfterHeaderView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_translate, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        menu.findItem(R.id.translate).expandActionView();
        mSearchView =  (SearchView) menu.findItem(R.id.translate).getActionView();
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint(getString(R.string.translation_hint));
        mSearchView.requestFocus();
        if (searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null
                        && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }
            mSearchView.setSearchableInfo(info);
        }

        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        makeTranslations(s);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        //TODO provide suggestions
        return false;
    }

    /**
     * a custom list adapter for displaying translations
     */
    private class TranslationListAdapter extends ArrayAdapter<DictEntry> {
        public TranslationListAdapter(Context context, List<DictEntry> entries) {
            super(context, R.layout.translation_tv, entries);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.translation_tv, parent, false);
            TextView translation = (TextView) myView.findViewById(R.id.tv_translation);
            DictEntry entry = getItem(position);
            translation.setText(entry.getSpanned());
            translation.setMovementMethod(LinkMovementMethod.getInstance());
            return myView;
        }
    }
}
