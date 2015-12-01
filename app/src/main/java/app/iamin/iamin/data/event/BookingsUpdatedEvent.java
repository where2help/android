package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class BookingsUpdatedEvent {

    public final String error;

    public BookingsUpdatedEvent(String error) {
        this.error = error;
    }
}
