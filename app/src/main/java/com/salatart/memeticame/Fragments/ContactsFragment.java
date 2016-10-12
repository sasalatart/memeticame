package com.salatart.memeticame.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
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
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.ContactsAdapter;

import java.util.ArrayList;

import static com.salatart.memeticame.Utils.ContactsUtils.PERMISSIONS_REQUEST_READ_CONTACTS;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    public static final String NEW_USER_FILTER = "newUserFilter";
    public static final String CONTACTS_STATE = "contactsFragmentState";
    public static final int REQUEST_NEW_CONTACT = 1;

    private ArrayList<User> mLocalContacts = new ArrayList<>();
    private ArrayList<User> mContacts = new ArrayList<>();
    private ContactsAdapter mAdapter;
    private ListView mContactsListView;

    private OnContactSelected mContactSelectedListener;
    private Routes.OnLogout mOnLogoutListener;

    private BroadcastReceiver mUsersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            User user = intent.getParcelableExtra(User.PARCELABLE_KEY);
            if (User.isPresent(mLocalContacts, user)) {
                mContacts.add(user);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mContactsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mLocalContacts = intent.getParcelableArrayListExtra(ContactsUtils.LOCAL_CONTACTS_PARCELABLE_KEY);
            mContacts = intent.getParcelableArrayListExtra(ContactsUtils.INTERSECTED_CONTACTS_PARCELABLE_KEY);
            mAdapter = new ContactsAdapter(getContext(), R.layout.list_item_contact, mContacts);
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mContactsListView.setAdapter(mAdapter);
                }
            });
        }
    };


    public ContactsFragment() {
        // Required empty public constructor
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mContacts = savedInstanceState.getParcelableArrayList(CONTACTS_STATE);
        }
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

        if (savedInstanceState != null) {
            mContacts = savedInstanceState.getParcelableArrayList(CONTACTS_STATE);
        }

        if (mContacts != null && mContacts.size() != 0) {
            mAdapter = new ContactsAdapter(getContext(), R.layout.list_item_contact, mContacts);
            mContactsListView.setAdapter(mAdapter);
        } else {
            ContactsUtils.retrieveContacts(getActivity());
        }

        setHasOptionsMenu(true);

        return view;
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
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mUsersReceiver, new IntentFilter(NEW_USER_FILTER));
        getActivity().registerReceiver(mContactsReceiver, new IntentFilter(ContactsUtils.RETRIEVE_CONTACTS_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mUsersReceiver);
        getActivity().unregisterReceiver(mContactsReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(CONTACTS_STATE, mContacts);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_CONTACT) {
            ContactsUtils.retrieveContacts(getActivity());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ContactsUtils.retrieveContacts(getActivity());
                } else {
                    // Disable the functionality that depends on this permission.
                }

                return;
            }
        }
    }

    public interface OnContactSelected {
        void onContactSelected(User user);
    }
}
