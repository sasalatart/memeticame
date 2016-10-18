package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.MessageCount;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by sasalatart on 8/29/16.
 */
public class ChatsAdapter extends ArrayAdapter<Chat> {
    private ArrayList<Chat> mChats;
    private LayoutInflater mLayoutInflater;

    public ChatsAdapter(Context context, int resource, ArrayList<Chat> chats) {
        super(context, resource, chats);
        mChats = chats;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Chat chat = mChats.get(position);

        if (view == null) {
            if (chat.isGroup()) {
                view = mLayoutInflater.inflate(R.layout.list_item_chat_group, parent, false);
            } else {
                view = mLayoutInflater.inflate(R.layout.list_item_chat, parent, false);
            }
        }

        setTextViews(view, chat);

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

    private void setTextViews(View view, Chat chat) {
        TextView titleView = (TextView) view.findViewById(R.id.label_title);
        titleView.setText(chat.getTitle());

        TextView adminView = (TextView) view.findViewById(R.id.label_admin);
        adminView.setText(chat.getParticipantsHash().get(chat.getAdmin()));

        TextView createdAtView = (TextView) view.findViewById(R.id.label_created_at);
        createdAtView.setText(TimeUtils.parseISODate(chat.getCreatedAt()));

        String unreadMessages = MessageCount.findOne(chat).getUnreadMessages() + "";
        TextView unreadCountView = (TextView) view.findViewById(R.id.label_unread_count);
        unreadCountView.setText(unreadMessages);
    }
}
