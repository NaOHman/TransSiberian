package com.naohman.transsiberian.Study;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by jeffrey on 2/24/15.
 * This creates an animation that follows a path
 */
public class QuadraticAnimation extends Animation {
    private PathMeasure measure;
    private float[] pos = new float[2];
    public QuadraticAnimation() {}

    public void setPath(float dx, float dy, boolean swapped) {
        Path path = new Path();
        if (swapped) {
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
        Log.d("Setting Position", pos[0] + " " + pos[1]);
    }
}
