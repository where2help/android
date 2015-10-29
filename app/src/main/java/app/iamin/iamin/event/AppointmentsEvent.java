package app.iamin.iamin.event;

import app.iamin.iamin.model.Need;

/**
 * Created by Markus on 13.10.15.
 */
public class AppointmentsEvent {

    private final Need[] needs;

    public AppointmentsEvent(Need[] needs) {
        this.needs = needs;
    }

    public Need[] getNeeds() {
        return this.needs;
    }
}
