package com.salatart.memeticame.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Channel;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.ChannelsUtils;
import com.salatart.memeticame.Utils.Routes;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class NewChannelActivity extends AppCompatActivity {

    public static String NAME_STATE = "nameState";
    public static String CATEGORIES_STATE = "categoriesState";

    @BindView(R.id.input_channel_name) EditText mChannelNameInput;
    @BindView(R.id.layout_categories) LinearLayout mCategoriesLayout;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_channel);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("New channel");

        if (savedInstanceState != null) {
            retrieveSavedInstanceState(savedInstanceState);
        }
    }

    public void retrieveSavedInstanceState(Bundle savedInstanceState) {
        String name = savedInstanceState.getString(NAME_STATE);
        if (name != null) {
            mChannelNameInput.setText(name);
        }

        ArrayList<String> categories = savedInstanceState.getStringArrayList(CATEGORIES_STATE);
        if (categories != null && categories.size() > 0) {
            for (String category : categories) {
                addCategoryInput(category);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(NAME_STATE, mChannelNameInput.getText().toString());
        savedInstanceState.putStringArrayList(CATEGORIES_STATE, getCategories());
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onAddCategoryInput(View view) {
        if (anyInputWithNoText()) return;

        addCategoryInput(null);
    }

    public void addCategoryInput(String content) {
        final EditText categoryInput = new EditText(NewChannelActivity.this);

        if (content != null) {
            categoryInput.setText(content);
        }

        categoryInput.setHint("Enter a name for this category");
        categoryInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) mCategoriesLayout.removeView(categoryInput);
            }
        });

        mCategoriesLayout.addView(categoryInput);
    }

    public ArrayList<String> getCategories() {
        ArrayList<String> categories = new ArrayList<>();

        for (int i = 0; i < mCategoriesLayout.getChildCount(); i++) {
            View child = mCategoriesLayout.getChildAt(i);

            if (!(child instanceof EditText)) continue;

            String categoryName = ((EditText) child).getText().toString();
            if (!categoryName.isEmpty()) {
                categories.add(categoryName);
            }
        }

        return categories;
    }

    public boolean anyInputWithNoText() {
        for (int i = 0; i < mCategoriesLayout.getChildCount(); i++) {
            View child = mCategoriesLayout.getChildAt(i);
            if ((child instanceof EditText) && ((EditText) child).getText().toString().isEmpty()) {
                Toast.makeText(NewChannelActivity.this, "There is still an input with no text.", Toast.LENGTH_LONG).show();
                return true;
            }
        }

        return false;
    }

    public void onCreateClick(View view) {
        String channelName = mChannelNameInput.getText().toString();
        if (channelName.isEmpty()) {
            Toast.makeText(NewChannelActivity.this, "You must add a name for the channel.", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<String> categories = getCategories();
        if (categories.size() == 0) {
            Toast.makeText(NewChannelActivity.this, "You must add at least one category.", Toast.LENGTH_LONG).show();
        } else {
            createChannel(channelName, categories);
        }
    }

    public void createChannel(String channelName, ArrayList<String> categories) {
        mProgressDialog = ProgressDialog.show(NewChannelActivity.this, "Please wait", "Creating channel...", true);
        Request request = Routes.channelsCreate(NewChannelActivity.this, channelName, categories);
        ChannelsUtils.createRequest(request, new OnRequestShowListener<Channel>() {
            @Override
            public void OnSuccess(Channel channel) {
                mProgressDialog.dismiss();
                Intent responseIntent = new Intent();
                responseIntent.putExtra(Channel.PARCELABLE_KEY, channel);
                setResult(RESULT_OK, responseIntent);
                finish();
            }

            @Override
            public void OnFailure(String message) {
                mProgressDialog.dismiss();
                CallbackUtils.onUnsuccessfulRequest(NewChannelActivity.this, message);
            }
        });
    }
}
