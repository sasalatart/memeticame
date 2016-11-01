package com.salatart.memeticame.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.PlainMemeUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.PlainMemeGalleryAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class PlainMemeGalleryActivity extends AppCompatActivity {

    @BindView(R.id.grid_view_plain_memes) GridView mGridView;
    @BindView(R.id.loading_memes) com.wang.avi.AVLoadingIndicatorView mLoading;

    private ArrayList<String[]> mPlainMemes;
    private PlainMemeGalleryAdapter mAdapter;

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
        mLoading.show();
        Request request = Routes.plainMemesIndex(PlainMemeGalleryActivity.this);
        PlainMemeUtils.indexRequest(request, new OnRequestIndexListener<String[]>() {
            @Override
            public void OnSuccess(final ArrayList<String[]> plainMemes) {
                PlainMemeGalleryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPlainMemes = plainMemes;
                        mAdapter = new PlainMemeGalleryAdapter(PlainMemeGalleryActivity.this, mPlainMemes);
                        mGridView.setAdapter(mAdapter);
                        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra(Meme.URI_KEY, Routes.DOMAIN + mPlainMemes.get(position)[1]);
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }
                        });
                        mLoading.hide();
                    }
                });
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequestWithSpinner(PlainMemeGalleryActivity.this, message, mLoading);
            }
        });
    }
}
