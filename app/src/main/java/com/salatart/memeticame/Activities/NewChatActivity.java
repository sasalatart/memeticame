package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Views.ChatsAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

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

        LinearLayout existingChatsLayout = (LinearLayout) findViewById(R.id.existingChats);
        ListView currentChatsListView = (ListView) findViewById(R.id.existingChatsListView);
        if (currentChats.size() > 0) {
            existingChatsLayout.setVisibility(View.VISIBLE);
            ChatsAdapter mAdapter = new ChatsAdapter(getApplicationContext(), R.layout.chat_individual_list_item, currentChats);
            currentChatsListView.setAdapter(mAdapter);
            currentChatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(ChatActivity.getIntent(getApplicationContext(), currentChats.get(position)));
                }
            });
        }

        mChatNameInput = (EditText)findViewById(R.id.chatNameInput);
        mChatNameInput.setText("Chat with " + mUser.getName(), TextView.BufferType.EDITABLE);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        return true;
    }

    public void createChat(View view) {
        String title = this.mChatNameInput.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Title can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = Routes.chatsCreateRequest(getApplicationContext(),
                SessionUtils.getPhoneNumber(getApplicationContext()),
                mUser,
                title);

        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonChat = new JSONObject(response.body().string());
                    startActivity(ChatActivity.getIntent(getApplicationContext(), Chat.fromJson(jsonChat)));
                } catch (JSONException e) {
                    Log.e("ERROR", e.toString());
                } finally {
                    response.body().close();
                }
            }
        });
    }
}
