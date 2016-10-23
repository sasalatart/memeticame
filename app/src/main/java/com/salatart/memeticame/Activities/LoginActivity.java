package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.salatart.memeticame.Models.LoginForm;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.databinding.ActivityLoginBinding;

import okhttp3.Request;

public class LoginActivity extends AppCompatActivity {
    private LoginForm mLoginForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (SessionUtils.loggedIn(getApplicationContext())) {
            startActivity(new Intent(this, MainActivity.class));
            LoginActivity.this.finish();
        } else {
            ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
            mLoginForm = new LoginForm("", "");
            binding.setLoginForm(mLoginForm);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_signup) {
            startActivity(new Intent(this, SignupActivity.class));
            LoginActivity.this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(final View view) {
        com.wang.avi.AVLoadingIndicatorView loadingLogin = (com.wang.avi.AVLoadingIndicatorView) findViewById(R.id.loading_login);
        Request request = Routes.login(mLoginForm.getPhoneNumber(), mLoginForm.getPassword());
        SessionUtils.login(LoginActivity.this, request, mLoginForm.getPhoneNumber(), view, loadingLogin);
    }
}
