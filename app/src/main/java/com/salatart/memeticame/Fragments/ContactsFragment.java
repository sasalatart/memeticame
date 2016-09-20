package com.salatart.memeticame.Fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.ContactsUtils;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.ContactsAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    public static final String TAG = "contacts_fragment";
    public static final int REQUEST_NEW_CONTACT = 1;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    private ArrayList<User> mContacts;
    private ContactsAdapter mAdapter;
    private ListView mContactsListView;
    private OnContactSelected mContactSelectedListener;
    private Routes.OnLogout mOnLogoutListener;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        mContactsListView = (ListView) view.findViewById(R.id.contacts_list_view);
        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mContactSelectedListener.onContactSelected(mContacts.get(position));
            }
        });

        showContacts();

        setHasOptionsMenu(true);

        return view;
    }

    /**
     * Checks if the app has permission to read phone contacts.
     * Only for SDK > 23.
     */
    private boolean hasContactsPermissions() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED;
    }

    private void showContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasContactsPermissions()) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            ContactsUtils.getContacts(getContext(), new ContactsUtils.ContactsProviderListener() {
                @Override
                public void OnContactsReady(final ArrayList<User> contacts) {
                    Request request = Routes.userIndexRequest(getContext());
                    HttpClient.getInstance().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("ERROR", e.toString());
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            try {
                                mContacts = User.intersect(contacts, User.fromJsonArray(new JSONArray(response.body().string())));
                                mAdapter = new ContactsAdapter(getContext(), R.layout.contact_list_item, mContacts);
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        mContactsListView.setAdapter(mAdapter);
                                    }
                                });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showContacts();
                } else {
                    // Disable the functionality that depends on this permission.
                }

                return;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contacts_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_contact) {
            startActivityForResult(new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI), REQUEST_NEW_CONTACT);
            return true;
        } else if (id == R.id.action_logout) {
            mOnLogoutListener.OnLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mContactSelectedListener = (OnContactSelected) context;
            mOnLogoutListener = (Routes.OnLogout) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onViewSelected");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_CONTACT) {
            showContacts();
        }
    }

    public interface OnContactSelected {
        void onContactSelected(User user);
    }
}
