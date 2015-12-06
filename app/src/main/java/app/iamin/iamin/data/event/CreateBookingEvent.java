package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class CreateBookingEvent {

    public final int status;
    public final int id;
    public final String error;

    public CreateBookingEvent(int status, int id, String error) {
        this. status = status;
        this.id = id;
        this.error = error;
    }
}
