package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Models.Channel;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.ChannelsUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.ChannelsAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class ChannelsActivity extends AppCompatActivity {

    @BindView(R.id.list_view_channels) ListView mChannelsListView;
    @BindView(R.id.loading_channels) com.wang.avi.AVLoadingIndicatorView mLoading;

    private ChannelsAdapter mAdapter;
    private ArrayList<Channel> mChannels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("Channels");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChannels();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.channels_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_create_channel) {
            Intent intent = new Intent(ChannelsActivity.this, NewChannelActivity.class);
            startActivityForResult(intent, FilterUtils.REQUEST_CREATE_CHANNEL);
        }

        return super.onOptionsItemSelected(item);
    }

    public void getChannels() {
        Request request = Routes.channelsIndex(ChannelsActivity.this);
        ChannelsUtils.indexRequest(request, new OnRequestIndexListener<Channel>() {
            @Override
            public void OnSuccess(final ArrayList<Channel> channels) {
                ChannelsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.hide();
                        mChannels = channels;
                        setAdapter();
                    }
                });
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequestWithSpinner(ChannelsActivity.this, message, mLoading);
            }
        });
    }

    public void setAdapter() {
        mAdapter = new ChannelsAdapter(ChannelsActivity.this, R.layout.list_item_channel, mChannels);
        mChannelsListView.setAdapter(mAdapter);
        mChannelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(CategoriesActivity.getIntent(ChannelsActivity.this, mChannels.get(position)));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilterUtils.REQUEST_CREATE_CHANNEL && resultCode == RESULT_OK && data != null) {
            mChannels.add((Channel) data.getParcelableExtra(Channel.PARCELABLE_KEY));
            mAdapter.notifyDataSetChanged();
        }
    }
}
