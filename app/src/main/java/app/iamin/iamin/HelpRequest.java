package app.iamin.iamin;

import android.location.Address;
import android.location.Geocoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private String selfLink;

    private static Map<String, TYPE> TYPE_NAMES;
    static {
        TYPE_NAMES = new HashMap<String, TYPE>();
        TYPE_NAMES.put("general", TYPE.VOLUNTEER);
        TYPE_NAMES.put("legal", TYPE.LAWYER);
        TYPE_NAMES.put("medical", TYPE.DOCTOR);
        TYPE_NAMES.put("translation", TYPE.INTERPRETER);
    }

    public HelpRequest() {

    }

    public String getType() {
        switch(this.mType) {
            case DOCTOR: return "Ärzte";
            case LAWYER: return "Rechtsberater";
            case INTERPRETER: return "Dolmetscher";
            case VOLUNTEER: default: return "Freiwillige";
        }
    }

    public int getTypeIcon() {
        switch(this.mType) {
            case DOCTOR: return R.mipmap.ic_medical;
            case LAWYER: return R.mipmap.ic_legal;
            case INTERPRETER: return R.mipmap.ic_interpretor;
            case VOLUNTEER: default: return R.mipmap.ic_volunteer;
        }
    }

    public TYPE getTypeEnum() {
        return this.mType;
    }

    public int getId() { return id; }
    public Date getStart() { return mStart; }
    public Date getEnd() { return mEnd; }
    public Address getAddress() { return mAddress; }
    public int getStillOpen() { return mStillOpen; }
    public String getSelfLink() { return selfLink; }

    public void setId(int id) { this.id = id; }
    public void setStart(Date start) { mStart = start;}
    public void setEnd(Date end) { mEnd = end; }
    public void setAddress(Address addr) { mAddress = addr; }
    public void setStillOpen(int stillOpen) { mStillOpen = stillOpen; }
    public void setType(TYPE type) { this.mType = type; }
    public void setType(String type) {
        if (HelpRequest.TYPE_NAMES.containsKey(type)) {
            this.mType = HelpRequest.TYPE_NAMES.get(type);
        } else {
            this.mType = TYPE.VOLUNTEER;
        }
    }
    public void setSelfLink(String selfLink) { this.selfLink = selfLink; }

    public void fromJSON(JSONObject obj, Geocoder coder) throws JSONException, IOException, ParseException {
        JSONObject attrs = obj.getJSONObject("attributes");
        setType(attrs.getString("category"));
        setId(obj.getInt("id"));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        setStart(simpleDateFormat.parse(attrs.getString("start-time")));
        setEnd(simpleDateFormat.parse(attrs.getString("end-time")));
        List<Address> addresses = coder.getFromLocationName(attrs.getString("city") + " " + attrs.getString("location"), 1);
        if (addresses.size() > 0) {
            setAddress(addresses.get(0));
        } else {
            Address address = new Address(Locale.GERMAN);
            address.setAddressLine(0, attrs.getString("city") + " " + attrs.getString("location"));
            setAddress(address);
        }
        setStillOpen(attrs.getInt("volunteers-needed"));
        setSelfLink(obj.getJSONObject("links").getString("self"));
    }
}
