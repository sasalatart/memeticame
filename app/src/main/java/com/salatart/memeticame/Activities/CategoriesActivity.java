package com.salatart.memeticame.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Category;
import com.salatart.memeticame.Models.Channel;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.ChannelsUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.CategoriesAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class CategoriesActivity extends AppCompatActivity {

    @BindView(R.id.list_view_categories) ListView mCategoriesListView;
    @BindView(R.id.loading_channel) com.wang.avi.AVLoadingIndicatorView mLoading;

    private Channel mChannel;

    public static Intent getIntent(Context context, Channel channel) {
        Intent intent = new Intent(context, CategoriesActivity.class);
        intent.putExtra(Channel.PARCELABLE_KEY, channel);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        mChannel = data.getParcelable(Channel.PARCELABLE_KEY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle(mChannel.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChannel();
    }

    public void getChannel() {
        Request request = Routes.channelsShow(CategoriesActivity.this, mChannel);
        ChannelsUtils.showRequest(request, new OnRequestShowListener<Channel>() {
            @Override
            public void OnSuccess(final Channel channel) {
                CategoriesActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.hide();
                        mChannel = channel;
                        setAdapter();
                    }
                });
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequestWithSpinner(CategoriesActivity.this, message, mLoading);
            }
        });
    }

    public void setAdapter() {
        final CategoriesAdapter adapter = new CategoriesAdapter(CategoriesActivity.this, R.layout.list_item_category, mChannel.getCategories());
        mCategoriesListView.setAdapter(adapter);
        mCategoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category category = adapter.getItem(position);
                startActivity(MemesActivity.getIntent(CategoriesActivity.this, category.getMemes(), category.getName()));
            }
        });
    }
}
