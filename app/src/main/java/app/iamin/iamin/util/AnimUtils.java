package app.iamin.iamin.util;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Markus on 03.11.15.
 */
public class AnimUtils {

    public static void playPopInAnim(View v, int startDelay, int duration) {
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

    public static void playPopOutAnim(View v, int startDelay, int duration) {
        if (v != null) {
            ViewCompat.animate(v)
                    .alpha(0f)
                    .scaleX(0f)
                    .scaleY(0f)
                    .setStartDelay(startDelay)
                    .setDuration(duration)
                    .setInterpolator(new OvershootInterpolator());
        }
    }
}
