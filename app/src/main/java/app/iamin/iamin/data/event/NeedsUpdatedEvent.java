package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class NeedsUpdatedEvent {

    public final String error;

    public NeedsUpdatedEvent(String error) {
        this.error = error;
    }
}
