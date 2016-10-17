package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.salatart.memeticame.Fragments.ChatInvitationsFragment;
import com.salatart.memeticame.Fragments.ChatsFragment;
import com.salatart.memeticame.Fragments.ContactsFragment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.ContactsUtils;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Views.ViewPagerAdapter;

import java.io.IOException;

import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static com.salatart.memeticame.Utils.ContactsUtils.PERMISSIONS_REQUEST_READ_CONTACTS;

public class MainActivity extends AppCompatActivity implements ChatsFragment.OnChatSelected,
        ChatsFragment.OnCreateGroupClicked, ContactsFragment.OnContactSelected, Routes.OnLogout {

    private ChatsFragment mChatsFragments;
    private ContactsFragment mContactsFragments;
    private ChatInvitationsFragment mChatInvitationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm.init(this);

        if (SessionUtils.getToken(getApplicationContext()).isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            MainActivity.this.finish();
        }

        setupViewPager();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (SessionUtils.getToken(getApplicationContext()).isEmpty()) {
            MainActivity.this.finish();
        }
    }

    private void setupViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        this.mChatsFragments = new ChatsFragment();
        this.mContactsFragments = new ContactsFragment();
        this.mChatInvitationsFragment = new ChatInvitationsFragment();

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(this.mChatsFragments, "Chats");
        adapter.addFragment(this.mContactsFragments, "Contacts");
        adapter.addFragment(this.mChatInvitationsFragment, "Chat Invitations");

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onContactSelected(User user) {
        Intent intent = new Intent(this, NewChatActivity.class);
        intent.putExtra(User.PARCELABLE_KEY, user);
        intent.putParcelableArrayListExtra(Chat.PARCELABLE_ARRAY_KEY, user.findChats(mChatsFragments.getChats()));
        startActivity(intent);
    }

    @Override
    public void OnChatSelected(Chat chat) {
        startActivity(ChatActivity.getIntent(getApplicationContext(), chat));
    }

    @Override
    public void OnCreateGroupClicked() {
        startActivity(NewChatGroupActivity.getIntent(getApplicationContext()));
    }

    @Override
    public void OnLogout() {
        Request request = Routes.logoutRequest(getApplicationContext());
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SessionUtils.logout(getApplicationContext());
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                MainActivity.this.finish();
                response.body().close();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ContactsUtils.retrieveContacts(MainActivity.this);
                } else {
                    // Disable the functionality that depends on this permission.
                }
            }
        }
    }
}
