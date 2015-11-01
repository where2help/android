package app.iamin.iamin.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
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
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * A com.google.android.gms.maps.MapView that shows a ColorDrawable while loading.
 *
 * @author Markus Rubey
 */
public class CustomMapView extends MapView implements OnMapReadyCallback, OnMapLoadedCallback, View.OnClickListener {

    private ColorDrawable foreground;
    private Need need;

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
        int color = ContextCompat.getColor(getContext(), R.color.windowBackgroundDark);
        foreground = new ColorDrawable(color);
        setForeground(foreground);
        getMapAsync(this);
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @TargetApi(LOLLIPOP)
    private void playEnterAnimation() {
        ValueAnimator anim = ValueAnimator.ofInt(255, 0);
        anim.setDuration(300);
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
        });
        anim.start();
    }

    @TargetApi(LOLLIPOP)
    private void dropPin(){
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
                .setInterpolator(new FastOutLinearInInterpolator())
                .translationY(0).setDuration(200).start();
    }

    private ImageView createPin(){
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
        if (SDK_INT < LOLLIPOP) {
            setForeground(null);
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
}
