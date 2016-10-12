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
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.ContactsUtils;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.ContactsSelectAdapter;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class AddParticipantsActivity extends AppCompatActivity {
    public static final String USERS_ADDED_FILTER = "usersAddedFilter";

    private ArrayList<User> mUsers = new ArrayList<>();
    private ArrayList<User> mSelectedUsers = new ArrayList<>();

    private ListView mUsersListView;
    private ContactsSelectAdapter mAdapter;

    private Chat mChat;

    private BroadcastReceiver mContactsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mUsers = intent.getParcelableArrayListExtra(ContactsUtils.INTERSECTED_CONTACTS_PARCELABLE_KEY);
            mUsers = User.difference(mUsers, mChat.getParticipants());
            mAdapter = new ContactsSelectAdapter(AddParticipantsActivity.this, R.layout.list_item_contact, mUsers, mSelectedUsers);
            AddParticipantsActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mUsersListView.setAdapter(mAdapter);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participants);

        Bundle data = getIntent().getExtras();
        mChat = data.getParcelable(Chat.PARCELABLE_KEY);

        mUsersListView = (ListView) findViewById(R.id.list_view_users_to_add);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        AddParticipantsActivity.this.unregisterReceiver(mContactsReceiver);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(ChatActivity.getIntent(AddParticipantsActivity.this, mChat));
        finish();
        return true;
    }

    public void addParticipants(View view) {
        if (mSelectedUsers.size() == 0) {
            Toast.makeText(AddParticipantsActivity.this, "You must select at least one participant.", Toast.LENGTH_LONG).show();
            return;
        }

        Request request = Routes.addParticipantsRequest(AddParticipantsActivity.this, mChat, mSelectedUsers);
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
