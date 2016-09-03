package cl.mecolab.memeticame.Utils;

import android.content.Context;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cl.mecolab.memeticame.Models.User;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * Created by sasalatart on 8/27/16.
 */
public class Routes {
    public static String LOGIN_URL = "http://mcctrack4.ing.puc.cl/api/v2/users/login";
    public static String SIGNUP_URL = "http://mcctrack4.ing.puc.cl/api/v2/users";
    public static String USERS_INDEX_URL = "http://mcctrack4.ing.puc.cl/api/v2/users";
    public static String CONVERSATIONS_INDEX_URL = "http://mcctrack4.ing.puc.cl/api/v2/users/get_conversations";
    public static String CONVERSATIONS_CREATE_URL = "http://mcctrack4.ing.puc.cl/api/v2/conversations/";
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

    public static Request buildUserIndexRequest(Context context) {
        return new Request.Builder()
                .url(USERS_INDEX_URL)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request buildConersationsIndexRequest(Context context) {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(CONVERSATIONS_INDEX_URL).newBuilder();
        urlBuilder.addQueryParameter("phone_number", SessionUtils.getPhoneNumber(context));

        return new Request.Builder()
                .url(urlBuilder.build().toString())
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request buildConversationsCreateRequest(Context context, String admin, ArrayList<User> participants, boolean isGroup, String title) {
        ArrayList<String> phoneNumbers = new ArrayList<>();
        for (User u: participants) { phoneNumbers.add(u.getPhoneNumber()); }

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("admin", admin);
        formBuilder.add("group", String.valueOf(isGroup));
        formBuilder.add("title", title);

        for (int i = 0; i < phoneNumbers.toArray().length; i++) {
            formBuilder.add("users[" + i + "]", phoneNumbers.get(i));
        }

        return new Request.Builder()
                .url(CONVERSATIONS_CREATE_URL)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(formBuilder.build())
                .build();
    }

    public static String bodyToString(final Request request){

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "Could not parse the body to a String";
        }
    }
}
