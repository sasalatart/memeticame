package com.mecolab.memeticameandroid.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.mecolab.memeticameandroid.Fragments.ChatsFragment;
import com.mecolab.memeticameandroid.Fragments.ContactsFragment;
import com.mecolab.memeticameandroid.Models.Chat;
import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.R;
import com.mecolab.memeticameandroid.Services.IDListenerService;
import com.mecolab.memeticameandroid.Utils.SessionUtils;
import com.mecolab.memeticameandroid.Views.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity implements ChatsFragment.OnChatSelected, ContactsFragment.OnContactSelected {

    private ChatsFragment mChatsFragments;
    private ContactsFragment mContactsFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (SessionUtils.getToken(getApplicationContext()).isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
            setupViewPager(viewPager);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);

            startService(new Intent(getBaseContext(), IDListenerService.class));
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        this.mChatsFragments = new ChatsFragment();
        this.mContactsFragments = new ContactsFragment();
        adapter.addFragment(this.mChatsFragments, "Chats");
        adapter.addFragment(this.mContactsFragments, "Contacts");

        viewPager.setAdapter(adapter);
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
}
