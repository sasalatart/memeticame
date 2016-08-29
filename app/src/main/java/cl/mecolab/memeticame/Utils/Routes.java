package cl.mecolab.memeticame.Utils;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by sasalatart on 8/27/16.
 */
public class Routes {
    public static String LOGIN_URL = "http://mcctrack4.ing.puc.cl/api/v2/users/login";
    public static String SIGNUP_URL = "http://mcctrack4.ing.puc.cl/api/v2/users";
    public static String USERS_INDEX_URL = "http://mcctrack4.ing.puc.cl/api/v2/users";
    public static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Request buildLoginRequest(String phoneNumber, String password) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("phone_number", phoneNumber);
        params.put("password", password);

        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());

        return new Request.Builder()
                .url(LOGIN_URL)
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }

    public static Request buildSignupRequest(String name, String phoneNumber, String password) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("phone_number", phoneNumber);
        params.put("password", password);

        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());

        return new Request.Builder()
                .url(SIGNUP_URL)
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }

    public static Request buildAllUsersRequest(String token) {
        return new Request.Builder()
                .url(USERS_INDEX_URL)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + token)
                .build();
    }
}
