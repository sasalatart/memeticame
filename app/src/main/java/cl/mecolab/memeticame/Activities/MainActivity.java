package cl.mecolab.memeticame.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import cl.mecolab.memeticame.Fragments.ChatsFragment;
import cl.mecolab.memeticame.Fragments.ContactsFragment;
import cl.mecolab.memeticame.Models.User;
import cl.mecolab.memeticame.R;
import cl.mecolab.memeticame.Utils.SessionUtils;
import cl.mecolab.memeticame.Views.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity implements ContactsFragment.OnContactSelected {

    private ChatsFragment mChatsFragments;
    private ContactsFragment mContactsFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (SessionUtils.getToken(getApplicationContext()).isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
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
        if (user.findChat(this.mChatsFragments.getChats()) != null) {

        } else {
            Intent intent = new Intent(this, NewChatActivity.class);
            intent.putExtra("participant", user);
            startActivity(intent);
        }
    }
}
