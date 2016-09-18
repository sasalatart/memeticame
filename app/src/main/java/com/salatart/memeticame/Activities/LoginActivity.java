package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.salatart.memeticame.Models.LoginForm;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.databinding.ActivityLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private LoginForm mLoginForm;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!SessionUtils.getToken(getApplicationContext()).isEmpty()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mLoginForm = new LoginForm("", "");
        binding.setLoginForm(mLoginForm);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(final View view) {
        Request request = Routes.loginRequest(mLoginForm.getPhoneNumber(), mLoginForm.getPassword());

        progressBar.setVisibility(View.VISIBLE);

        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", "Failed to login");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        SessionUtils.saveToken(jsonResponse.getString("api_key"), getApplicationContext());
                        SessionUtils.savePhoneNumber(mLoginForm.getPhoneNumber(), getApplicationContext());
                        SessionUtils.registerFCMToken(getApplicationContext());
                        startActivity(new Intent(view.getContext(), MainActivity.class));
                    } catch (JSONException e) {
                        Log.e("ERROR", e.toString());
                    }
                } else {
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                response.body().close();
            }
        });
    }
}
