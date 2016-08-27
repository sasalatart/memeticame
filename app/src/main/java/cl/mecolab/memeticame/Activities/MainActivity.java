package cl.mecolab.memeticame.Activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import cl.mecolab.memeticame.Fragments.ChatsFragment;
import cl.mecolab.memeticame.Fragments.ContactsFragment;
import cl.mecolab.memeticame.Models.User;
import cl.mecolab.memeticame.R;
import cl.mecolab.memeticame.Views.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity implements ContactsFragment.OnContactSelected {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ContactsFragment(), "Contacts");
        adapter.addFragment(new ChatsFragment(), "Chats");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onContactSelected(User user) {

    }
}
