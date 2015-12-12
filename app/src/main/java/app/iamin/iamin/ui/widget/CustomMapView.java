package app.iamin.iamin.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import app.iamin.iamin.R;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.util.NeedUtils;
import app.iamin.iamin.util.UiUtils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;

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
        anim.setDuration(350L);
        anim.setStartDelay(100L);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                foreground.setAlpha(value);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CustomMapView.this.setForeground(scrim);
            }
        });
        anim.start();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (need != null && need.isValid()) {
            LatLng position = NeedUtils.getLocation(need);
            map.getUiSettings().setMapToolbarEnabled(false);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f));
            map.addMarker(new MarkerOptions().position(position));
            map.setOnMapLoadedCallback(this);
        }
    }

    @Override
    public void onMapLoaded() {
        if (SDK_INT < ICE_CREAM_SANDWICH) {
            setForeground(scrim);
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