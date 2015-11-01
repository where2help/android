package app.iamin.iamin.util;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import app.iamin.iamin.R;
import app.iamin.iamin.data.model.Need;

/**
 * Created by Markus on 30.10.15.
 */
public class NeedUtils {

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

    public static int getCategory(String category) {
        if (CATEGORY_NAMES.containsKey(category)) {
            return CATEGORY_NAMES.get(category);
        } else {
            return CATEGORY_VOLUNTEER;
        }
    }

    public static String getCategorySingular(int category) {
        switch (category) {
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

    public static String getCategoryPlural(int category) {
        switch (category) {
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

    public static int getCategoryIcon(int category) {
        switch (category) {
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

    public static LatLng getLocation(Need need) {
        return new LatLng(need.getLat(), need.getLng());
    }


    public static Need createNeedfromIntent(Intent intent) {
        Need need = new Need();

        Bundle bundle = intent.getExtras();
        need.setId(bundle.getInt("id"));
        need.setCategory(bundle.getInt("category"));

        need.setLat(bundle.getDouble("latitude"));
        need.setLng(bundle.getDouble("longitude"));
        need.setCity(bundle.getString("city"));
        need.setLocation(bundle.getString("location"));

        need.setStart(new Date(bundle.getLong("start")));
        need.setEnd(new Date(bundle.getLong("end")));
        need.setDate(bundle.getString("date"));

        need.setNeeded(bundle.getInt("needed"));
        need.setCount(bundle.getInt("count"));
        need.setSelfLink(bundle.getString("selfLink"));

        need.setIsAttending(bundle.getBoolean("attending"));

        return need;
    }
}
