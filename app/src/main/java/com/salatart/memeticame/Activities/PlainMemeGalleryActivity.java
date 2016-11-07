package com.salatart.memeticame.Activities;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.MemeUtils;
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

    private ProgressDialog mProgressDialog;

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
                                String memeName = MemeUtils.getNameFromUrl(mPlainMemes.get(position)[1]);

                                Uri localUri = FileUtils.getUriFromFileName(PlainMemeGalleryActivity.this, memeName);
                                if (localUri == null) {
                                    mProgressDialog = ProgressDialog.show(PlainMemeGalleryActivity.this, "Please wait", "Downloading meme...", true);
                                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    mProgressDialog.setMax(100);

                                    long downloadId = FileUtils.downloadFile(PlainMemeGalleryActivity.this, Uri.parse(Routes.DOMAIN + "/" + mPlainMemes.get(position)[1]), memeName);
                                    new DownloadAsyncTask(downloadId, memeName).execute();
                                } else {
                                    finishWithUriResult(localUri);
                                }
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

    public void finishWithUriResult(Uri uri) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Meme.URI_KEY, uri);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private class DownloadAsyncTask extends AsyncTask {

        private long mDownloadId;
        private String mDownloadName;

        public DownloadAsyncTask(long downloadId, String downloadName) {
            mDownloadId = downloadId;
            mDownloadName = downloadName;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            while (updateProgressBar()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        public boolean updateProgressBar() {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(mDownloadId);
            try {
                DownloadManager downloadManager = (DownloadManager) PlainMemeGalleryActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor cursor = downloadManager.query(q);
                cursor.moveToFirst();

                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                int downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                float progress = (status == DownloadManager.STATUS_SUCCESSFUL ? 1 : (float) downloaded / (float) total);

                cursor.close();

                if (status == DownloadManager.STATUS_FAILED || status == DownloadManager.STATUS_PAUSED) {
                    mProgressDialog.dismiss();
                    CallbackUtils.onUnsuccessfulRequest(PlainMemeGalleryActivity.this, "Failed to download meme. Try again.");
                } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    mProgressDialog.dismiss();
                    finishWithUriResult(FileUtils.getUriFromFileName(PlainMemeGalleryActivity.this, mDownloadName));
                } else {
                    mProgressDialog.setProgress((int) progress * 100);
                }

                return status != DownloadManager.STATUS_FAILED && status != DownloadManager.STATUS_PAUSED && status != DownloadManager.STATUS_SUCCESSFUL;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
