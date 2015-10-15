package app.iamin.iamin.event;

import app.iamin.iamin.model.Need;

/**
 * Created by Markus on 13.10.15.
 */
public class NeedsEvent {

    private final Need[] needs;

    public NeedsEvent(Need[] needs) {
        this.needs = needs;
    }

    public Need[] getNeeds() {
        return this.needs;
    }
}
