package app.iamin.iamin.event;

/**
 * Created by Markus on 13.10.15.
 */
public class LogoutEvent {

    private boolean isSuccsess = false;

    public LogoutEvent(Boolean isSuccsess) {
        this.isSuccsess = isSuccsess;
    }

    public boolean isSuccsess() {
        return this.isSuccsess;
    }
}
