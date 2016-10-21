package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.salatart.memeticame.Models.SignupForm;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.UserUtils;
import com.salatart.memeticame.databinding.ActivitySignupBinding;

import okhttp3.Request;

public class SignupActivity extends AppCompatActivity {
    private SignupForm mSignupForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ActivitySignupBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        mSignupForm = new SignupForm("", "", "", "");
        binding.setSignupForm(mSignupForm);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.signup_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_login) {
            startActivity(new Intent(this, LoginActivity.class));
            SignupActivity.this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signup(final View submitButton) {
        if (!mSignupForm.getPassword().equals(mSignupForm.getPasswordConfirmation())) {
            Toast.makeText(getApplicationContext(), "Password must match its confirmation.", Toast.LENGTH_SHORT).show();
            return;
        }

        com.wang.avi.AVLoadingIndicatorView loadingSignup = (com.wang.avi.AVLoadingIndicatorView) findViewById(R.id.loading_signup);
        Request request = Routes.signup(mSignupForm.getName(), mSignupForm.getPhoneNumber(), mSignupForm.getPassword(), mSignupForm.getPasswordConfirmation());
        UserUtils.signup(SignupActivity.this, request, mSignupForm.getPhoneNumber(), submitButton, loadingSignup);
    }
}
