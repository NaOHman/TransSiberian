package com.naohman.language.transsiberian;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class Definition extends ActionBarActivity {
    public static final String QUERY_URL ="https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s&imgsz=medium";
    private List<Drawable> imgs;
    private int position = -2;
    private ImageSwitcher switcher;
    private ProgressBar pb;
    private String keyword;
    float initialX;
    private TextView tv_keyword;
    //include pair
    //quizlet integration
    //loading spinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definition);
        Intent intent = getIntent();
        keyword = intent.getStringExtra("keyword");
        imgs = new ArrayList<>();
        getImages(keyword);
        pb = (ProgressBar) findViewById(R.id.switcher_loading);
        tv_keyword = (TextView) findViewById(R.id.definition);
        tv_keyword.setText(keyword);
        tv_keyword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                say(keyword);
            }
        });
        switcher = (ImageSwitcher) findViewById(R.id.image_switcher);
        switcher.setVisibility(View.INVISIBLE);
        pb.setVisibility(View.VISIBLE);
        switcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView myView = new ImageView(getApplicationContext());
                myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                myView.setLayoutParams(new ImageSwitcher.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT,
                         ViewGroup.LayoutParams.MATCH_PARENT));
                return myView;
            }
        });
        switcher.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        float finalX = event.getX();
                        int minSwipe = switcher.getWidth() / 3;
                        if (Math.abs(finalX - initialX) > minSwipe) {
                            if (initialX > finalX) {
                                previous();
                            } else {
                                next();
                            }
                        }
                        break;
               }
               return true;
            }
        });
    }

    public void say(String text){
        if (!MyTTS.getInstance(null).say(text)) {
            Toast t = Toast.makeText(this, "Text To Speech is unavailable", Toast.LENGTH_LONG);
            t.show();
        }
    }

    public void next(){
        if (!imgs.isEmpty()) {
            position = position == imgs.size() - 1 ? 0 : position + 1;
            Animation in = AnimationUtils.loadAnimation(this,
                    android.R.anim.slide_in_left);
            Animation out = AnimationUtils.loadAnimation(this,
                    android.R.anim.slide_out_right);
            switcher.setInAnimation(in);
            switcher.setOutAnimation(out);
            switcher.setImageDrawable(imgs.get(position));
        }
    }
    public void previous(){
        if (!imgs.isEmpty()) {
            position = position == 0 ? imgs.size() - 1 : position - 1;
            Animation in = AnimationUtils.loadAnimation(this,
                    R.anim.slide_in_right);
            Animation out = AnimationUtils.loadAnimation(this,
                    R.anim.slide_out_left);
            switcher.setInAnimation(in);
            switcher.setOutAnimation(out);
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
            Log.e("CONNECTIVITY", "Not Connected to Internet");
        }
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
                next();
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            switcher.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
            Log.d("Images Fetched", "Found " +imgs.size() + " images");
        }
    }
}
