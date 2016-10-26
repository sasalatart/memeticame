package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.GridView;

import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.PlainMemeUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.MemeGalleryAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class PlainMemeGalleryActivity extends AppCompatActivity {

    @BindView(R.id.grid_view_plain_memes) GridView mGridView;
    @BindView(R.id.loading_memes) com.wang.avi.AVLoadingIndicatorView mLoading;

    private ArrayList<String[]> mPlainMemes;
    private MemeGalleryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plain_meme_gallery);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("Plain Meme Gallery");

        setAdapter();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return true;
    }

    public void setAdapter() {
        Request request = Routes.plainMemesIndex(PlainMemeGalleryActivity.this);
        PlainMemeUtils.indexRequest(PlainMemeGalleryActivity.this, request, mLoading, new OnRequestIndexListener<String[]>() {
            @Override
            public void OnSuccess(final ArrayList<String[]> plainMemes) {
                PlainMemeGalleryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPlainMemes = plainMemes;
                        mAdapter = new MemeGalleryAdapter(PlainMemeGalleryActivity.this, mPlainMemes);
                        mGridView.setAdapter(mAdapter);
                        mLoading.hide();
                    }
                });
            }
        });
    }
}
