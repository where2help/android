package app.iamin.iamin.event;

import app.iamin.iamin.HelpRequest;

/**
 * Created by Markus on 13.10.15.
 */
public class NeedsEvent {

    private final HelpRequest[] needs;

    public NeedsEvent(HelpRequest[] needs) {
        this.needs = needs;
    }

    public HelpRequest[] getNeeds() {
        return this.needs;
    }
}
