package com.naohman.transsiberian.Exchange;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.naohman.language.transsiberian.R;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
/**
 * Created by Jeffrey Lyman
 * An activity that performs currency exchanges between Dollars and Rubles
 */
public class Exchange extends ActionBarActivity implements TextView.OnEditorActionListener {
    //TODO warn user if exchange rate is too old
    //TODO alert when no currency has been downloaded
    //TODO add other currency options
    private final static String CURRENCTY_URL = "http://www.freecurrencyconverterapi.com/api/v3/convert?q=USD_RUB";
    private float exchange;
    private static final int ANIM_DURATION = 500;
    private QuadraticAnimation toRight, toLeft;
    private ImageView swap;
    private boolean swapped = false;
    private float animDistance, animHeight;
    private Menu menu;
    private EditText currency_et;
    private SharedPreferences prefs;
    private TextView foreign, currency_tv, ruble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);
        currency_et = (EditText) findViewById(R.id.currency_et);
        currency_et.setOnEditorActionListener(this);
        foreign = (TextView) findViewById(R.id.input_label);
        ruble = (TextView) findViewById(R.id.output_label);
        currency_tv = (TextView) findViewById(R.id.currency_tv);
        swap = (ImageView) findViewById(R.id.swap);
        prefs = this.getPreferences(Context.MODE_PRIVATE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getExchange();

        //Set up Animations
        toRight = new QuadraticAnimation();
        toRight.setDuration(ANIM_DURATION);
        toRight.setFillAfter(true);
        toLeft = new QuadraticAnimation();
        toLeft.setDuration(ANIM_DURATION);
        toLeft.setFillAfter(true);
        ViewTreeObserver vto = foreign.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                animDistance = foreign.getX() - ruble.getX();
                animHeight = foreign.getHeight() / 2;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exchange, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.currency) {
            //Todo switch currency
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void getExchange(){
        exchange = prefs.getFloat("exchange", 0.0f);
        ConnectivityManager cManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            new ExchangeFetcher().execute();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("exchange", exchange);
            editor.apply();
        }
    }

    /**
     * a function that switches the input currency and ouput currency
     */
    public void swap(View v){
        toRight.setPath(-animDistance, -animHeight, swapped);
        toLeft.setPath(animDistance, animHeight, swapped);
        foreign.startAnimation(toRight);
        ruble.startAnimation(toLeft);
        swapped = !swapped;
        convert(null);
    }

    /**
     * a function that converts between the input and output currencies
     */
    public void convert(View v){
        String base = currency_et.getText().toString();
        if (exchange == 0.0 || base.matches("\\s*")){
            currency_tv.setText("");
            //Todo explain what's up
        } else {
            float baseRate  = Float.parseFloat(base);
            if (swapped){
                currency_tv.setText("" + (baseRate / exchange));
            } else {
                currency_tv.setText("" + (baseRate * exchange));
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO){
            convert(null);
            return true;
        }
        return false;
    }

    /**
     * A private class that attempts to pull currency exchange data from the internet
     */
    private class ExchangeFetcher extends AsyncTask<Void,Void,Float>{
        @Override
        protected Float doInBackground(Void... params) {
            Float rate = 0.0f;
            try {
                URI address = new URI(CURRENCTY_URL);
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(address);
                HttpResponse res = httpClient.execute(request);
                InputStream inStream = res.getEntity().getContent();
                String json = IOUtils.toString(inStream);
                JSONObject jObj = new JSONObject(json);
                rate = (float) jObj.getJSONObject("results")
                        .getJSONObject("USD_RUB")
                        .getDouble("val");
            } catch (Exception e) {}
            return rate;
        }

        @Override
        protected void onPostExecute(Float v) {
            Exchange.this.exchange = v;
        }
    }
}
