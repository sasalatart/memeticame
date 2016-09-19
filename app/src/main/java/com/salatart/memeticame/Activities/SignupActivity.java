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
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import com.salatart.memeticame.Models.SignupForm;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.databinding.ActivitySignupBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signup(final View view) {
        if (!mSignupForm.getPassword().equals(mSignupForm.getPasswordConfirmation())) {
            Toast.makeText(getApplicationContext(), "Password must match its confirmation.", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = Routes.signupRequest(mSignupForm.getName(),
                mSignupForm.getPhoneNumber(),
                mSignupForm.getPassword(),
                mSignupForm.getPasswordConfirmation());

        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", "Failed to signup");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    SessionUtils.saveToken(jsonResponse.getString("api_key"), getApplicationContext());
                    SessionUtils.savePhoneNumber(mSignupForm.getPhoneNumber(), getApplicationContext());
                    SessionUtils.registerFCMToken(getApplicationContext());
                    startActivity(new Intent(view.getContext(), MainActivity.class));
                } catch (Exception e) {
                    Log.e("ERROR", e.toString());
                } finally {
                    response.body().close();
                }
            }
        });
    }
}