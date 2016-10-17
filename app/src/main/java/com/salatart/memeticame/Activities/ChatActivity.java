package com.salatart.memeticame.Activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.MessageCount;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.AudioManager;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.ParserUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Views.MessagesAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    public static final int REQUEST_PICK_FILE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;
    public static final int REQUEST_VIDEO_CAPTURE = 3;
    public static final int REQUEST_MEMEAUDIO_FILE = 4;
    public static final int PERMISSIONS_CODE = 200;
    public static final String PICTURE_STATE = "pictureState";
    public static final String VIDEO_STATE = "videoState";

    public static boolean sIsActive = false;

    private boolean mPermissionToRecordAudio = false;
    private boolean mPermissionToUseCamera = false;
    private boolean mPermissionToWrite = false;
    private String[] mPermissions = {"android.permission.RECORD_AUDIO", "android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private Chat mChat;
    private MessageCount mMessageCount;
    private MessagesAdapter mAdapter;

    private boolean mCurrentlyRecording;
    private AudioManager mAudioManager;

    private Attachment mCurrentAttachment;
    private Uri mCurrentImageUri;
    private Uri mCurrentVideoUri;

    private EditText mMessageInput;
    private ImageView mAttachmentImageView;
    private ImageButton mCancelButton;
    private ImageButton mRecordButton;
    private ListView mMessagesListView;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message newMessage = intent.getParcelableExtra(Message.PARCELABLE_KEY);
            if (mChat.getId() == newMessage.getChatId() && !newMessage.isMine(getApplicationContext())) {
                mChat.getMessages().add(newMessage);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mOnDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.notifyDataSetChanged();
        }
    };

    private BroadcastReceiver mUsersKickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Chat chat = intent.getParcelableExtra(Chat.PARCELABLE_KEY);
            User user = intent.getParcelableExtra(User.PARCELABLE_KEY);
            if (mChat.getId() == chat.getId()) {
                mChat.onUserRemoved(ChatActivity.this, user);
            }
        }
    };

    private BroadcastReceiver mUserAcceptedInvitationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatInvitation chatInvitation = intent.getParcelableExtra(ChatInvitation.PARCELABLE_KEY);
            if (chatInvitation.getChatId() == mChat.getId()) {
                mChat.getParticipants().add(chatInvitation.getUser());
            }
        }
    };

    private BroadcastReceiver mZipReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
            startActivity(getIntent());
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

        if (savedInstanceState != null) {
            mCurrentImageUri = savedInstanceState.getParcelable(PICTURE_STATE);
            mCurrentVideoUri = savedInstanceState.getParcelable(VIDEO_STATE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasMediaPermissions()) {
            requestPermissions(mPermissions, PERMISSIONS_CODE);
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle data = getIntent().getExtras();
        mChat = data.getParcelable(Chat.PARCELABLE_KEY);

        mMessageCount = MessageCount.findOne(mChat);
        mMessageCount.update(mChat, mChat.getMessages().size(), 0);

        setTitle(mChat.getTitle());

        mMessagesListView = (ListView) findViewById(R.id.list_view_messages);
        mAdapter = new MessagesAdapter(ChatActivity.this, R.layout.list_item_message_in_text, mChat);
        mMessagesListView.setAdapter(mAdapter);

        mMessageInput = (EditText) findViewById(R.id.input_message);
        mAttachmentImageView = (ImageView) findViewById(R.id.attachment);
        mCancelButton = (ImageButton) findViewById(R.id.button_cancel_attachment);
        mRecordButton = (ImageButton) findViewById(R.id.take_audio);

        mMessagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Attachment attachment = mAdapter.getItem(position).getAttachment();
                if (attachment != null && !FileUtils.openFile(ChatActivity.this, attachment)) {
                    Toast.makeText(ChatActivity.this, "Can't open this file.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAudioManager = new AudioManager();
        mCurrentlyRecording = false;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (SessionUtils.getToken(getApplicationContext()).isEmpty()) {
            ChatActivity.this.finish();
        }

        getChat();

        registerReceiver(mMessageReceiver, new IntentFilter(FilterUtils.NEW_MESSAGE_FILTER));
        registerReceiver(mOnDownloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(mUsersKickedReceiver, new IntentFilter(FilterUtils.USER_KICKED_FILTER));
        registerReceiver(mUserAcceptedInvitationReceiver, new IntentFilter(FilterUtils.CHAT_INVITATION_ACCEPTED_FILTER));
        registerReceiver(mZipReceiver, new IntentFilter(FilterUtils.UNZIP_FILTER));
        sIsActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mOnDownloadReceiver);
        unregisterReceiver(mUsersKickedReceiver);
        unregisterReceiver(mUserAcceptedInvitationReceiver);
        unregisterReceiver(mZipReceiver);
        mMessageCount.update(mChat, mChat.getMessages().size(), 0);
        sIsActive = false;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(PICTURE_STATE, mCurrentImageUri);
        savedInstanceState.putParcelable(VIDEO_STATE, mCurrentVideoUri);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        MenuItem addParticipantsItem = menu.findItem(R.id.action_add_participants);
        addParticipantsItem.setVisible(mChat.isGroup());
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_see_participants) {
            Intent intent = new Intent(this, ParticipantsActivity.class);
            intent.putExtra(Chat.PARCELABLE_KEY, mChat);
            startActivity(intent);
        } else if (id == R.id.action_add_participants) {
            Intent intent = new Intent(this, AddParticipantsActivity.class);
            intent.putExtra(Chat.PARCELABLE_KEY, mChat);
            startActivity(intent);
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }

        return true;
    }

    public void getChat() {
        Request request = Routes.chatRequest(getApplicationContext(), mChat);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        mChat = ParserUtils.chatFromJson(new JSONObject(response.body().string()));
                        mAdapter = new MessagesAdapter(getApplicationContext(), R.layout.list_item_message_in_text, mChat);
                        ChatActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMessagesListView.setAdapter(mAdapter);
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("ERROR", e.toString());
                    }
                } else {
                    Log.e("ERROR", "Could not retrieve chat");
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
                        mChat.getMessages().remove(message);
                        mChat.getMessages().add(ParserUtils.messageFromJson(new JSONObject(response.body().string())));
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
                mChat.getMessages().remove(message);
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
            toggleAttachmentVisibilities();
            mCurrentAttachment = null;
        }

        mChat.getMessages().add(message);
        mAdapter.notifyDataSetChanged();
        return message;
    }

    public void cancelAttachment(View view) {
        toggleAttachmentVisibilities();
    }

    public void toggleAttachmentVisibilities() {
        mCancelButton.setVisibility(mCancelButton.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        mAttachmentImageView.setVisibility(mAttachmentImageView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

        if (mAttachmentImageView.getVisibility() == View.VISIBLE) {
            if (mCurrentAttachment.isImage() || mCurrentAttachment.isVideo() || mCurrentAttachment.isMemeaudio()) {
                Glide.with(getApplicationContext())
                        .load(mCurrentAttachment.getShowableStringUri(ChatActivity.this))
                        .override(Attachment.IMAGE_THUMB_SIZE, Attachment.IMAGE_THUMB_SIZE)
                        .into(mAttachmentImageView);
            } else if (mCurrentAttachment.isAudio()) {
                mAttachmentImageView.setImageResource(R.drawable.ic_record_voice_over_black_24dp);
            } else {
                mAttachmentImageView.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
            }
        } else {
            mCurrentAttachment = null;
        }
    }

    public void selectMediaResource(View view) {
        startActivityForResult(FileUtils.getSelectFileIntent("*/*"), REQUEST_PICK_FILE);
    }

    public void dispatchTakeMemeaudioIntent(View view) {
        Intent takeMemeaudioIntent = new Intent(ChatActivity.this, MemeaudioActivity.class);
        startActivityForResult(takeMemeaudioIntent, REQUEST_MEMEAUDIO_FILE);
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = FileUtils.createMediaFile(getApplicationContext(), "jpg", Environment.DIRECTORY_PICTURES);
            if (photoFile != null) {
                mCurrentImageUri = FileProvider.getUriForFile(this, "com.salatart.memeticame.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentImageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void dispatchTakeVideoIntent(View view) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File videoFile = FileUtils.createMediaFile(getApplicationContext(), "mp4", Environment.DIRECTORY_PICTURES);
            if (videoFile != null) {
                mCurrentVideoUri = FileProvider.getUriForFile(this, "com.salatart.memeticame.fileprovider", videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentVideoUri);
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    public void toggleRecording(View view) {
        if (mCurrentlyRecording) {
            File audioFile = mAudioManager.stopAudioRecording();
            Uri uri = mAudioManager.addRecordingToMediaLibrary(ChatActivity.this, audioFile);
            setCurrentAttachmentFromUri(uri);
            mRecordButton.setColorFilter(Color.BLACK);
        } else {
            mAudioManager.startAudioRecording(ChatActivity.this);
            mRecordButton.setColorFilter(Color.RED);
        }
        mCurrentlyRecording = !mCurrentlyRecording;
    }

    private void setCurrentAttachmentFromUri(Uri uri) {
        mCurrentAttachment = ParserUtils.attachmentFromUri(ChatActivity.this, uri);

        if (mCurrentAttachment == null) {
            return;
        }

        toggleAttachmentVisibilities();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null) {
            setCurrentAttachmentFromUri(data.getData());
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setCurrentAttachmentFromUri(mCurrentImageUri);
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            setCurrentAttachmentFromUri(mCurrentVideoUri);
        } else if (requestCode == REQUEST_MEMEAUDIO_FILE && resultCode == RESULT_OK && data != null) {
            Uri memeaudioZipUri = (Uri) data.getExtras().get(MemeaudioActivity.MEMEAUDIO_ZIP);
            setCurrentAttachmentFromUri(memeaudioZipUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_CODE:
                mPermissionToRecordAudio = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                mPermissionToUseCamera = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                mPermissionToWrite = (grantResults[2] == PackageManager.PERMISSION_GRANTED);
                break;
        }

        if (!mPermissionToRecordAudio || !mPermissionToUseCamera || !mPermissionToWrite) {
            ChatActivity.super.finish();
        }
    }

    private boolean hasMediaPermissions() {
        boolean canRecordAudio = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean canUseCamera = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean canWriteToStorage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return canRecordAudio && canUseCamera && canWriteToStorage;
    }
}
