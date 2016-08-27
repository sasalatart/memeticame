package cl.mecolab.memeticame.Activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cl.mecolab.memeticame.Models.Credentials;
import cl.mecolab.memeticame.R;
import cl.mecolab.memeticame.Utils.HttpClient;
import cl.mecolab.memeticame.Utils.SessionUtils;
import cl.mecolab.memeticame.databinding.ActivityLoginBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private Credentials mCredentials;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!SessionUtils.getToken(getSharedPreferences(SessionUtils.PREFERENCES, 0)).equals("")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mCredentials = new Credentials("", "");
        binding.setCredentials(mCredentials);
    }

    public void login(final View view) {
        Request request = SessionUtils.buildLoginRequest(mCredentials.getPhoneNumber(), mCredentials.getPassword());

        progressBar.setVisibility(View.VISIBLE);

        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", "Failed to login");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    if (!jsonResponse.getString("api_key").equals("null")) {
                        SessionUtils.saveToken(jsonResponse.getString("api_key"), getSharedPreferences(SessionUtils.PREFERENCES, 0));
                        Intent intent = new Intent(view.getContext(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("ERROR", e.toString());
                }
            }
        });
    }
}
