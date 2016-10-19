package com.salatart.memeticame.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.salatart.memeticame.Listeners.OnContactsReadListener;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.ContactsUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Views.ContactsAdapter;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {

    private ArrayList<User> mLocalContacts = new ArrayList<>();
    private ArrayList<User> mContacts = new ArrayList<>();
    private ContactsAdapter mAdapter;
    private ListView mContactsListView;

    private OnContactSelected mContactSelectedListener;

    private BroadcastReceiver mUsersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            User user = intent.getParcelableExtra(User.PARCELABLE_KEY);
            if (User.isPresent(mLocalContacts, user.getPhoneNumber())) {
                mContacts.add(user);
                mAdapter.notifyDataSetChanged();
            }
        }
    };


    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        mContactsListView = (ListView) view.findViewById(R.id.contacts_list_view);

        setContacts();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mContactSelectedListener = (OnContactSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onViewSelected");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mUsersReceiver, new IntentFilter(FilterUtils.NEW_USER_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mUsersReceiver);
    }

    public void setContacts() {
        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mContactSelectedListener.onContactSelected(mContacts.get(position));
            }
        });

        mContacts = User.findAll();
        setAdapter();
        ContactsUtils.retrieveContacts(getActivity(), new OnContactsReadListener() {
            @Override
            public void OnRead(ArrayList<User> intersectedContacts, ArrayList<User> localContacts) {
                mLocalContacts = localContacts;
                mContacts = intersectedContacts;
                setAdapter();
            }
        });
    }

    public void setAdapter() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter = new ContactsAdapter(getContext(), R.layout.list_item_contact, mContacts);
                mContactsListView.setAdapter(mAdapter);
            }
        });
    }

    public interface OnContactSelected {
        void onContactSelected(User user);
    }
}
