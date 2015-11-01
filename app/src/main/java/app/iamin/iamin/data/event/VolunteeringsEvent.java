package app.iamin.iamin.data.event;

import java.util.List;

/**
 * Created by Markus on 13.10.15.
 */
public class VolunteeringsEvent {

    private final List<String> errors;

    public VolunteeringsEvent(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return this.errors;
    }
}
