package com.naohman.transsiberian.study;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
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

public class Study extends ActionBarActivity implements View.OnTouchListener {
    private static long fast = 300, slow = 800, out = 500;
    private TextView set_title, set_description;
    private AnimatorSet flipOut, flipIn;
    private Menu menu;
    private float startingX, initialX, initialY, minSwipe;
    private RelativeLayout holder;
    private ImageView correct, incorrect;
    private QuizletSet mySet;
    private FlashCardManager mgr;
    private static Animation downIn, lOut, rOut;
    private ViewPropertyAnimator cAnim = null, iAnim = null;
    private boolean frontFirst = true, flip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mySet = (QuizletSet) getIntent().getSerializableExtra("set");
        if (mySet == null)
            bail();
        setContentView(R.layout.activity_study);
        incorrect = (ImageView) findViewById(R.id.incorrect_img);
        correct = (ImageView) findViewById(R.id.correct_img);
        holder = (RelativeLayout) findViewById(R.id.flashcard_holder);
        set_title = (TextView) findViewById(R.id.set_title);
        set_title.setText(mySet.getTitle());
        set_description = (TextView) findViewById(R.id.set_description);
        set_description.setText(mySet.getDescription());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.title_box).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit();
            }
        });
        lOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        rOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        downIn = AnimationUtils.loadAnimation(this, R.anim.slide_down_in);
        flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flip_left_out);
        flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flip_left_in);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mySet == null)
            bail();
        Quizlet quizlet = Quizlet.getInstance();
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
    public void flip(View v) {
        View next = mgr.flipView(this);
        fade(fast, fast, 0f);
        flipOut.setTarget(v);
        holder.addView(next);
        flipIn.setTarget(next);
        flipOut.start();
        flipIn.start();
    }

    /**
     * handle the user dragging the flashcard view, move it off screen if need be
     * @param v the view that was clicked
     * @param event the motion event associated with it
     * @return whether the event was handled
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (flipIn.isRunning() || flipOut.isRunning())
            return false;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startingX = v.getX();
                minSwipe = v.getWidth() / 3;
                initialX = event.getRawX();
                initialY = event.getRawY();
                fade(fast, fast, 0.5f);
                flip = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                float change = event.getRawX() - initialX;
                v.setX(startingX + change);
                //If the card is moved sufficiently far from the start, don't flip it
                if (Math.abs(change) > minSwipe/3)
                    flip = false;
                adjustAlpha(change);
                return true;
            case MotionEvent.ACTION_UP:
                float dx = event.getRawX() - initialX;
                float dy = event.getRawY() - initialY;
                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > minSwipe) {
                    swipeOut(v, dx);
                    if (mgr.roundComplete()) {
                        reset();
                    } else {
                        View newView = mgr.getView(this);
                        holder.addView(newView);
                        newView.startAnimation(downIn);
                        holder.removeView(v);
                    }
                    return true;
                }
                v.setX(startingX);
                fade(fast, fast, 0f);
                if (flip)
                    flip(v);
                return true;
        }
        return false;
    }

    /**
     * Cancel any pending animations on the hint images and start a new one
     * @param cDur the duration of the animation on the 'correct' image
     * @param iDur the duraton of the animation on the 'incorrect' image
     * @param targetAlpha the alpha to be faded too
     */
    private void fade(long cDur, long iDur, float targetAlpha){
        if (cAnim != null)
            cAnim.cancel();
        correct.setVisibility(View.VISIBLE);
        cAnim = correct.animate().
                alpha(targetAlpha).
                setDuration((long) (cDur * Math.abs(correct.getAlpha() - targetAlpha))).
                setStartDelay(targetAlpha == 0.5f ? 100 : 0);
        if (iAnim != null)
            iAnim.cancel();
        incorrect.setVisibility(View.VISIBLE);
        iAnim = incorrect.animate().
                alpha(targetAlpha).
                setDuration((long) (iDur * Math.abs(incorrect.getAlpha() - targetAlpha))).
                setStartDelay(targetAlpha == 0.5f ? 100 : 0);
    }

    /**
     * Set the alpha value of the hint buttons to match the given x coordinate
     * @param change an x coordinate
     */
    public void adjustAlpha(float change){
        if (cAnim != null)
            cAnim.cancel();
        if (iAnim != null)
            iAnim.cancel();
        incorrect.setAlpha(bound(0f, .5f + (change / (2 * minSwipe)), 1f));
        correct.setAlpha(bound(0f, .5f - (change / (2 * minSwipe)), 1f));
    }

    /**
     * Remove the view in the direction given and fade out the hint images
     * @param view the view to be removed
     * @param dx the direction the view was moved
     */
    public void swipeOut(View view, float dx){
        float total = view.getWidth() + startingX - minSwipe;
        long dur = (long) (out * ((total - (Math.abs(dx) - minSwipe))/total));
        if (dx < 0) {
            rOut.setDuration(dur);
            view.startAnimation(rOut);
            fade(slow, fast, 0f);
            mgr.next();
        } else {
            lOut.setDuration(dur);
            view.startAnimation(lOut);
            fade(fast, slow, 0f);
            mgr.save();
        }
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
        score.findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            //Restart the session
            @Override
            public void onClick(View v) {
                mgr.reset();
                holder.removeAllViews();
                holder.addView(mgr.getView(Study.this));
            }
        });
        if (mgr.isFinished()) {
            score.findViewById(R.id.next_round).setVisibility(View.GONE);
        } else {
            score.findViewById(R.id.next_round).setOnClickListener(new View.OnClickListener() {
                //Go to the next round
                @Override
                public void onClick(View v) {
                    mgr.nextRound();
                    holder.removeAllViews();
                    holder.addView(mgr.getView(Study.this));
                }
            });
        }
    }

    /**
     * @param low a lower bound
     * @param x a number
     * @param high an upper bound
     * @return the number if it is between the two bounds, otherwise, the bound it exceeds
     */
    private static float bound(float low, float x, float high){
        return Math.min(high, Math.max(low, x));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_study, menu);
        this.menu = menu;
        setLangIcon();
        return true;
    }

    /**
     * Start the SetActivity so that the user can edit this set.
     */
    public void edit(){
        Intent intent = new Intent(this, SetActivity.class);
        intent.putExtra(SetActivity.SET, mySet);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_edit:
                edit();
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
}
