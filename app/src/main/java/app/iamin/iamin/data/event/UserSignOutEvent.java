package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class UserSignOutEvent {

    public final String error;

    public UserSignOutEvent(String error) {
        this.error = error;
    }
}
