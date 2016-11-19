package com.salatart.memeticame.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.MemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SeeMemeActivity extends AppCompatActivity {

    @BindView(R.id.image_meme) ImageView mMemeImageView;
    @BindView(R.id.tag_container) co.lujun.androidtagview.TagContainerLayout mTagContainer;
    @BindView(R.id.meme_rating_bar) com.iarcuschin.simpleratingbar.SimpleRatingBar mRatingBar;

    private Meme mMeme;
    private Uri mLocalUri;

    public static Intent getIntent(Context context, Meme meme) {
        Intent intent = new Intent(context, SeeMemeActivity.class);
        intent.putExtra(Meme.PARCELABLE_KEY, meme);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_meme);

        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        mMeme = data.getParcelable(Meme.PARCELABLE_KEY);
        mLocalUri = FileUtils.getUriFromFileName(SeeMemeActivity.this, MemeUtils.getNameFromUrl(mMeme.getOriginalUrl()));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle(mMeme.getName());

        setTags();
        setMeme();
        setRating();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meme_menu, menu);
        menu.findItem(R.id.action_download_meme).setVisible(mLocalUri == null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_download_meme) {
            FileUtils.downloadFile(SeeMemeActivity.this, Uri.parse(mMeme.getOriginalUrl()), MemeUtils.getNameFromUrl(mMeme.getOriginalUrl()));
            item.setVisible(false);
        } else if (id == R.id.action_rate_meme) {

        } else {
            finish();
        }

        return true;
    }

    public void setTags() {
        mTagContainer.setTags(mMeme.getTags());
    }

    public void setMeme() {
        String url = (mLocalUri == null ? mMeme.getOriginalUrl() : mLocalUri.toString());
        Glide.with(SeeMemeActivity.this)
                .load(url)
                .placeholder(R.drawable.ic_access_time_black_24dp)
                .crossFade()
                .into(mMemeImageView);
    }

    public void setRating() {
        SimpleRatingBar.AnimationBuilder builder = mRatingBar.getAnimationBuilder()
                .setRatingTarget((float) mMeme.getRating())
                .setDuration(1000)
                .setRepeatCount(0)
                .setInterpolator(new LinearInterpolator());
        builder.start();
        mRatingBar.setRating((float) mMeme.getRating());
    }
}
