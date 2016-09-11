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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.MessagesAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    public static final String NEW_MESSAGE_FILTER = "newMessageFilter";

    private Chat mChat;
    private EditText mMessageInput;
    private ArrayList<Message> mMessages;
    private MessagesAdapter mAdapter;
    private ListView mMessagesListView;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message newMessage = intent.getParcelableExtra(Message.PARCELABLE_KEY);
            if (mChat.getId() != newMessage.getChatId()) {
                return;
            }

            mMessages.add(newMessage);
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle data = getIntent().getExtras();
        mChat = data.getParcelable(Chat.PARCELABLE_KEY);

        setTitle(mChat.getTitle());

        mMessagesListView = (ListView) findViewById(R.id.messagesListView);
        mMessageInput = (EditText) findViewById(R.id.messageInput);

        getMessages();
    }

    @Override
    public void onResume() {
        super.onResume();
        getApplicationContext().registerReceiver(mMessageReceiver, new IntentFilter(ChatActivity.NEW_MESSAGE_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getApplicationContext().unregisterReceiver(mMessageReceiver);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        return true;
    }

    public void getMessages() {
        final Request request = Routes.chatMessagesRequest(getApplicationContext(), mChat.getId());
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HttpClient.getInstance().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("ERROR", e.toString());
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        try {
                            mMessages = Message.fromJsonArray(new JSONArray(response.body().string()));
                            mAdapter = new MessagesAdapter(getApplicationContext(), R.layout.message_in_list_item, mMessages, mChat);
                            mMessagesListView.setAdapter(mAdapter);
                        } catch (IOException | JSONException e) {
                            Log.e("ERROR", e.toString());
                        } finally {
                            response.body().close();
                        }
                    }
                });
            }
        });
    }

    public void sendMessage(View view) {
        String content = mMessageInput.getText().toString();

        if (content.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Can't send empty messages.", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = Routes.messagesCreateRequest(getApplicationContext(), mChat.getId(), content);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                response.body().close();
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMessageInput.setText("");
                    }
                });
            }
        });
    }

    public static Intent getIntent(Context context, Chat chat) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Chat.PARCELABLE_KEY, chat);
        return intent;
    }
}
