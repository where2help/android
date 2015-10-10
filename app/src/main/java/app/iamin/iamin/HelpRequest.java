package app.iamin.iamin;

import android.location.Location;

import java.util.Date;

/**
 * Created by Paul on 10-10-2015.
 */
public class HelpRequest {

    public enum TYPE {
        DOCTOR,
        LAWYER,
        INTERPRETER,
        VOLUNTEER
    };

    private TYPE mType;
    private Date mStart;
    private Date mEnd;
    private Location mLocation;
    private int mStillOpen;

    public HelpRequest(TYPE type) {
        this.mType = type;
    }

    public String getName() {
        switch(this.mType) {
            case DOCTOR: return "Ärzte";
            case LAWYER: return "Rechtsberater";
            case INTERPRETER: return "Dolmetscher";
            case VOLUNTEER: return "Freiwillige";
        }
        return null;
    }

    public Date getStart() { return mStart; }
    public Date getEnd() { return mEnd; }
    public Location getLocation() { return mLocation; }
    public int getStillOpen() { return mStillOpen; }

    public void setStart(Date start) { mStart = start;}
    public void setEnd(Date end) { mEnd = end; }
    public void setLocation(Location loc) { mLocation = loc; }
    public void setStillOpen(int stillOpen) { mStillOpen = stillOpen; }
}
