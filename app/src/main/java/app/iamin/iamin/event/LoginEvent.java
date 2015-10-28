package app.iamin.iamin.event;

import java.util.List;

/**
 * Created by Markus on 13.10.15.
 */
public class LoginEvent {

    public final List<String> errors;

    public LoginEvent(List<String> errors) {
        this.errors = errors;
    }
}
