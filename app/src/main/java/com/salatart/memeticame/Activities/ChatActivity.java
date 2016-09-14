package com.salatart.memeticame.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.MessagesAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    public static final String NEW_MESSAGE_FILTER = "newMessageFilter";
    public static final int PICK_FILE_REQUEST = 1;

    private Chat mChat;
    private ArrayList<Message> mMessages;
    private MessagesAdapter mAdapter;

    private Attachment mCurrentAttachment;

    private EditText mMessageInput;
    private ImageView mAttachmentImageView;
    private ImageButton mCancelButton;
    private ListView mMessagesListView;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message newMessage = intent.getParcelableExtra(Message.PARCELABLE_KEY);

            if (mChat.getId() != newMessage.getChatId()) {
                return;
            }

            if (!newMessage.isMine(getApplicationContext())) {
                mMessages.add(newMessage);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    public static Intent getIntent(Context context, Chat chat) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Chat.PARCELABLE_KEY, chat);
        return intent;
    }

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
        mAttachmentImageView = (ImageView) findViewById(R.id.attachment);
        mCancelButton = (ImageButton) findViewById(R.id.cancelAttachmentButton);

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
        Request request = Routes.chatMessagesRequest(getApplicationContext(), mChat.getId());
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    mMessages = Message.fromJsonArray(new JSONArray(response.body().string()));
                    mAdapter = new MessagesAdapter(getApplicationContext(), R.layout.message_in_text_list_item, mMessages, mChat);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMessagesListView.setAdapter(mAdapter);
                        }
                    });
                } catch (IOException | JSONException e) {
                    Log.e("ERROR", e.toString());
                } finally {
                    response.body().close();
                }
            }
        });
    }

    public void sendMessage(View view) {
        final String content = mMessageInput.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Can't send empty messages.", Toast.LENGTH_SHORT).show();
            return;
        }

        final Message message = createFakeMessage(content);
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageInput.setText("");
            }
        });

        Request request = Routes.messagesCreateRequest(getApplicationContext(), message);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onSendFailure(message);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        mMessages.remove(message);
                        mMessages.add(Message.fromJson(new JSONObject(response.body().string())));
                        ChatActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (IOException | JSONException e) {
                        Log.e("ERROR", e.toString());
                    }
                } else {
                    onSendFailure(message);
                }
                response.body().close();
            }
        });
    }

    public void onSendFailure(final Message message) {
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessages.remove(message);
                mAdapter.notifyDataSetChanged();
                mMessageInput.setText(message.getContent());
                Toast.makeText(getApplicationContext(), "Could not send message", Toast.LENGTH_LONG).show();
            }
        });
    }

    private Message createFakeMessage(String content) {
        Message message = Message.createFake(getApplicationContext(), content, mChat.getId());
        if (mCurrentAttachment != null) {
            message.setAttachment(mCurrentAttachment.clone());
            toggleButtonVisibilities();
            mCurrentAttachment = null;
        }

        mMessages.add(message);
        mAdapter.notifyDataSetChanged();
        return message;
    }

    public void cancelAttachment(View view) {
        toggleButtonVisibilities();
    }

    public void toggleButtonVisibilities() {
        mCancelButton.setVisibility(mCancelButton.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        mAttachmentImageView.setVisibility(mAttachmentImageView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        mAttachmentImageView.setImageResource(R.drawable.ic_image_black_24dp);
    }

    public void selectResource(View view) {
        Intent intent = new Intent();
        intent.setType("image/* video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select file"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                mCurrentAttachment = new Attachment(FileUtils.getName(getApplicationContext(), uri),
                        FileUtils.getMimeType(getApplicationContext(), uri),
                        FileUtils.encodeToBase64(getApplicationContext(), uri),
                        null);
                toggleButtonVisibilities();
            } catch (IOException e) {
                Log.e("ERROR", e.toString());
            }
        }
    }
}
