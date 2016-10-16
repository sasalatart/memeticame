package com.salatart.memeticame.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.ContactsUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.ParserUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.ContactsSelectAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class AddParticipantsActivity extends AppCompatActivity {

    private ArrayList<User> mUsers = new ArrayList<>();
    private ArrayList<User> mSelectedUsers = new ArrayList<>();
    private ArrayList<User> mInvitedUsers = new ArrayList<>();

    @BindView(R.id.list_view_users_to_add) ListView mUsersListView;
    private ContactsSelectAdapter mAdapter;

    private Chat mChat;

    private BroadcastReceiver mContactsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mUsers = intent.getParcelableArrayListExtra(ContactsUtils.INTERSECTED_CONTACTS_PARCELABLE_KEY);
            mUsers = User.difference(mUsers, mChat.getParticipants());
            mAdapter = new ContactsSelectAdapter(AddParticipantsActivity.this, R.layout.list_item_contact, mUsers, mSelectedUsers, mInvitedUsers);
            AddParticipantsActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mUsersListView.setAdapter(mAdapter);
                }
            });
            getInvitations();
        }
    };

    private BroadcastReceiver mUsersKickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Chat chat = intent.getParcelableExtra(Chat.PARCELABLE_KEY);
            User user = intent.getParcelableExtra(User.PARCELABLE_KEY);
            if (mChat.getId() == chat.getId() && mChat.onUserRemoved(AddParticipantsActivity.this, user)) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mNewChatInvitationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatInvitation chatInvitation = intent.getParcelableExtra(ChatInvitation.PARCELABLE_KEY);
            if (chatInvitation.getChatId() == mChat.getId()) {
                mInvitedUsers.add(chatInvitation.getUser());
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mChatInvitationAcceptedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatInvitation chatInvitation = intent.getParcelableExtra(ChatInvitation.PARCELABLE_KEY);
            if (chatInvitation.getChatId() == mChat.getId()) {
                mChat.getParticipants().add(chatInvitation.getUser());
                User.removeFromList(mUsers, chatInvitation.getUser());
                User.removeFromList(mSelectedUsers, chatInvitation.getUser());
                User.removeFromList(mInvitedUsers, chatInvitation.getUser());
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mChatInvitationRejectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatInvitation chatInvitation = intent.getParcelableExtra(ChatInvitation.PARCELABLE_KEY);
            if (chatInvitation.getChatId() == mChat.getId()) {
                User.removeFromList(mInvitedUsers, chatInvitation.getUser());
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participants);

        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        mChat = data.getParcelable(Chat.PARCELABLE_KEY);

        mUsersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedContact = mUsers.get(position);
                if (mSelectedUsers.contains(selectedContact)) {
                    mSelectedUsers.remove(selectedContact);
                } else {
                    mSelectedUsers.add(selectedContact);
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        ContactsUtils.retrieveContacts(AddParticipantsActivity.this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("Add users to group");
    }

    @Override
    public void onResume() {
        super.onResume();
        AddParticipantsActivity.this.registerReceiver(mContactsReceiver, new IntentFilter(ContactsUtils.RETRIEVE_CONTACTS_FILTER));
        AddParticipantsActivity.this.registerReceiver(mUsersKickedReceiver, new IntentFilter(FilterUtils.USER_KICKED_FILTER));
        AddParticipantsActivity.this.registerReceiver(mNewChatInvitationsReceiver, new IntentFilter(FilterUtils.NEW_CHAT_INVITATION_FILTER));
        AddParticipantsActivity.this.registerReceiver(mChatInvitationAcceptedReceiver, new IntentFilter(FilterUtils.CHAT_INVITATION_ACCEPTED_FILTER));
        AddParticipantsActivity.this.registerReceiver(mChatInvitationRejectedReceiver, new IntentFilter(FilterUtils.CHAT_INVITATION_REJECTED_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        AddParticipantsActivity.this.unregisterReceiver(mContactsReceiver);
        AddParticipantsActivity.this.unregisterReceiver(mUsersKickedReceiver);
        AddParticipantsActivity.this.unregisterReceiver(mNewChatInvitationsReceiver);
        AddParticipantsActivity.this.unregisterReceiver(mChatInvitationAcceptedReceiver);
        AddParticipantsActivity.this.unregisterReceiver(mChatInvitationRejectedReceiver);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(ChatActivity.getIntent(AddParticipantsActivity.this, mChat));
        finish();
        return true;
    }

    public void getInvitations() {
        Request request = Routes.chatInvitationsFromChatRequest(AddParticipantsActivity.this, mChat);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        ArrayList<ChatInvitation> chatInvitations = ParserUtils.chatInvitationsFromJsonArray(new JSONArray(response.body().string()));
                        for (ChatInvitation chatInvitation : chatInvitations) {
                            mInvitedUsers.add(chatInvitation.getUser());
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch(JSONException e) {
                        Log.e("ERROR", e.toString());
                    }
                } else {
                    HttpClient.parseErrorMessage(response);
                }
                response.body().close();
            }
        });
    }

    public void addParticipants(View view) {
        if (mSelectedUsers.size() == 0) {
            Toast.makeText(AddParticipantsActivity.this, "You must select at least one participant.", Toast.LENGTH_LONG).show();
            return;
        }

        Request request = Routes.inviteUsersRequest(AddParticipantsActivity.this, mChat, mSelectedUsers);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    startActivity(ChatActivity.getIntent(AddParticipantsActivity.this, mChat));
                } else {
                    AddParticipantsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddParticipantsActivity.this, HttpClient.parseErrorMessage(response), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                response.body().close();
            }
        });
    }
}
