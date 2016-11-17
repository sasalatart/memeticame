package com.salatart.memeticame.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.salatart.memeticame.Listeners.OnRequestListener;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Category;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.CategoriesUtils;
import com.salatart.memeticame.Utils.DownloadAsyncTask;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.MemesAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class CategoryActivity extends AppCompatActivity {

    @BindView(R.id.grid_view_memes) GridView mGridView;
    @BindView(R.id.loading_category) com.wang.avi.AVLoadingIndicatorView mLoading;

    private Category mCategory;
    private ProgressDialog mProgressDialog;

    public static Intent getIntent(Context context, Category category) {
        Intent intent = new Intent(context, CategoryActivity.class);
        intent.putExtra(Category.PARCELABLE_KEY, category);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle data = getIntent().getExtras();
        mCategory = data.getParcelable(Category.PARCELABLE_KEY);

        setTitle(mCategory.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCategory();
    }

    public void getCategory() {
        Request request = Routes.categoriesShow(CategoryActivity.this, mCategory);
        CategoriesUtils.showRequest(request, new OnRequestShowListener<Category>() {
            @Override
            public void OnSuccess(final Category category) {
                CategoryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.hide();
                        mCategory = category;
                        setAdapter();
                    }
                });
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequestWithSpinner(CategoryActivity.this, message, mLoading);
            }
        });
    }

    public void setAdapter() {
        final MemesAdapter adapter = new MemesAdapter(CategoryActivity.this, R.layout.grid_item_meme, mCategory.getMemes());
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Meme meme = (Meme) adapter.getItem(position);

                Uri localUri = FileUtils.getUriFromFileName(CategoryActivity.this, meme.getName());
                if (localUri == null) {
                    mProgressDialog = ProgressDialog.show(CategoryActivity.this, "Please wait", "Downloading meme...", true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setMax(100);

                    long downloadId = FileUtils.downloadFile(CategoryActivity.this, Uri.parse(Routes.DOMAIN + "/" + meme.getOriginalUrl()), meme.getName());
                    DownloadAsyncTask asyncTask = new DownloadAsyncTask(CategoryActivity.this, mProgressDialog, downloadId, new OnRequestListener() {
                        @Override
                        public void OnSuccess() {
                        }

                        @Override
                        public void OnFailure(String message) {
                            CallbackUtils.onUnsuccessfulRequest(CategoryActivity.this, message);
                        }
                    });
                    asyncTask.execute();
                }
            }
        });
    }
}
