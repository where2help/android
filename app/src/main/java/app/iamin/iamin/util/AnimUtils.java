package app.iamin.iamin.util;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Markus on 03.11.15.
 */
public class AnimUtils {

    public static void playPopAnim(View v, int startDelay, int duration) {
        if (v != null) {
            ViewCompat.setAlpha(v, 0f);
            ViewCompat.setScaleX(v, 0f);
            ViewCompat.setScaleY(v, 0f);

            ViewCompat.animate(v)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(startDelay)
                    .setDuration(duration)
                    .setInterpolator(new OvershootInterpolator());
        }
    }
}
