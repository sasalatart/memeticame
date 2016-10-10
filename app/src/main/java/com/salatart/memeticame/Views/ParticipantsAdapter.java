package com.salatart.memeticame.Views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/9/16.
 */

public class ParticipantsAdapter extends ContactsAdapter {
    private Chat mChat;

    public ParticipantsAdapter(Context context, int resource, Chat chat) {
        super(context, resource, chat.getParticipants());
        mChat = chat;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageButton removeButton = (ImageButton) view.findViewById(R.id.button_kick_from_group);

        User user = mChat.getParticipants().get(position);
        String myPhoneNumber = SessionUtils.getPhoneNumber(getContext());

        boolean myself = user.getPhoneNumber().equals(myPhoneNumber);
        boolean notAdmin = !myPhoneNumber.equals(mChat.getAdmin());

        if (myself || notAdmin) {
            removeButton.setVisibility(View.GONE);
        } else {
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final User userToRemove = mChat.getParticipants().get(position);
                    Request request = Routes.kickUserRequest(getContext(), mChat, userToRemove);
                    HttpClient.getInstance().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("ERROR", e.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                mChat.getParticipants().remove(userToRemove);
                            } else {
                                Log.e("ERROR", "Could not kick this user");
                            }

                            response.body().close();
                        }
                    });
                }
            });
        }

        return view;
    }
}
