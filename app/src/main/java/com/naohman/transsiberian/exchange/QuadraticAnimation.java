package com.naohman.transsiberian.exchange;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by jeffrey on 2/24/15.
 * This creates an animation that follows a quadratic path
 */
public class QuadraticAnimation extends Animation {
    private PathMeasure measure;
    private float[] pos = new float[2];

    /**
     * create an empty Quadratic animation
     */
    public QuadraticAnimation() {}

    /**
     * Change the Animation's path
     * @param dx the horizontal distance of the animation
     * @param dy the vertical distance of the animation
     * @param rightToLeft whether to go left to right or right to left
     */
    public void setPath(float dx, float dy, boolean rightToLeft) {
        Path path = new Path();
        if (rightToLeft) {
            path.moveTo(dx, 0);
            path.quadTo(dx / 2, -dy, 0, 0);
        } else {
            path.moveTo(0, 0);
            path.quadTo(dx / 2, dy, dx, 0);
        }
        measure = new PathMeasure(path, false);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t){
        measure.getPosTan(measure.getLength() * interpolatedTime, pos,null);
        t.getMatrix().setTranslate(pos[0], pos[1]);
    }
}
