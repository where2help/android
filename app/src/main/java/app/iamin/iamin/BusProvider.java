package app.iamin.iamin;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Created by Markus on 13.10.15.
 */
public class BusProvider {

    private static final EventBus BUS = new EventBus();

    public static EventBus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // empty
    }

    public static class EventBus extends Bus {
        private final Handler mainThread = new Handler(Looper.getMainLooper());

        @Override
        public void post(final Object event) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        post(event);
                    }
                });
            }
        }
    }
}