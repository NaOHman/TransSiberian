package com.naohman.transsiberian.study;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.naohman.transsiberian.quizlet.QuizletSet;
import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.quizlet.Quizlet;
import com.naohman.transsiberian.quizlet.Term;

import java.util.List;

public class Study extends ActionBarActivity implements View.OnTouchListener, Animation.AnimationListener, Animator.AnimatorListener {
    //TODO mid flip bug, move from location with appropriate time,
    private TextView set_title, set_description;
    private AnimatorSet flipOut, flipIn;
    private Menu menu;
    private float startingX, initialX, initialY, minSwipe;
    private RelativeLayout holder;
    private ImageView correct, incorrect;
    private int animationCount = 0;
    private QuizletSet mySet;
    private FlashCardManager mgr;
    private static Animation downIn, lOut, rOut;
    private GestureDetector gestureDetector;
    private boolean frontFirst = true, flip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        holder = (RelativeLayout) findViewById(R.id.flashcard_holder);
        incorrect = (ImageView) findViewById(R.id.incorrect_img);
        correct = (ImageView) findViewById(R.id.correct_img);
        mySet = (QuizletSet) getIntent().getSerializableExtra("set");
        if (mySet == null)
            bail();
        set_title = (TextView) findViewById(R.id.set_title);
        set_title.setText(mySet.getTitle());
        set_description = (TextView) findViewById(R.id.set_description);
        set_description.setText(mySet.getDescription());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        setUpAnimations();
        findViewById(R.id.title_box).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                study();
            }
        });
    }

    public void setUpAnimations(){
        lOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        rOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        lOut.setAnimationListener(this);
        rOut.setAnimationListener(this);
        downIn = AnimationUtils.loadAnimation(this, R.anim.slide_down_in);
        flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flip_left_out);
        flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flip_left_in);
        for (Animator child : flipOut.getChildAnimations())
            child.addListener(this);
        for (Animator child : flipIn.getChildAnimations())
            child.addListener(this);

    }

    @Override
    public void onResume(){
        super.onResume();
        if (mySet == null)
            bail();
        Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        List<Term> terms = quizlet.getSetTerms(mySet.get_id());
        if (terms.isEmpty())
            bail();
        mgr = new FlashCardManager(quizlet.getSetTerms(mySet.get_id()), frontFirst, holder);
        holder.addView(mgr.getView(this));
    }

    /**
     * something is wrong with the set, take the user to the set list page
     */
    public void bail(){
        Intent intent = new Intent(this, SetListActivity.class);
        startActivity(intent);
    }

    /**
     * Flip the card over
     * @param v the view that was clicked
     */
    public void onClick(View v) {
        Log.d("Clicked on view", "Yay");
        View next = mgr.flipView(this);
        flipOut.setTarget(v);
        flipIn.setTarget(next);
        holder.addView(next);
        flipOut.start();
        flipIn.start();
    }

    /**
     * called when a round is complete. Display the score and ask the user how to proceed
     */
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

    /**
     * Start the next round
     * @param v the view that was clicked
     */
    public void nextRound(View v){
        mgr.nextRound();
        holder.removeAllViews();
        holder.addView(mgr.getView(this));
    }

    /**
     * restart the flashcard session
     * @param v the view that was clicked
     */
    public void restart(View v){
        mgr.reset();
        holder.removeAllViews();
        holder.addView(mgr.getView(this));
    }

    /**
     * handle the user dragging the flashcard view, move it offscreen if need be
     * @param v the view that was clicked
     * @param event the motion event associaed with it
     * @return whether the event was handled
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (animationCount != 0)
            return false;
        if (gestureDetector.onTouchEvent(event)){
            onClick(v);
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startingX = v.getX();
                minSwipe = v.getWidth() / 3;
                initialX = event.getRawX();
                initialY = event.getRawY();
                flip = true;
                Log.d("Action down", "returning true");
                return true;
            case MotionEvent.ACTION_MOVE:
                float change = event.getRawX() - initialX;
                v.setX(startingX + change);
                Log.d("Dx", "=" + Math.abs(change));
                //If the card is moved sufficiently far from the start, don't flip it
                if (Math.abs(change) > minSwipe/3)
                    flip = false;
                incorrect.setAlpha(Math.min(1.0f, Math.abs(change)/minSwipe));
                correct.setAlpha(Math.min(1.0f, Math.abs(change)/minSwipe));
                Log.d("Action Move", "returning true");
                return true;
            case MotionEvent.ACTION_UP:
                float dx = event.getRawX() - initialX;
                float dy = event.getRawY() - initialY;
                incorrect.setAlpha(0.0f);
                correct.setAlpha(0.0f);
                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > minSwipe) {
                    if (dx < 0) {
                        v.startAnimation(rOut);
                        mgr.next();
                    } else {
                        v.startAnimation(lOut);
                        mgr.save();
                    }
                    if (mgr.roundComplete()) {
                        reset();
                        Log.d("new Round", "returning true");
                        return true;
                    }
                    View newView = mgr.getView(this);
                    holder.addView(newView);
                    newView.startAnimation(downIn);
                    holder.removeView(v);
                    Log.d("View Flipped", "returning true");
                    return true;
                }
                v.setX(startingX);
                Log.d("Action Up ", "returning " + !flip);
                return !flip;
            default:
                Log.d("default case", "returning ");
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_study, menu);
        this.menu = menu;
        setLangIcon();
        return true;
    }

    public void study(){
        Intent intent = new Intent(this, SetActivity.class);
        intent.putExtra(SetActivity.SET, mySet);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_edit:
                study();
                return true;
            case R.id.action_restart:
                mgr.reset();
                holder.removeAllViews();
                holder.addView(mgr.getView(this));
                return true;
            case R.id.action_lang:
                frontFirst = !frontFirst;
                setLangIcon();
                mgr.changeFirst();
                holder.removeAllViews();
                holder.addView(mgr.getView(this));
                return true;
            case R.id.action_settings:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * switch the language symbol between Я and R
     * todo handle sets with the same languag for terms and defs
     */
    public void setLangIcon(){
        if (frontFirst ^ mySet.getLang_terms().equals(Quizlet.RUSSIAN)){
            menu.getItem(0).setIcon(R.drawable.my_ya);
            menu.getItem(0).setTitle("Russian First");
        } else {
            menu.getItem(0).setIcon(R.drawable.my_r);
            menu.getItem(0).setTitle("English First");
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
        animationCount++;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        animationCount--;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {}

    @Override
    public void onAnimationStart(Animator animation) {
        animationCount++;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        animationCount--;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        animationCount--;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {}

    /**
     * a private class to simplify the onTouch madness
     */
    private class SingleTapConfirm extends SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }
}
