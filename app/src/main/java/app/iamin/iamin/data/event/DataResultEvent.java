package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class DataResultEvent {

    public final int id;
    public final String action;
    public final String error;

    public DataResultEvent(String action, int id, String error) {
        this.action = action;
        this.id = id;
        this.error = error;
    }
}
