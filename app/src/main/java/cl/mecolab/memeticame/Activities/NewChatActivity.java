package cl.mecolab.memeticame.Activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import cl.mecolab.memeticame.Models.User;
import cl.mecolab.memeticame.R;
import cl.mecolab.memeticame.Utils.HttpClient;
import cl.mecolab.memeticame.Utils.Routes;
import cl.mecolab.memeticame.Utils.SessionUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class NewChatActivity extends AppCompatActivity {

    private ArrayList<User> mParticipants = new ArrayList<>();
    private EditText mChatNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle data = getIntent().getExtras();
        User user = data.getParcelable(User.PARCELABLE_KEY);
        this.mParticipants.add(user);

        this.mChatNameInput = (EditText)findViewById(R.id.chatNameInput);
        this.mChatNameInput.setText("Chat with " + user.getName(), TextView.BufferType.EDITABLE);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        return true;
    }

    public void createChat(View view) {
        String title = this.mChatNameInput.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Title can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = Routes.buildConversationsCreateRequest(getApplicationContext(),
                SessionUtils.getPhoneNumber(getApplicationContext()),
                this.mParticipants,
                this.mParticipants.size() > 1,
                title);

        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
