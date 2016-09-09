package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.R;

/**
 * Created by sasalatart on 8/29/16.
 */
public class ChatsAdapter extends ArrayAdapter<Chat> {
    private ArrayList<Chat> mChats;
    private LayoutInflater mLayoutInflater;

    public ChatsAdapter(Context context, int resource, ArrayList<Chat> chats) {
        super(context, resource, chats);
        mChats = chats;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Return the view of a row.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Chat chat = mChats.get(position);

        if (view == null) {
            if (chat.isGroup()) {
                view = mLayoutInflater.inflate(R.layout.chat_group_list_item, parent, false);
            } else {
                view = mLayoutInflater.inflate(R.layout.chat_individual_list_item, parent, false);
            }
        }

        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(chat.getTitle());
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = getItem(position);

        if (chat.isGroup()) {
            return 0;
        } else {
            return 1;
        }
    }
}
