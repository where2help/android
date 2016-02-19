package app.iamin.iamin.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by Markus on 19.02.16.
 */
public class InstantAutoCompleteTextView extends AutoCompleteTextView {

    public InstantAutoCompleteTextView(Context context) {
        super(context);
    }

    public InstantAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InstantAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (hasFocus() && !isPopupShowing()) {
                showDropDown();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            // showKeyboard();
            showDropDown();
        } else {
            hideKeyboard();
            dismissDropDown();
        }
    }

    private void showKeyboard() {
        InputMethodManager inm = (InputMethodManager)
                getContext().getSystemService(INPUT_METHOD_SERVICE);
        inm.showSoftInput(this, 0);
    }

    private void hideKeyboard() {
        InputMethodManager inm = (InputMethodManager)
                getContext().getSystemService(INPUT_METHOD_SERVICE);
        inm.hideSoftInputFromWindow(getWindowToken(), 0);
    }
}
