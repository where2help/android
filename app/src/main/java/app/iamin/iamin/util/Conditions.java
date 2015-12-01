package app.iamin.iamin.util;

import android.text.TextUtils;

/**
 * Created by Markus on 12.11.15.
 */
public class Conditions {

    public static <T> T notNull(T t, String name) {
        if (t == null) {
            throw new NullPointerException(name + "must not be null");
        }
        return t;
    }

    public static <T extends CharSequence> T notBlank(T t, String name) {
        if (TextUtils.isEmpty(notNull(t, name))) {
            throw new NullPointerException(name + "must not be blank");
        }
        return t;
    }
}
