package app.iamin.iamin.event;

/**
 * Created by Markus on 13.10.15.
 */
public class LoginEvent {

    public final int status;

    public LoginEvent(int status) {
        this.status = status;
    }
}
