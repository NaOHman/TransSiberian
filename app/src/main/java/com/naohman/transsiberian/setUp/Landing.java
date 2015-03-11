package com.naohman.transsiberian.setUp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.naohman.transsiberian.exchange.Exchange;
import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.study.Authenticate;
import com.naohman.transsiberian.translation.activity.Translate;

/**
 * Created by Jeffrey Lyman
 * The landing activity that allows users to pick what they want to do with the app
 */
public class Landing extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        findViewById(R.id.translate).setOnClickListener(this);
        findViewById(R.id.exchange).setOnClickListener(this);
        findViewById(R.id.study).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()){
            case R.id.translate:
                intent = new Intent(Landing.this, Translate.class);
                break;
            case R.id.exchange:
                intent = new Intent(Landing.this, Exchange.class);
                break;
            case R.id.study:
                intent = new Intent(Landing.this, Authenticate.class);
                break;
        }
        if (intent != null)
            startActivity(intent);
    }
}
