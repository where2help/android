package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class BookingCreatedEvent {

    public final int id;
    public final String error;

    public BookingCreatedEvent(int id, String error) {
        this.id = id;
        this.error = error;
    }
}
