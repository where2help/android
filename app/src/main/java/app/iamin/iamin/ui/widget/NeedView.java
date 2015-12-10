package app.iamin.iamin.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import app.iamin.iamin.R;
import app.iamin.iamin.data.DataManager;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.util.NeedUtils;

/**
 * Created by Markus on 16.10.15.
 * <p/>
 * A custom ViewGroup which solves the issue with different text heights in the same row.
 * NeedView will be used in MainActivity, DetailActivity and UserActivity.
 */

public class NeedView extends FrameLayout {

    private boolean isAttached = false;

    private ImageView iconView;
    private ImageView checkView;

    private TextView countView;
    private TextView categoryView;
    private TextView addressView;
    private TextView dateTextView;

    private Need need;

    private int keyline;

    private int padding;

    private float dp;

    private boolean inDetail = false;

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
        checkView = (ImageView) getChildAt(5);
        countView = (TextView) getChildAt(1);
        categoryView = (TextView) getChildAt(2);
        addressView = (TextView) getChildAt(3);
        dateTextView = (TextView) getChildAt(4);

        if (!inDetail) {
            addressView.setMaxLines(1);
            dateTextView.setMaxLines(1);
        }

        isAttached = true;
        fill();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // Measure the iconView
        measureChild(iconView, widthSpec, heightSpec);

        // Measure the checkView
        measureChild(checkView, widthSpec, heightSpec);

        // Measure the countView
        measureChild(countView, widthSpec, heightSpec);

        // Measure the categoryView
        measureChild(categoryView, widthSpec, heightSpec);

        // Measure the addressView
        measureChildWithMargins(addressView, widthSpec, padding * 3 + checkView.getMeasuredWidth(), heightSpec, 0);

        // Measure the dateTextView
        measureChildWithMargins(dateTextView, widthSpec, padding * 3, heightSpec, 0);

        // Set dimensions
        int width = MeasureSpec.getSize(widthSpec);
        int height = 2 * padding + iconView.getMeasuredHeight() + addressView.getMeasuredHeight()
                + dateTextView.getMeasuredHeight();

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int baseline = padding + iconView.getMeasuredHeight();

        iconView.layout(
                padding,
                padding,
                baseline,
                baseline);

        checkView.layout(
                getMeasuredWidth() - padding - checkView.getMeasuredWidth(),
                (getMeasuredHeight() - checkView.getMeasuredWidth()) / 2,
                getMeasuredWidth() - padding,
                (getMeasuredHeight() + checkView.getMeasuredWidth()) / 2);

        countView.layout(
                baseline,
                baseline - countView.getMeasuredHeight() + (int) (2 * dp),
                baseline + countView.getMeasuredWidth(),
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
        int category = need.getCategory();
        iconView.setImageResource(NeedUtils.getCategoryIcon(category));
        countView.setText(String.valueOf(need.getNeeded()));
        categoryView.setText(category == 1 ? NeedUtils.getCategorySingular(category) : NeedUtils.getCategoryPlural(category));
        addressView.setText(need.getCity() + " " + need.getLocation());
        dateTextView.setText(need.getDate());
        checkView.setVisibility(need.isAttending() && DataManager.hasUser() ? View.VISIBLE : View.GONE);
    }

    public void setNeed(Need need) {
        this.need = need;
        if (isAttached) fill();
    }

/*
    public void setDistance(String distance) {
        String address = need.getAddress().getAddressLine(0);
        SpannableString span = new SpannableString(address + " (" + distance + ")");
        span.setSpan(new RelativeSizeSpan(0.75f), address.length() + 1, span.length(), 0);
        addressView.setText(span);
    }

    private SpannableString getDuration() {
        String date = need.getDate();
        String duration = TimeUtils.getDuration(need.getStart(), need.getEnd());
        SpannableString span = new SpannableString(date + " (" + duration + ")");
        span.setSpan(new RelativeSizeSpan(0.75f), date.length() + 1, span.length(), 0);
        return span;
    }
*/
    public void setInDetail(boolean inDetail) {
        this.inDetail = inDetail;
    }
}
