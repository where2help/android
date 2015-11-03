package app.iamin.iamin.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import app.iamin.iamin.R;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.util.NeedUtils;
import app.iamin.iamin.util.UiUtils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * A com.google.android.gms.maps.MapView that shows a ColorDrawable while loading.
 *
 * @author Markus Rubey
 */
public class CustomMapView extends MapView implements OnMapReadyCallback, OnMapLoadedCallback, View.OnClickListener {

    private static final String TAG = "CustomMapView";

    private ColorDrawable foreground;
    private ColorDrawable scrim;
    private Need need;

    private int height;

    public CustomMapView(Context context) {
        super(context);
    }

    public CustomMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        scrim = new ColorDrawable(Color.BLACK);
        scrim.setAlpha(0);
        int color = ContextCompat.getColor(getContext(), R.color.windowBackgroundDark);
        foreground = new ColorDrawable(color);
        setForeground(foreground);
        getMapAsync(this);
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @TargetApi(ICE_CREAM_SANDWICH)
    private void playEnterAnimation() {
        ValueAnimator anim = ValueAnimator.ofInt(255, 0);
        anim.setDuration(300L);
        anim.setStartDelay(350L);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                foreground.setAlpha(value);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                dropPin();

            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CustomMapView.this.setForeground(scrim);
            }
        });
        anim.start();
    }

    @TargetApi(ICE_CREAM_SANDWICH)
    private void dropPin() {
        ImageView pin = createPin();
        pin.setAlpha(0f);
        pin.setScaleX(0);
        pin.setScaleY(0);
        pin.setTranslationY(-getMeasuredHeight() / 2);
        addView(pin);
        pin.animate()
                .alpha(1)
                .rotationBy(45)
                .scaleX(1).scaleY(1)
                .translationY(0)
                .setInterpolator(new FastOutLinearInInterpolator())
                .setDuration(200L)
                .setStartDelay(0L).start();
    }

    private ImageView createPin() {
        ImageView pin = new ImageView(getContext());
        LayoutParams lp = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        pin.setLayoutParams(lp);
        pin.setImageResource(R.drawable.ic_toolbar_logo);
        pin.setClickable(true);
        pin.setOnClickListener(this);
        return pin;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.getUiSettings().setMapToolbarEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(NeedUtils.getLocation(need), 13f));
        map.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        if (SDK_INT < ICE_CREAM_SANDWICH) {
            setForeground(scrim);
            addView(createPin());
        } else {
            playEnterAnimation();
        }
    }

    public void setNeed(Need need) {
        this.need = need;
    }

    @Override
    public void onClick(View v) {
        UiUtils.fireMapIntent(getContext(), need);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h != oldh) {
            height = h;
        }
    }

    public void setOffset(int scrollY) {
        float ratio = (float) scrollY / (float) height;
        int alpha = (int) (ratio * 200);
        scrim.setAlpha(alpha);
    }
}
