package cl.mecolab.memeticame.Models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andres Matte on 8/10/2016.
 */
public class User {
    private String mName;
    private String mPhoneNumber;

    public User(String name, String phoneNumber) {
        mName = name;
        mPhoneNumber = phoneNumber;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
    }

    public static ArrayList<User> fromJsonArray(JSONArray jsonResponse) throws JSONException {
        ArrayList<User> users = new ArrayList<>();

        for (int i = 0; i < jsonResponse.length(); i++) {
            JSONObject jsonUser = jsonResponse.getJSONObject(i);
            users.add(new User(jsonUser.getString("name"), jsonUser.getString("phone_number")));
        }

        return users;
    }

    public static ArrayList<User> intersect(ArrayList<User> localUsers, ArrayList<User> externalUsers) {
        ArrayList<User> users = new ArrayList<>();

        for (User lU: localUsers) {
            String luPhoneNumber = lU.getPhoneNumber().replace(" ", "").replace("-", "");
            for (User eU: externalUsers) {
                String eUPhoneNumber = eU.getPhoneNumber().replace(" ", "").replace("-", "");
                if (luPhoneNumber.equals(eUPhoneNumber)) {
                    users.add(lU);
                    break;
                }
            }
        }

        return users;
    }
}
