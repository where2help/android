package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class UserSignUpEvent {

    public final String error;

    public UserSignUpEvent(String error) {
        this.error = error;
    }
}
