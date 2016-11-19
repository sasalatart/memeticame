package com.salatart.memeticame.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.salatart.memeticame.Listeners.OnRequestListener;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.DownloadAsyncTask;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.MemesAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemesActivity extends AppCompatActivity {

    public static String TITLE_KEY = "title";

    protected ArrayList<Meme> mMemes;
    protected MemesAdapter mAdapter;
    protected ProgressDialog mProgressDialog;

    @BindView(R.id.grid_view_memes) GridView mGridView;

    public static Intent getIntent(Context context, ArrayList<Meme> memes, String title) {
        Intent intent = new Intent(context, MemesActivity.class);
        intent.putExtra(Meme.PARCELABLE_ARRAY_KEY, memes);
        intent.putExtra(TITLE_KEY, title);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memes);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle data = getIntent().getExtras();
        mMemes = data.getParcelableArrayList(Meme.PARCELABLE_ARRAY_KEY);
        setTitle(data.getString(TITLE_KEY));

        setAdapter();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public void setAdapter() {
        mAdapter = new MemesAdapter(MemesActivity.this, R.layout.grid_item_meme, mMemes);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Meme meme = (Meme) mAdapter.getItem(position);

                Uri localUri = FileUtils.getUriFromFileName(MemesActivity.this, meme.getName());
                if (localUri == null) {
                    mProgressDialog = ProgressDialog.show(MemesActivity.this, "Please wait", "Downloading meme...", true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setMax(100);

                    long downloadId = FileUtils.downloadFile(MemesActivity.this, Uri.parse(Routes.DOMAIN + "/" + meme.getOriginalUrl()), meme.getName());
                    DownloadAsyncTask asyncTask = new DownloadAsyncTask(MemesActivity.this, mProgressDialog, downloadId, new OnRequestListener() {
                        @Override
                        public void OnSuccess() {
                        }

                        @Override
                        public void OnFailure(String message) {
                            CallbackUtils.onUnsuccessfulRequest(MemesActivity.this, message);
                        }
                    });
                    asyncTask.execute();
                }
            }
        });
    }
}
