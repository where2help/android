package app.iamin.iamin.data.event;

import java.util.List;

/**
 * Created by Markus on 13.10.15.
 */
public class RegisterEvent {

    public final List<String> errors;

    public RegisterEvent( List<String> errors) {
        this.errors = errors;
    }
}