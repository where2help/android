package app.iamin.iamin.event;

/**
 * Created by Markus on 13.10.15.
 */
public class LoginEvent {

    private boolean isSuccsess = false;

    public LoginEvent(Boolean isSuccsess) {
        this.isSuccsess = isSuccsess;
    }

    public boolean isSuccsess() {
        return this.isSuccsess;
    }
}
