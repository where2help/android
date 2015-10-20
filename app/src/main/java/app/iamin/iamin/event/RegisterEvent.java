package app.iamin.iamin.event;

/**
 * Created by Markus on 13.10.15.
 */
public class RegisterEvent {

    private boolean isSuccsess = false;

    public RegisterEvent(Boolean isSuccsess) {
        this.isSuccsess = isSuccsess;
    }

    public boolean isSuccsess() {
        return this.isSuccsess;
    }
}
