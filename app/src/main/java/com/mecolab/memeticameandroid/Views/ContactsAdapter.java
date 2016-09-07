package com.mecolab.memeticameandroid.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.R;

/**
 * Custom Contacts Adapter.
 */
public class ContactsAdapter extends ArrayAdapter<User> {
    private ArrayList<User> mContacts;
    private LayoutInflater mLayoutInflater;

    public ContactsAdapter(Context context, int resource, ArrayList<User> contacts) {
        super(context, resource, contacts);
        mContacts = contacts;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Return the view of a row.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.contact_list_item, parent, false);
        }

        User user = mContacts.get(position);

        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        TextView phoneView = (TextView) view.findViewById(R.id.contact_phone_number);

        nameView.setText(user.getName());
        phoneView.setText(user.getPhoneNumber());

        return view;
    }
}
