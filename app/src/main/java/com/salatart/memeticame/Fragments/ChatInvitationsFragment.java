package com.salatart.memeticame.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.ParserUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Views.ChatInvitationsAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/15/16.
 */

public class ChatInvitationsFragment extends Fragment {

    private ArrayList<ChatInvitation> mChatInvitations;
    private ChatInvitationsAdapter mAdapter;
    private ListView mChatInvitationsListView;

    private BroadcastReceiver mChatInvitationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<ChatInvitation> chatInvitations = intent.getParcelableArrayListExtra(ChatInvitation.PARCELABLE_KEY_ARRAY_LIST);
            ChatInvitation myChatInvitation = ChatInvitation.includesUser(chatInvitations, SessionUtils.getPhoneNumber(getContext()));
            if (myChatInvitation != null) {
                mChatInvitations.add(myChatInvitation);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mAdapterResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.notifyDataSetChanged();
        }
    };

    public ChatInvitationsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat_invitations, container, false);

        mChatInvitationsListView = (ListView) view.findViewById(R.id.list_view_chat_invitations);

        setChatInvitations();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mChatInvitationsReceiver, new IntentFilter(FilterUtils.NEW_CHAT_INVITATION_FILTER));
        getActivity().registerReceiver(mAdapterResponseReceiver, new IntentFilter(FilterUtils.CHAT_INVITATIONS_ADAPTER_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mChatInvitationsReceiver);
        getActivity().unregisterReceiver(mAdapterResponseReceiver);
    }

    public void setChatInvitations() {
        Request request = Routes.chatInvitationIndexRequest(getActivity());
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        mChatInvitations = ParserUtils.chatInvitationsFromJsonArray(new JSONArray(response.body().string()));
                        mAdapter = new ChatInvitationsAdapter(getContext(), R.layout.list_item_chat_invitation, mChatInvitations);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mChatInvitationsListView.setAdapter(mAdapter);
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("ERROR", e.toString());
                    }
                } else {
                    HttpClient.parseErrorMessage(response);
                }

                response.body().close();
            }
        });
    }
}
