package com.salatart.memeticame.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.salatart.memeticame.Models.User;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Andres Matte on 8/10/2016.
 */
public class ContactsUtils {
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 101;
    public static final String RETRIEVE_CONTACTS_FILTER = "retrieveContactsFilter";
    public static final String LOCAL_CONTACTS_PARCELABLE_KEY = "localContactsParcelableKey";
    public static final String INTERSECTED_CONTACTS_PARCELABLE_KEY = "intersectedContactsParcelableKey";

    public static boolean hasContactsPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void getContacts(Context context, ContactsProviderListener listener) {
        new GetContactsTask(context, listener).execute();
    }

    public static void retrieveContacts(final Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasContactsPermissions(activity)) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            ContactsUtils.getContacts(activity, new ContactsUtils.ContactsProviderListener() {
                @Override
                public void OnContactsReady(final ArrayList<User> contacts) {
                    Request request = Routes.userIndexRequest(activity);
                    HttpClient.getInstance().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("ERROR", e.toString());
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            try {
                                if (activity == null) {
                                    return;
                                }

                                ArrayList<User> localContacts = contacts;
                                ArrayList<User> intersectedContacts = User.intersect(contacts, ParserUtils.usersFromJsonArray(new JSONArray(response.body().string())));
                                Intent intent = new Intent(RETRIEVE_CONTACTS_FILTER);
                                intent.putExtra(LOCAL_CONTACTS_PARCELABLE_KEY, localContacts);
                                intent.putExtra(INTERSECTED_CONTACTS_PARCELABLE_KEY, intersectedContacts);
                                activity.sendBroadcast(intent);
                            } catch (JSONException | IOException e) {
                                Log.e("ERROR", e.toString());
                            } finally {
                                response.body().close();
                            }
                        }
                    });
                }
            });
        }
    }

    public interface ContactsProviderListener {
        void OnContactsReady(ArrayList<User> users);
    }

    /**
     * AsyncTask enables proper and easy use of the UI thread. This class allows to perform
     * background operations and publish results on the UI thread without having to manipulate
     * threads and/or handlers.
     * See <a href="https://developer.android.com/reference/android/os/AsyncTask.html">AsyncTask</a>
     */
    private static class GetContactsTask extends AsyncTask<String, Void, ArrayList<User>> {

        private Context mContext;
        private ContactsProviderListener mListener;
        private ContentResolver mResolver;

        public GetContactsTask(Context context, ContactsProviderListener listener) {
            super();
            this.mContext = context;
            this.mListener = listener;
            this.mResolver = context.getContentResolver();
        }

        @Override
        protected ArrayList<User> doInBackground(String... params) {
            return getPhoneContacts(mContext);
        }

        @Override
        protected void onPostExecute(ArrayList<User> users) {
            mListener.OnContactsReady(users);
        }

        public ArrayList<User> getPhoneContacts(Context context) {
            if (mResolver == null) {
                mResolver = context.getContentResolver();
            }

            ArrayList<User> contacts = new ArrayList<>();
            Cursor cursor = mResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
                do {
                    User contact = getContact(cursor);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            return contacts;
        }

        private User getContact(Cursor cursor) {
            User contact = null;
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor c = mResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null);
                if (c != null && c.getCount() != 0 && c.moveToFirst()) {
                    String phoneNumber = c.getString(c.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String name = c.getString(c.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    contact = new User(-1, name, phoneNumber);
                    c.close();
                }
            }

            return contact;
        }
    }
}
