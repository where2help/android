package app.iamin.iamin.event;

import android.location.Location;

/**
 * Created by Markus on 13.10.15.
 */
public class LocationUpdateEvent {

    private final Location location;

    public LocationUpdateEvent(Location location) {
        this.location = location;
    }

    public Location getStatus() {
        return this.location;
    }
}
