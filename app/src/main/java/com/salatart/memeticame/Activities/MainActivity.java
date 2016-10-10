package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.salatart.memeticame.Fragments.ChatsFragment;
import com.salatart.memeticame.Fragments.ContactsFragment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Views.ViewPagerAdapter;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ChatsFragment.OnChatSelected,
        ChatsFragment.OnCreateGroupClicked, ContactsFragment.OnContactSelected, Routes.OnLogout {

    private ChatsFragment mChatsFragments;
    private ContactsFragment mContactsFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (SessionUtils.getToken(getApplicationContext()).isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            MainActivity.this.finish();
        } else if (savedInstanceState != null) {
            this.mChatsFragments = (ChatsFragment) getSupportFragmentManager().getFragment(savedInstanceState, "chatsFragment");
            this.mContactsFragments = (ContactsFragment) getSupportFragmentManager().getFragment(savedInstanceState, "contactsFragment");
        } else {
            this.mChatsFragments = new ChatsFragment();
            this.mContactsFragments = new ContactsFragment();
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (SessionUtils.getToken(getApplicationContext()).isEmpty()) {
            MainActivity.this.finish();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(this.mChatsFragments, "Chats");
        adapter.addFragment(this.mContactsFragments, "Contacts");

        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "contactsFragment", mContactsFragments);
        getSupportFragmentManager().putFragment(outState, "chatsFragment", mChatsFragments);
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
}
