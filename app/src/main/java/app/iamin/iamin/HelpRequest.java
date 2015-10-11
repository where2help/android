package app.iamin.iamin;

import android.location.Address;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    private int id;
    private TYPE mType;
    private Date mStart = new Date();
    private Date mEnd = new Date();
    private Address mAddress = new Address(Locale.GERMAN);
    private int mStillOpen = 0;

    private static Map<String, TYPE> TYPE_NAMES;
    static {
        TYPE_NAMES = new HashMap<String, TYPE>();
        TYPE_NAMES.put("general", TYPE.VOLUNTEER);
    }

    public HelpRequest(String type) {
        if (HelpRequest.TYPE_NAMES.containsKey(type)) {
            this.mType = HelpRequest.TYPE_NAMES.get(type);
        } else {
            this.mType = TYPE.VOLUNTEER;
        }
    }

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

    public int getId() { return id; }
    public Date getStart() { return mStart; }
    public Date getEnd() { return mEnd; }
    public Address getAddress() { return mAddress; }
    public int getStillOpen() { return mStillOpen; }

    public void setId(int id) { this.id = id; }
    public void setStart(Date start) { mStart = start;}
    public void setEnd(Date end) { mEnd = end; }
    public void setAddress(Address addr) { mAddress = addr; }
    public void setStillOpen(int stillOpen) { mStillOpen = stillOpen; }
}
