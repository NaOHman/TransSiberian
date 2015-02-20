package com.naohman.language.transsiberian.Activities;

import android.content.Context;
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

import com.naohman.language.transsiberian.Singletons.MyTTS;
import com.naohman.language.transsiberian.R;
import com.naohman.language.transsiberian.Singletons.SetUpManager;

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

/*
 * Created by Jeffrey Lyman
 * An activity for displaying a chosen keyword-definition pair
 * Shows the results of google image searches for the keyword
 */
public class Definition extends ActionBarActivity implements View.OnClickListener,
        ViewSwitcher.ViewFactory, View.OnTouchListener, AdapterView.OnItemClickListener {
    public static final String QUERY_URL ="https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s&imgsz=medium";
    private List<Drawable> imgs = new ArrayList<>();
    private int position = -2, minSwipe;
    private ImageSwitcher switcher;
    private TextView no_images;
    private ProgressBar pb;
    private String keyword;
    private ListView lv_definitions;
    float initialX;
    private static Animation lIn, lOut, rIn, rOut;
    private TextView tv_keyword;

    //TODO add look up function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definition);
        SetUpManager sMgr = SetUpManager.getInstance();
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
    }

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

    private void makeAnimations(){
        lIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        lOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        rIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        rOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
    }

    @Override
    public void onClick(View v){
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

    @Override
    public View makeView() {
        ImageView myView = new ImageView(getApplicationContext());
        myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        myView.setLayoutParams(new ImageSwitcher.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT));
        return myView;
    }

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

    /*
     * change the image shown by the Image switcher, if next is true the image
     * will come in from the left, otherwise it will come in from the left
     */
    public void changeImage(boolean next){
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String word = (String) parent.getItemAtPosition(position);
        Intent intent = new Intent(this, NewTermOrSet.class);
        intent.putExtra("term", keyword);
        intent.putExtra("definition", word);
        startActivity(intent);
    }

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
}
