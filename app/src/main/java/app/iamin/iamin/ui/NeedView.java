package app.iamin.iamin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import app.iamin.iamin.R;
import app.iamin.iamin.model.Need;
import app.iamin.iamin.util.TimeUtils;

/**
 * Created by Markus on 16.10.15.
 *
 * A custom ViewGroup which solves the issue with different text heights in the same row.
 * NeedView will be used in MainActivity, DetailActivity and UserActivity.
 */

public class NeedView extends FrameLayout{

    private ImageView iconView;
    private TextView countView;
    private TextView categoryView;
    private TextView addressView;
    private TextView dateTextView;

    private Need need;

    private int keyline;

    public NeedView(Context context) {
        super(context);
    }

    public NeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        keyline = getResources().getDimensionPixelSize(R.dimen.keyline_1);

        iconView = (ImageView) getChildAt(0);
        countView = (TextView) getChildAt(1);
        categoryView = (TextView) getChildAt(2);
        addressView = (TextView) getChildAt(3);
        dateTextView = (TextView) getChildAt(4);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // TODO: measure correctly!
        int width = MeasureSpec.getSize(widthSpec);
        int height = getPaddingBottom() + getPaddingTop();

        //Measure the iconView-Button
        iconView.setImageResource(need.getCategoryIcon());
        measureChildWithMargins(iconView, widthSpec, 0, heightSpec, 0);

        //Measure the countView
        countView.setText(need.getCount() + "");
        measureChildWithMargins(countView, widthSpec, 0, heightSpec, 0);

        //Measure the categoryView
        categoryView.setText(need.getCount() == 1 ?
                need.getCategorySingular() : need.getCategoryPlural());
        measureChildWithMargins(categoryView, widthSpec, 0, heightSpec, 0);

        // Update the constraints
        height += iconView.getMeasuredHeight();

        //Measure the addressView
        addressView.setText(need.getAddress().getAddressLine(0));
        measureChildWithMargins(addressView, widthSpec, 0, heightSpec, 0);

        // Update the constraints
        height += addressView.getMeasuredHeight();

        //Measure the dateTextView
        String dayStr = TimeUtils.formatHumanFriendlyShortDate(getContext(), need.getStart());
        String dString = dayStr + " " + TimeUtils.formatTimeOfDay(need.getStart()) + " - " +
                TimeUtils.formatTimeOfDay(need.getEnd()) + " Uhr";

        dateTextView.setText(dString);
        measureChildWithMargins(dateTextView, widthSpec, 0, heightSpec, 0);

        height += dateTextView.getMeasuredHeight();

        // Set the dimension for this ViewGroup
        setMeasuredDimension(width, height);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int horizontalPadding, int parentHeightMeasureSpec, int verticalPadding) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();

        int childWidthMeasureSpec = getChildMeasureSpec(
                parentWidthMeasureSpec,
                horizontalPadding,
                lp.width);

        int childHeightMeasureSpec = getChildMeasureSpec(
                parentHeightMeasureSpec,
                verticalPadding,
                lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int baseline =  getPaddingTop() + iconView.getMeasuredHeight();
        float dp = getResources().getDisplayMetrics().density;

        iconView.layout(
                getPaddingLeft(),
                getPaddingTop(),
                getPaddingLeft() + iconView.getMeasuredWidth(),
                baseline);

        countView.layout(
                keyline,
                baseline - countView.getMeasuredHeight() + (int) (2 * dp),
                keyline + countView.getMeasuredWidth(),
                baseline);

        categoryView.layout(
                keyline + countView.getMeasuredWidth(),
                baseline - categoryView.getMeasuredHeight(),
                getMeasuredWidth() - getPaddingRight(),
                baseline);

        addressView.layout(
                getPaddingLeft(),
                baseline,
                getMeasuredWidth(),
                baseline + addressView.getMeasuredHeight());

        dateTextView.layout(
                getPaddingLeft(),
                baseline + addressView.getMeasuredHeight(),
                getMeasuredWidth(),
                getMeasuredHeight() - getPaddingBottom());
    }

    public void setNeed(Need need) {
        this.need = need;
    }

    public void setCount(int count) {
        need.setCount(count);
        requestLayout();
        invalidate();
    }

    // TODO: show distance and duration
}
