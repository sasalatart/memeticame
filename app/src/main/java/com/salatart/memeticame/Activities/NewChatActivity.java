package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Views.ChatsAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.Request;

public class NewChatActivity extends AppCompatActivity {

    private User mUser;
    private EditText mChatNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("New chat");

        Bundle data = getIntent().getExtras();
        mUser = data.getParcelable(User.PARCELABLE_KEY);
        final ArrayList<Chat> currentChats = data.getParcelableArrayList(Chat.PARCELABLE_ARRAY_KEY);

        LinearLayout existingChatsLayout = (LinearLayout) findViewById(R.id.existing_chats);
        ListView currentChatsListView = (ListView) findViewById(R.id.list_view_existing_chats);
        if (currentChats.size() > 0) {
            existingChatsLayout.setVisibility(View.VISIBLE);
            ChatsAdapter mAdapter = new ChatsAdapter(getApplicationContext(), R.layout.list_item_chat, currentChats);
            currentChatsListView.setAdapter(mAdapter);
            currentChatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(ChatActivity.getIntent(getApplicationContext(), currentChats.get(position)));
                }
            });
        }

        mChatNameInput = (EditText) findViewById(R.id.input_chat_name);
        mChatNameInput.setText("Chat with " + mUser.getName(), TextView.BufferType.EDITABLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (SessionUtils.getToken(getApplicationContext()).isEmpty()) {
            NewChatActivity.this.finish();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        return true;
    }

    public void createChat(View view) {

        String title = this.mChatNameInput.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Title can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = Routes.chatsCreateRequest(getApplicationContext(), title, new ArrayList<>(Arrays.asList(mUser)), false);
        Chat.createFromRequest(NewChatActivity.this, request);
    }
}
