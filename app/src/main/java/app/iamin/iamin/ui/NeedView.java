package app.iamin.iamin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Markus on 16.10.15.
 *
 * A custom ViewGroup which solves the issue with different text heights in the same row.
 * NeedView will be used in MainActivity, DetailActivity and UserActivity.
 */

public class NeedView extends ViewGroup{

    ImageView iconImageView;
    TextView countTextView;
    TextView categoryTextView;
    TextView addressTextView;
    TextView dateTextView;

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

        iconImageView = (ImageView) getChildAt(0);
        countTextView = (TextView) getChildAt(1);
        categoryTextView = (TextView) getChildAt(2);
        addressTextView = (TextView) getChildAt(3);
        dateTextView = (TextView) getChildAt(4);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO: measure correctly!
        int widthConstraints = getPaddingLeft() + getPaddingRight();
        int heightConstraints = 0;

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        //Measure the iconImageView-Button
        measureChildWithMargins(
                iconImageView,
                widthMeasureSpec,
                widthConstraints,
                heightMeasureSpec,
                heightConstraints);

        //Measure the countTextView
        countTextView.setText("");
        measureChildWithMargins(
                countTextView,
                widthMeasureSpec,
                widthConstraints,
                heightMeasureSpec,
                0);

        //Measure the categoryTextView
        categoryTextView.setText("");
        measureChildWithMargins(
                categoryTextView,
                widthMeasureSpec,
                widthConstraints,
                heightMeasureSpec,
                heightConstraints);

        // Update the constraints
        heightConstraints += iconImageView.getMeasuredHeight();

        //Measure the addressTextView
        addressTextView.setText("");
        measureChildWithMargins(
                addressTextView,
                widthMeasureSpec,
                widthConstraints,
                heightMeasureSpec,
                heightConstraints);

        // Update the constraints
        heightConstraints += addressTextView.getMeasuredHeight();

        //Measure the dateTextView
        dateTextView.setText("");
        measureChildWithMargins(
                dateTextView,
                widthMeasureSpec,
                widthConstraints,
                heightMeasureSpec,
                heightConstraints);

        height = heightConstraints + dateTextView.getMeasuredHeight();

        // Set the dimension for this ViewGroup
        setMeasuredDimension(width, height);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int horizontalPadding, int parentHeightMeasureSpec, int verticalPadding) {
        LayoutParams lp = child.getLayoutParams();

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
        // TODO: layout children!
    }
}
