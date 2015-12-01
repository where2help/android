package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class UserSignInEvent {

    public final String error;

    public UserSignInEvent(String error) {
        this.error = error;
    }
}
