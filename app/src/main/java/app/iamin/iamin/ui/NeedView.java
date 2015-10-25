package app.iamin.iamin.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import app.iamin.iamin.R;
import app.iamin.iamin.model.Need;

/**
 * Created by Markus on 16.10.15.
 * <p/>
 * A custom ViewGroup which solves the issue with different text heights in the same row.
 * NeedView will be used in MainActivity, DetailActivity and UserActivity.
 */

public class NeedView extends FrameLayout {

    private boolean isAttached = false;

    private ImageView iconView;
    private TextView countView;
    private TextView categoryView;
    private TextView addressView;
    private TextView dateTextView;

    private Need need;

    private int keyline;

    private int padding;

    private float dp;

    public NeedView(Context context) {
        this(context, null);
    }

    public NeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        keyline = res.getDimensionPixelSize(R.dimen.keyline_1);
        padding = res.getDimensionPixelSize(R.dimen.padding);
        dp = res.getDisplayMetrics().density;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        iconView = (ImageView) getChildAt(0);
        countView = (TextView) getChildAt(1);
        categoryView = (TextView) getChildAt(2);
        addressView = (TextView) getChildAt(3);
        dateTextView = (TextView) getChildAt(4);

        isAttached = true;
        fill();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int height = 2 * padding;

        // Measure the iconView-Button
        measureChildWithMargins(iconView, widthSpec, 0, heightSpec, 0);

        // Measure the countView
        measureChildWithMargins(countView, widthSpec, 0, heightSpec, 0);

        // Measure the categoryView
        measureChildWithMargins(categoryView, widthSpec, 0, heightSpec, 0);

        // Update height
        height += iconView.getMeasuredHeight();

        // Measure the addressView
        measureChildWithMargins(addressView, widthSpec, 0, heightSpec, 0);

        // Update height
        height += addressView.getMeasuredHeight();

        // Measure the dateTextView
        measureChildWithMargins(dateTextView, widthSpec, 0, heightSpec, 0);

        // Update height
        height += dateTextView.getMeasuredHeight();

        // Set dimensions
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int baseline = padding + iconView.getMeasuredHeight();

        iconView.layout(
                padding,
                padding,
                padding + iconView.getMeasuredWidth(),
                baseline);

        countView.layout(
                keyline,
                baseline - countView.getMeasuredHeight() + (int) (2 * dp),
                keyline + countView.getMeasuredWidth(),
                baseline);

        categoryView.layout(
                countView.getRight(),
                baseline - categoryView.getMeasuredHeight(),
                getMeasuredWidth() - padding,
                baseline);

        addressView.layout(
                padding,
                baseline,
                categoryView.getRight(),
                baseline + addressView.getMeasuredHeight());

        dateTextView.layout(
                padding,
                addressView.getBottom(),
                addressView.getRight(),
                getMeasuredHeight() - padding);
    }

    private void fill() {
        iconView.setImageResource(need.getCategoryIcon());
        countView.setText(need.getCount() + "");
        categoryView.setText(need.getCount() == 1 ? need.getCategorySingular() : need.getCategoryPlural());
        addressView.setText(need.getAddress().getAddressLine(0));
        dateTextView.setText(need.getDate());
    }

    public void setNeed(Need need) {
        this.need = need;
        if (isAttached) fill();
    }

    public void setCount(int count) {
        this.need.setCount(count);
        if (isAttached) countView.setText(need.getCount() + "");
    }

    // TODO: show distance and duration
}
