package app.iamin.iamin;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Markus on 10.10.15.
 */
public class CountView extends View{

    private int w = 0;
    private int h = 0;

    private static final String gebraucht = "gebraucht";
    private int count = 0;

    private int textSize;
    private int numberSize;

    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CountView(Context context) {
        super(context);
        init();
    }

    public CountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Resources res = getResources();
        textSize = res.getDimensionPixelSize(R.dimen.countview_small);
        numberSize = res.getDimensionPixelSize(R.dimen.countview_large);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.WHITE);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(gebraucht, w / 2, h - textSize * 1.2f, paint);

        paint.setTextSize(numberSize);
        canvas.drawText(count + "", w / 2, (h - numberSize * 1.2f) / 2 + textSize, paint);
    }

    public void setCount(int count) {
        this.count = count;
        invalidate();
    }
}
