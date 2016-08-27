package cl.mecolab.memeticame.Utils;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by sasalatart on 8/27/16.
 */
public class SessionUtils {

    public static String PREFERENCES = "SESSION";

    public static String getToken(SharedPreferences credentials) {
        return credentials.getString("Token", "");
    }

    public static Request buildLoginRequest(String phoneNumber, String password) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        Map<String, String> params = new HashMap<String, String>();
        params.put("phone_number", phoneNumber);
        params.put("password", password);

        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());

        return new Request.Builder()
                .url(RoutesUtils.LOGIN_URL)
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }

    public static void saveToken(String token, SharedPreferences credentials) {
        SharedPreferences.Editor editor = credentials.edit();
        editor.putString("Token", token);
        editor.commit();
    }
}
