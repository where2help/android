package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class BookingCanceledEvent {

    public final int id;
    public final String error;

    public BookingCanceledEvent(int id, String error) {
        this.id = id;
        this.error = error;
    }
}
