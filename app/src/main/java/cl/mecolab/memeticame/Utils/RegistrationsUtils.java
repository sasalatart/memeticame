package cl.mecolab.memeticame.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by sasalatart on 8/28/16.
 */
public class RegistrationsUtils {
    public static Request buildSignupRequest(String name, String phoneNumber, String password) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("phone_number", phoneNumber);
        params.put("password", password);

        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());

        return new Request.Builder()
                .url(Routes.SIGNUP_URL)
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }
}
