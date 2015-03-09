package com.naohman.transsiberian.exchange;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.net.URISyntaxException;

/**
 * Created by Jeffrey Lyman
 * An activity that performs currency exchanges
 */
public class Exchange extends ActionBarActivity implements TextView.OnEditorActionListener {
    //TODO add other currency options
    private final static String CURRENCTY_URL = "http://www.freecurrencyconverterapi.com/api/v3/convert?q=";
    private final static String EURO = "EUR";
    private final static String RUBLE = "RUB";
    private final static String DOLLAR = "USD";
    private final static String YEN = "JPY";
    private final static String POUND = "GBP";
    private final static String SEP = "_";
    private final static String LAST_CURRENCY = "last currency";
    private static final int ANIM_DURATION = 500;
    private static long hour = 3600000;
    private static long day = 86400000;

    private boolean swapped = false;
    private float animDistance, animHeight;
    private float[] rates;
    private QuadraticAnimation toRight, toLeft;
    private Menu menu;
    private int whichRate = 0;
    private EditText currency_et;
    private ImageView swap;
    private SharedPreferences prefs;
    private String currentCurrency;
    private TextView foreign, currency_tv, ruble;
    private LinearLayout tv_container;
    private ProgressBar loading;

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
        tv_container = (LinearLayout) findViewById(R.id.tv_container);
        loading = (ProgressBar) findViewById(R.id.loading);
        prefs = this.getPreferences(Context.MODE_PRIVATE);
        currentCurrency = prefs.getString(LAST_CURRENCY, DOLLAR);
        //todo update menu
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
        int id = item.getItemId();

        if (id == R.id.currency) {
            //Todo switch currency
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void getExchange(){
        long elapsed = System.currentTimeMillis() - prefs.getLong(currentCurrency,0);
        rates[0] = prefs.getFloat(currentCurrency+SEP+RUBLE, 0f);
        rates[1] = prefs.getFloat(RUBLE+SEP+currentCurrency, 0f);
        //If the rate is less three hours old, don't bother updating it
        if (elapsed < 3*hour)
            return;
        ConnectivityManager cManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            new ExchangeFetcher().execute();
        } else {
            //If the exchange rate is more than a day old, warn the user
            if (elapsed != 0) {
                String days = "" + ((int) elapsed / day);
                String plural = elapsed > day * 2 ? "s": "";
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.warning))
                        .setMessage(String.format(getString(R.string.old_currency), days, plural))
                        .setPositiveButton(R.string.use_old, null)
                        .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getExchange();
                            }
                        })
                        .show();
            } else {
                //If there is no currency disable exchange and alert the user
                tv_container.setVisibility(View.INVISIBLE);
                new AlertDialog.Builder(this)
                        .setMessage(R.string.no_internet)
                        .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getExchange();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
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
        if (rates[whichRate] == 0.0 || base.matches("\\s*")){
            currency_tv.setText("");
        } else {
            float baseRate  = Float.parseFloat(base);
            if (swapped){
                currency_tv.setText("" + (baseRate / rates[whichRate]));
            } else {
                currency_tv.setText("" + (baseRate * rates[whichRate]));
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
    private class ExchangeFetcher extends AsyncTask<Void,Void,float[]>{
        @Override
        protected void onPreExecute(){
            tv_container.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected float[] doInBackground(Void... params) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                float[] rates = new float[2];
                rates[0] = fetchRate(httpClient, currentCurrency + SEP + RUBLE);
                rates[1] = fetchRate(httpClient, RUBLE + SEP + currentCurrency);
                return rates;
            } catch (Exception e) {
                Log.e("Error fetching currency", e.getMessage());
            }
            return new float[0];
        }

        @Override
        protected void onPostExecute(float[] newRates) {
            tv_container.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
            //This probably means something out of the user's control happened
            if (newRates.length == 0){
                new AlertDialog.Builder(Exchange.this)
                        .setMessage(R.string.fetch_error)
                        .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getExchange();
                            }
                        }).setNegativeButton(R.string.cancel, null)
                        .show();
                //no currency data, disable exchange
                if (rates[0] == 0)
                    tv_container.setVisibility(View.INVISIBLE);
            } else {
                Exchange.this.rates = newRates;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat(currentCurrency+SEP+RUBLE, rates[0]);
                editor.putFloat(RUBLE+SEP+currentCurrency, rates[1]);
                editor.putLong(currentCurrency, System.currentTimeMillis());
                editor.apply();
            }
        }
    }

    private static float fetchRate(HttpClient client, String query) throws Exception {
        try {
            URI address = new URI(CURRENCTY_URL + query);
            HttpGet request = new HttpGet();
            request.setURI(address);
            HttpResponse res = client.execute(request);
            InputStream inStream = res.getEntity().getContent();
            String json = IOUtils.toString(inStream);
            JSONObject jObj = new JSONObject(json);
            return (float) jObj.getJSONObject("results")
                    .getJSONObject(query)
                    .getDouble("val");
        } catch (URISyntaxException e) {
            Log.e("Bad URI Syntax", e.getMessage());
            return 0;
        }
    }
}
