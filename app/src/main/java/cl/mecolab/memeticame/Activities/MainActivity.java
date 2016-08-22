package cl.mecolab.memeticame.Activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import cl.mecolab.memeticame.Fragments.ContactsFragment;
import cl.mecolab.memeticame.Models.User;
import cl.mecolab.memeticame.R;

public class MainActivity extends AppCompatActivity implements
        ContactsFragment.OnContactSelected {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // If fragment is already added, replace it.
        if (getSupportFragmentManager().findFragmentByTag(ContactsFragment.TAG) != null) {
            transaction = transaction.replace(R.id.main_container,
                    new ContactsFragment(), null);
        } else {
            transaction = transaction.add(R.id.main_container,
                    new ContactsFragment(), ContactsFragment.TAG);
        }
        transaction.commit();
    }

    /**
     * Called when contact is selected on ContactsFragment.
     *
     * @param user Contact selected.
     */
    @Override
    public void onContactSelected(User user) {

    }
}
