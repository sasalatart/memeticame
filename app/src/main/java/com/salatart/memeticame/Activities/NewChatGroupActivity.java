package com.salatart.memeticame.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.ContactsUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.ContactsSelectAdapter;

import java.util.ArrayList;

import okhttp3.Request;

import static com.salatart.memeticame.Utils.ContactsUtils.PERMISSIONS_REQUEST_READ_CONTACTS;

public class NewChatGroupActivity extends AppCompatActivity {

    private ArrayList<User> mContacts = new ArrayList<>();
    private ArrayList<User> mSelectedContacts = new ArrayList<>();
    private EditText mGroupNameInput;
    private ListView mContactsListView;
    private ContactsSelectAdapter mAdapter;

    private BroadcastReceiver mContactsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mContacts.size() != 0) {
                return;
            }

            mContacts = intent.getParcelableArrayListExtra(ContactsUtils.INTERSECTED_CONTACTS_PARCELABLE_KEY);
            mAdapter = new ContactsSelectAdapter(NewChatGroupActivity.this, R.layout.list_item_contact, mContacts, mSelectedContacts);
            NewChatGroupActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mContactsListView.setAdapter(mAdapter);
                }
            });
        }
    };

    public static Intent getIntent(Context context) {
        return new Intent(context, NewChatGroupActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat_group);

        mGroupNameInput = (EditText) findViewById(R.id.group_name_input);

        mContactsListView = (ListView) findViewById(R.id.contacts_list_view);
        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedContact = mContacts.get(position);
                if (mSelectedContacts.contains(selectedContact)) {
                    mSelectedContacts.remove(selectedContact);
                } else {
                    mSelectedContacts.add(selectedContact);
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        showContacts();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("New Group Chat");
    }

    @Override
    public void onResume() {
        super.onResume();
        NewChatGroupActivity.this.registerReceiver(mContactsReceiver, new IntentFilter(ContactsUtils.RETRIEVE_CONTACTS_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        NewChatGroupActivity.this.unregisterReceiver(mContactsReceiver);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        return true;
    }

    public void createGroup(View view) {
        if (mSelectedContacts.size() == 0) {
            Toast.makeText(NewChatGroupActivity.this, "You must add participants before creating a group.", Toast.LENGTH_LONG).show();
            return;
        }

        String title = mGroupNameInput.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(NewChatGroupActivity.this, "You must choose a name before creating a group.", Toast.LENGTH_LONG).show();
            return;
        }

        Request request = Routes.chatsCreateRequest(getApplicationContext(), title, mSelectedContacts, true);
        Chat.createFromRequest(NewChatGroupActivity.this, request);
    }

    private void showContacts() {
        ContactsUtils.retrieveContacts(NewChatGroupActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showContacts();
                } else {
                    // Disable the functionality that depends on this permission.
                }

                return;
            }
        }
    }
}
