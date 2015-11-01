package app.iamin.iamin.data.event;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Markus on 13.10.15.
 */
public class LocationEvent {

    private final LatLng location;

    public LocationEvent(LatLng location) {
        this.location = location;
    }

    public LatLng getLocation() {
        return this.location;
    }
}
