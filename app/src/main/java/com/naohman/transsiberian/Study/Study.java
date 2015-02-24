package com.naohman.transsiberian.Study;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.naohman.transsiberian.Quizlet.QuizletSet;
import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.Quizlet.Quizlet;

public class Study extends ActionBarActivity implements View.OnClickListener, View.OnTouchListener {
    private TextView set_title, set_description;
    private AnimatorSet flipOut, flipIn;
    private Menu menu;
    private float startingX, initialX, initialY, minSwipe;
    private RelativeLayout holder;
    private ImageView correct, incorrect;
    private int current = 0;
    private QuizletSet mySet;
    private FlashCardManager mgr;
    private static Animation downIn, lOut, rOut;
    private boolean frontFirst = true, flip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        holder = (RelativeLayout) findViewById(R.id.flashcard_holder);
        incorrect = (ImageView) findViewById(R.id.incorrect_img);
        correct = (ImageView) findViewById(R.id.correct_img);
        mySet = (QuizletSet) getIntent().getSerializableExtra("set");
        set_title = (TextView) findViewById(R.id.set_title);
        set_title.setText(mySet.getTitle());
        set_description = (TextView) findViewById(R.id.set_description);
        set_description.setText(mySet.getDescription());
        Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mgr = new FlashCardManager(quizlet.getSetTerms(mySet.get_id()), frontFirst);
        holder.addView(mgr.getView(this, holder, getLayoutInflater()));
        lOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        rOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        downIn = AnimationUtils.loadAnimation(this, R.anim.slide_down_in);
        flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flip_left_out);
        flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flip_left_in);
    }

    @Override
    public void onClick(View v) {
        View next = mgr.flipView(this, holder, getLayoutInflater());
        flipOut.setTarget(v);
        flipIn.setTarget(next);
        holder.addView(next);
        flipOut.start();
        flipIn.start();
    }

    public void reset(){
        holder.removeAllViews();
        View score = getLayoutInflater().inflate(R.layout.set_complete, holder);
        ((TextView) score.findViewById(R.id.completed_set_name)).setText(mySet.getTitle());
        ((TextView) score.findViewById(R.id.score)).
                setText("Round: " + mgr.getRoundScore() +
                        "\nTotal: " + mgr.getTotalScore());
        if (mgr.isFinished())
            score.findViewById(R.id.next_round).setVisibility(View.GONE);
    }

    public void nextRound(View v){
        mgr.nextRound();
        holder.removeAllViews();
        holder.addView(mgr.getView(this,holder,getLayoutInflater()));
    }

    public void restart(View v){
        mgr.reset();
        holder.removeAllViews();
        holder.addView(mgr.getView(this,holder,getLayoutInflater()));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startingX = v.getX();
            minSwipe = v.getWidth() / 3;
            initialX = event.getRawX();
            initialY = event.getRawY();
            flip = true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE){
            float dx = event.getRawX() - initialX;
            v.setX(startingX + dx);
            Log.d("Dx", "=" + Math.abs(dx));
            if (Math.abs(dx) > minSwipe/3)
                flip = false;
            incorrect.setAlpha(Math.min(1.0f, Math.abs(dx)/minSwipe));
            correct.setAlpha(Math.min(1.0f, Math.abs(dx)/minSwipe));
        }else if (event.getAction() == MotionEvent.ACTION_UP) {
            float dx = event.getRawX() - initialX;
            float dy = event.getRawY() - initialY ;
            if(Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > minSwipe) {
                incorrect.setAlpha(0.0f);
                correct.setAlpha(0.0f);
                if (dx < 0) {
                    v.startAnimation(rOut);
                    mgr.next();
                } else {
                    v.startAnimation(lOut);
                    mgr.save();
                }
                if (mgr.roundComplete()){
                    reset();
                    return true;
                }
                View newView = mgr.getView(this, holder, getLayoutInflater());
                holder.addView(newView);
                newView.startAnimation(downIn);
                holder.removeView(v);
                return true;
            } else {
                incorrect.setAlpha(0.0f);
                correct.setAlpha(0.0f);
                v.setX(startingX);
                return !flip;
            }
        }
        return false;
    }

    public void edit(View v){
        Intent intent = new Intent(this, SetActivity.class);
        intent.putExtra("set", mySet);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_study, menu);
        this.menu = menu;
        setLangIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_edit:
                edit(null);
                return true;
            case R.id.action_restart:
                mgr.reset();
                holder.removeAllViews();
                holder.addView(mgr.getView(this, holder, getLayoutInflater()));
                return true;
            case R.id.action_lang:
                frontFirst = !frontFirst;
                setLangIcon();
                mgr.changeFirst();
                holder.removeAllViews();
                holder.addView(mgr.getView(this, holder, getLayoutInflater()));
                return true;
            case R.id.action_settings:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLangIcon(){
        if (frontFirst ^ mySet.getLang_terms() == Quizlet.RUSSIAN){
            menu.getItem(0).setIcon(R.drawable.my_ya);
            menu.getItem(0).setTitle("Russian First");
        } else {
            menu.getItem(0).setIcon(R.drawable.my_r);
            menu.getItem(0).setTitle("English First");
        }
    }
}
