package com.naohman.language.transsiberian;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

/**
 * Created by jeffrey on 1/19/15.
 */
public class Splash extends Activity implements ViewSwitcher.ViewFactory{
    private ProgressBar pb_loading;
    private ImageSwitcher switcher;
    final int[] images = {R.drawable.car2, R.drawable.car1, R.drawable.car4, R.drawable.car3};
    int lastImage = 0;
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.splash);
        SetUpManager.getInstance(getApplicationContext());
        pb_loading = (ProgressBar) findViewById(R.id.loading);
        switcher= (ImageSwitcher) findViewById(R.id.train_switcher);
        switcher.setFactory(this);
        pb_loading.setIndeterminate(true);
        pb_loading.setVisibility(View.VISIBLE);
        Animation in = AnimationUtils.loadAnimation(this, R.anim.train_in);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.train_out);
        switcher.setInAnimation(in);
        switcher.setOutAnimation(out);
        switcher.setImageResource(R.drawable.color_train);
    }

    public Runnable nextRunnable(){
        if (lastImage >= images.length){
            return new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Splash.this, Translate.class);
                    startActivity(intent);
                }
            };
        } else {
            final int i = lastImage;
            lastImage++;
            return new Runnable(){
                @Override
                public void run() {
                    switcher.setImageResource(images[i]);
                }
            };
        }
    }

    @Override
    public View makeView() {
        ImageView myView = new AnimatedImageView(this);
        myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        myView.setLayoutParams(new ImageSwitcher.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT));
        return myView;
    }

    private class AnimatedImageView extends ImageView {
        public AnimatedImageView(Context c){
            super(c);
        }
        @Override
        public void onAnimationEnd(){
            nextRunnable().run();
        }
    }
}
