package app.iamin.iamin.event;

import java.util.List;

/**
 * Created by Markus on 13.10.15.
 */
public class LogoutEvent {

    private List<String> errors;

    public LogoutEvent(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return this.errors;
    }
}
