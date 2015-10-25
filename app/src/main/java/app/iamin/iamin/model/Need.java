package app.iamin.iamin.model;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import app.iamin.iamin.R;
import app.iamin.iamin.util.TimeUtils;

/**
 * Created by Paul on 10-10-2015.
 */
public class Need {

    private int id;

    private int mCategory;
    public static final int CATEGORY_VOLUNTEER = 0;
    public static final int CATEGORY_LAWYER = 1;
    public static final int CATEGORY_DOCTOR = 2;
    public static final int CATEGORY_INTERPRETER = 3;

    private static Map<String, Integer> CATEGORY_NAMES;

    static {
        CATEGORY_NAMES = new HashMap<>();
        CATEGORY_NAMES.put("general", CATEGORY_VOLUNTEER);
        CATEGORY_NAMES.put("legal", CATEGORY_LAWYER);
        CATEGORY_NAMES.put("medical", CATEGORY_DOCTOR);
        CATEGORY_NAMES.put("translation", CATEGORY_INTERPRETER);
    }

    private Address mAddress = new Address(Locale.GERMAN);

    private Date mStart = new Date();
    private Date mEnd = new Date();
    private String mDate = "";

    private int mCount = 0;
    private String selfLink = "";

    public Need() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setCategory(int category) {
        this.mCategory = category;
    }

    public void setCategory(String category) {
        if (Need.CATEGORY_NAMES.containsKey(category)) {
            this.mCategory = Need.CATEGORY_NAMES.get(category);
        } else {
            this.mCategory = CATEGORY_VOLUNTEER;
        }
    }

    public int getCategory() {
        return mCategory;
    }

    public String getCategoryPlural() {
        switch (this.mCategory) {
            case CATEGORY_DOCTOR:
                return "Ärzte";
            case CATEGORY_LAWYER:
                return "Rechtsberater";
            case CATEGORY_INTERPRETER:
                return "Dolmetscher";
            case CATEGORY_VOLUNTEER:
            default:
                return "Freiwillige";
        }
    }

    public String getCategorySingular() {
        switch (this.mCategory) {
            case CATEGORY_DOCTOR:
                return "Arzt";
            case CATEGORY_LAWYER:
                return "Rechtsberater";
            case CATEGORY_INTERPRETER:
                return "Dolmetscher";
            case CATEGORY_VOLUNTEER:
            default:
                return "Freiwilliger";
        }
    }

    public int getCategoryIcon() {
        switch (this.mCategory) {
            case CATEGORY_DOCTOR:
                return R.drawable.ic_medical;
            case CATEGORY_LAWYER:
                return R.drawable.ic_legal;
            case CATEGORY_INTERPRETER:
                return R.drawable.ic_interpretor;
            case CATEGORY_VOLUNTEER:
            default:
                return R.drawable.ic_volunteer;
        }
    }

    public void setAddress(Address addr) {
        mAddress = addr;
    }

    public Address getAddress() {
        return mAddress;
    }

    public LatLng getLocation() {
        return new LatLng(mAddress.getLatitude(), mAddress.getLongitude());
    }

    public void setStart(Date start) {
        mStart = start;
    }

    public Date getStart() {
        return mStart;
    }

    public void setEnd(Date end) {
        mEnd = end;
    }

    public Date getEnd() {
        return mEnd;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDate() {
        return mDate;
    }

    public void setCount(int stillOpen) {
        mCount = stillOpen;
    }

    public int getCount() {
        return mCount;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public String getSelfLink() {
        return selfLink;
    }


    public Need fromJSON(Context context, JSONObject obj) throws JSONException, IOException, ParseException {
        Need need = new Need();

        JSONObject attrs = obj.getJSONObject("attributes");
        need.setId(obj.getInt("id"));
        need.setCategory(attrs.getString("category"));

        Address address = new Address(Locale.GERMAN);
        address.setAddressLine(0, attrs.getString("city") + " " + attrs.getString("location"));
        address.setLatitude(attrs.getString("lat").equals("null") ? 0 : attrs.getDouble("lat"));
        address.setLongitude(attrs.getString("lng").equals("null") ? 0 : attrs.getDouble("lng"));
        need.setAddress(address);

        need.setStart(TimeUtils.FORMAT_API.parse(attrs.getString("start-time")));
        need.setEnd(TimeUtils.FORMAT_API.parse(attrs.getString("end-time")));
        need.setDate(TimeUtils.formatHumanFriendlyShortDate(context, need.getStart()) + " " +
                TimeUtils.formatTimeOfDay(need.getStart()) + " - " +
                TimeUtils.formatTimeOfDay(need.getEnd()) + " Uhr");

        need.setCount(attrs.getInt("volunteers-needed"));
        need.setSelfLink(obj.getJSONObject("links").getString("self"));

        return need;
    }

    public Need fromIntent(Intent intent) {
        Need need = new Need();

        Bundle bundle = intent.getExtras();
        need.setId(bundle.getInt("id"));
        need.setCategory(bundle.getInt("category"));

        Address address = new Address(Locale.GERMAN);
        address.setLatitude(bundle.getDouble("latitude"));
        address.setLongitude(bundle.getDouble("longitude"));
        address.setAddressLine(0, bundle.getString("address"));
        need.setAddress(address);

        need.setStart(new Date(bundle.getLong("start")));
        need.setEnd(new Date(bundle.getLong("end")));
        need.setDate(bundle.getString("date"));

        need.setCount(bundle.getInt("count"));
        need.setSelfLink(bundle.getString("selfLink"));

        return need;
    }
}
