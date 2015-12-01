package app.iamin.iamin.data.event;

/**
 * Created by Markus on 11.11.15.
 */
public class NeedFeedUpdatedEvent {

    public final String error;

    public NeedFeedUpdatedEvent(String error) {
        this.error = error;
    }
}
