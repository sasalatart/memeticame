package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;

import java.util.ArrayList;

public class ContactsAdapter extends ArrayAdapter<User> {
    private ArrayList<User> mContacts;
    private LayoutInflater mLayoutInflater;
    private int mResource;

    public ContactsAdapter(Context context, int resource, ArrayList<User> contacts) {
        super(context, resource, contacts);
        mContacts = contacts;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = mLayoutInflater.inflate(mResource, parent, false);
        }

        User user = mContacts.get(position);

        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        TextView phoneView = (TextView) view.findViewById(R.id.contact_phone_number);

        nameView.setText(user.getName());
        phoneView.setText(user.getPhoneNumber());

        return view;
    }
}
