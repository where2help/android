package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class UserSignOutEvent {

    public final int status;
    public final String error;

    public UserSignOutEvent(int status, String error) {
        this.status = status;
        this.error = error;
    }
}
