package app.iamin.iamin.ui.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import app.iamin.iamin.R;

/**
 * A SwipeRefreshLayout with a workaround for the following issue:
 * https://code.google.com/p/android/issues/detail?id=77712
 */
public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {

    private boolean isLaidOut = false;
    private boolean isRefreshing = false;

    public CustomSwipeRefreshLayout(Context context) {
        super(context);
        init();
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        int distance = (int) (64 * 2 * getResources().getDisplayMetrics().density);
        setDistanceToTriggerSync(distance);
        setColorSchemeResources(R.color.colorPrimary);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isLaidOut) {
            isLaidOut = true;
            setRefreshing(isRefreshing);
        }
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if (isLaidOut) {
            super.setRefreshing(refreshing);
        } else {
            isRefreshing = refreshing;
        }
    }
}
