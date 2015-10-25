package app.iamin.iamin.util;

import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Created by Markus on 24.10.15.
 */
public class TextUtils {

    public static boolean isEmailValid(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }
}
