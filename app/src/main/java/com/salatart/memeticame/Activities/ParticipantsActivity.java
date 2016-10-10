package com.salatart.memeticame.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Views.ParticipantsAdapter;

public class ParticipantsActivity extends AppCompatActivity {
    public static final String USER_KICKED_FILTER = "userKickedFilter";

    private ParticipantsAdapter mAdapter;
    private Chat mChat;

    private BroadcastReceiver mUsersKickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int chatId = intent.getIntExtra("chat_id", 0);
            int userId = intent.getIntExtra("user_id", 0);
            if (mChat.onUserRemoved(ParticipantsActivity.this, chatId, userId)) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle data = getIntent().getExtras();
        mChat = data.getParcelable(Chat.PARCELABLE_KEY);

        ListView mContactsListView = (ListView) findViewById(R.id.list_view_participants);
        mAdapter = new ParticipantsAdapter(ParticipantsActivity.this, R.layout.list_item_participant, mChat);
        mContactsListView.setAdapter(mAdapter);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mUsersKickedReceiver, new IntentFilter(USER_KICKED_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsersKickedReceiver);
    }
}
