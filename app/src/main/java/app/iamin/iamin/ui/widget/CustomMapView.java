package app.iamin.iamin.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import app.iamin.iamin.R;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.util.NeedUtils;
import app.iamin.iamin.util.UiUtils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;

/**
 * A com.google.android.gms.maps.MapView that shows a ColorDrawable while loading and
 * fades in automatically when loading is done.
 *
 * @author Markus Rubey
 */
public class CustomMapView extends MapView implements OnMapReadyCallback, OnMapLoadedCallback {

    private static final String TAG = "CustomMapView";

    private ColorDrawable foreground;
    private ColorDrawable scrim;

    private GoogleMap mMap;
    private Need need;

    private int height;
    private float offsetY = 0;

    private boolean isCreated = false;

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
        mMap = map;
        setupMap(map);
    }

    private void setupMap(GoogleMap map) {
        if (map != null && need != null && need.isValid()) {
            LatLng position = NeedUtils.getLocation(need);
            map.getUiSettings().setMapToolbarEnabled(false);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f));
            map.addMarker(new MarkerOptions().position(position));
            map.setOnMapLoadedCallback(this);
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    UiUtils.fireMapIntent(getContext(), need);
                    return true;
                }
            });
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    UiUtils.fireMapIntent(getContext(), need);
                }
            });
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
        setupMap(mMap);
    }

    public void setOffset(int scrollY) {
        offsetY = scrollY;
        float ratio = (float) scrollY / (float) height;
        int alpha = (int) (ratio * 200);
        scrim.setAlpha(alpha);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h != oldh) {
            height = h;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (offsetY > 0) {
            canvas.clipRect(0, 0, getWidth(), height - offsetY);
        }
        super.onDraw(canvas);
    }

    public void create() {
        onCreate(null);
        isCreated = true;
    }

    public void resume() {
        if (isCreated) onResume();
    }

    public void pause() {
        if (isCreated) onPause();
    }

    public void destroy() {
        if (isCreated) onDestroy();
    }

    public void saveInstanceState() {
        if (isCreated)  onSaveInstanceState();
    }

    public void lowMemory() {
        if (isCreated) onLowMemory();
    }
}