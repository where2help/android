package app.iamin.iamin.data.event;

import java.util.List;

/**
 * Created by Markus on 13.10.15.
 */
public class NeedsEvent {

    private final List<String> errors;

    public NeedsEvent(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return this.errors;
    }
}
