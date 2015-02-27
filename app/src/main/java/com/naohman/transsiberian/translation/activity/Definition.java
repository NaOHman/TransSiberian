package com.naohman.transsiberian.translation.activity;

import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.quizlet.Quizlet;
import com.naohman.transsiberian.quizlet.QuizletSet;
import com.naohman.transsiberian.quizlet.SetFragment;
import com.naohman.transsiberian.quizlet.Term;
import com.naohman.transsiberian.quizlet.TermFragment;
import com.naohman.transsiberian.setUp.SetUpManager;
import com.naohman.transsiberian.translation.util.MyTTS;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Jeffrey Lyman
 * An activity for displaying a chosen keyword-definition pair
 * Shows the results of google image searches for the keyword
 */
public class Definition extends ActionBarActivity implements View.OnClickListener,
        ViewSwitcher.ViewFactory, View.OnTouchListener, AdapterView.OnItemClickListener,
        TermFragment.NewTermListener, SetFragment.NewSetListener {
    //TODO subtly display google logo
    private static final String QUERY_URL ="https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s&imgsz=medium";
    private List<Drawable> imgs = new ArrayList<>();
    private int position = -2, minSwipe;
    private ImageSwitcher switcher;
    private TextView no_images;
    private ProgressBar pb;
    private String keyword, definition;
    private ListView lv_definitions;
    private QuizletSet mySet;
    private float initialX;
    private Quizlet quizlet;
    private static Animation lIn, lOut, rIn, rOut;
    private TextView tv_keyword;
    private FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_definition);
        SetUpManager sMgr = new SetUpManager();
        sMgr.loadTTS(getApplicationContext());
        pb = (ProgressBar) findViewById(R.id.switcher_loading);
        pb.setVisibility(View.VISIBLE);
        no_images = (TextView) findViewById(R.id.no_images);
        no_images.setVisibility(View.INVISIBLE);
        switcher = (ImageSwitcher) findViewById(R.id.image_switcher);
        switcher.setVisibility(View.INVISIBLE);
        switcher.setFactory(this);
        switcher.setOnTouchListener(this);
        minSwipe = switcher.getWidth() / 3;
        makeAnimations();
        setMeanings();
        getImages(keyword);
        quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
    }

    /**
     * Pull the keyword /meaning list from the intent and update the ui accordingly
     */
    private void setMeanings(){
        Intent intent = getIntent();
        tv_keyword = (TextView) findViewById(R.id.definition);
        lv_definitions = (ListView) findViewById(R.id.definition_lv);
        keyword = intent.getStringExtra("keyword");
        List<String> meanings = Arrays.asList(intent.getStringArrayExtra("meanings"));
        tv_keyword.setOnClickListener(this);
        tv_keyword.setText(keyword);
        lv_definitions.setAdapter(new MeaningListAdapter(this, R.layout.translation_tv, meanings));
        lv_definitions.setOnItemClickListener(this);
    }

    /**
     * load the animations used by the switcher
     */
    private void makeAnimations(){
        lIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        lOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        rIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        rOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
    }

    /**
     * When the keyword is clicked say the word
     * todo tts icon?
     * @param v the view containing the keyword
     */
    @Override
    public void onClick(View v){
        //Loading the TTS can take a while so it's loaded asynchronously
        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                return MyTTS.getInstance(getApplicationContext()).say(params[0]);
            }
            @Override
            protected void onPostExecute(Boolean success) {
                if (!success){
                    Toast t=Toast.makeText(Definition.this,
                            "Text To Speech is unavailable",Toast.LENGTH_LONG);
                    t.show();
                }
            }
        }.execute(keyword);
    }

    /**
     * a view factory for the image switcher
     * @return a plain imageview
     */
    @Override
    public View makeView() {
        ImageView myView = new ImageView(getApplicationContext());
        myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        myView.setLayoutParams(new ImageSwitcher.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT));
        return myView;
    }

    /**
     * When the image switcher is dragged change the picture
     * @param v the view being touched
     * @param event the motion event
     * @return whether the event was handled
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            initialX = event.getX();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            float finalX = event.getX();
            if (Math.abs(finalX - initialX) > minSwipe) {
                changeImage(initialX < finalX);
            }
        }
        return true;
    }

    /**
     * change the image shown by the Image switcher,
     * @param next whether to load the image from the left or the right
     */
    private void changeImage(boolean next){
        if (!imgs.isEmpty()) {
            if (next) {
                position = position == imgs.size() - 1 ? 0 : position + 1;
                switcher.setInAnimation(lIn);
                switcher.setOutAnimation(lOut);
            } else {
                position = position == 0 ? imgs.size() - 1 : position - 1;
                switcher.setInAnimation(rIn);
                switcher.setOutAnimation(rOut);
            }
            switcher.setImageDrawable(imgs.get(position));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_definition, menu);
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

    /**
     * pull a list of images of google image search
     * @param keyword the images to search
     */
    private void getImages(String keyword){
        ConnectivityManager cManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            new FetchImage().execute(keyword);
        } else {
            pb.setVisibility(View.INVISIBLE);
            no_images.setVisibility(View.VISIBLE);
        }
    }

    /**
     * When a meaning is clicked
     * @param parent the listView
     * @param view the child view
     * @param position the position of the child in the list
     * @param id the id of the child
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        definition = (String) parent.getItemAtPosition(position);
        final List<QuizletSet> sets = quizlet.getAllSets();
        String[] titles = new String[sets.size()];
        for (int i=0; i<sets.size(); i++)
            titles[i] = sets.get(i).getTitle();
        new AlertDialog.Builder(this)
                .setTitle(R.string.create_term)
                .setItems(titles, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mySet = sets.get(which);
                        TermFragment termFragment  = TermFragment.newInstance(keyword, definition);
                        termFragment.show(fm, "new term");
                    }
                })
                .setPositiveButton(R.string.new_set, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SetFragment setFragment = SetFragment.newInstance();
                        setFragment.show(fm, "new set");
                    }
                })
                .setNegativeButton(R.string.cancel, null).show();
    }

    @Override
    public void addSet(String title, String description, String termLang, String defLang) {
        mySet = quizlet.createSet(title, description, termLang, defLang);
        TermFragment termFragment  = TermFragment.newInstance(keyword, definition);
        termFragment.show(fm, "new term");
    }


    @Override
    public void addTerm(String term, String definition) {
        Log.d("Adding set", "here be dragons");
        quizlet.createTerm(mySet.get_id(), term, definition);
    }


    /**
     * a private class that fetches the google image results
     */
    private class FetchImage extends AsyncTask<String, Drawable, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                URI address = new URI(String.format(QUERY_URL, Uri.encode(params[0])));
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(address);
                HttpResponse res = httpClient.execute(request);
                InputStream inStream = res.getEntity().getContent();
                String json = IOUtils.toString(inStream);
                JSONObject jObj = new JSONObject(json);
                JSONArray objs = jObj.getJSONObject("responseData").getJSONArray("results");
                for(int i=0; i<objs.length(); i++){
                    URI uri = new URI(objs.getJSONObject(i).getString("url"));
                    HttpGet imgGet = new HttpGet();
                    imgGet.setURI(uri);
                    HttpResponse imgRes = httpClient.execute(imgGet);
                    InputStream imgStream = imgRes.getEntity().getContent();
                    BitmapDrawable img = new BitmapDrawable(getResources(), imgStream);
                    publishProgress(img);
                }
                return null;
            } catch (Exception e) {
                Log.e("FETCH IMAGE", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Drawable... values) {
            imgs.addAll(Arrays.asList(values));
            if (position == -2){
                position = -1;
                switcher.setVisibility(View.VISIBLE);
                pb.setVisibility(View.INVISIBLE);
                changeImage(true);
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            switcher.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
            Log.d("Images Fetched", "Found " +imgs.size() + " images");
        }
    }

    /**
     * a private class for displaying meanings in a list view
     */
    private class MeaningListAdapter extends ArrayAdapter<String> {
        public MeaningListAdapter(Context context, int resource, List<String> meanings) {
            super(context, resource, meanings);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.translation_tv, parent, false);
            TextView translation = (TextView) myView.findViewById(R.id.tv_translation);
            String meaning = getItem(position);
//            Log.d("Meaning", meaning);
            translation.setText(meaning);
            return myView;
        }
    }

    @Override
    public void editSet(QuizletSet oldSet, String title, String description, String termLang, String defLang) {}
    @Override
    public void editTerm(Term oldTerm, String term, String definition) {}
}
