package com.salatart.memeticame.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.MemeUtils;
import com.salatart.memeticame.Utils.Routes;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class SeeMemeActivity extends AppCompatActivity {

    @BindView(R.id.image_meme) ImageView mMemeImageView;
    @BindView(R.id.tag_container) co.lujun.androidtagview.TagContainerLayout mTagContainer;
    @BindView(R.id.meme_rating_bar) com.iarcuschin.simpleratingbar.SimpleRatingBar mRatingBar;

    private Meme mMeme;
    private float mMyRating;
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

        setMeme();
        getMyRating();
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
            showRatingDialog();
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(Meme.PARCELABLE_KEY, mMeme);
            setResult(RESULT_OK, returnIntent);
            finish();
        }

        return true;
    }

    public void setMeme() {
        setTags();
        setMemeImage();
        setRating();
    }

    public void setTags() {
        mTagContainer.setTags(mMeme.getTags());
    }

    public void setMemeImage() {
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

    public void showRatingDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(SeeMemeActivity.this);
        View promptView = layoutInflater.inflate(R.layout.prompt_meme_rating, null);

        final com.iarcuschin.simpleratingbar.SimpleRatingBar myRatingBar = (com.iarcuschin.simpleratingbar.SimpleRatingBar) promptView.findViewById(R.id.my_meme_rating_bar);
        myRatingBar.setRating(mMyRating);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SeeMemeActivity.this);
        alertDialogBuilder.setTitle("Rate this meme");
        alertDialogBuilder.setView(promptView);

        alertDialogBuilder.setPositiveButton("Rate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rate(myRatingBar.getRating());
                dialog.dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        SimpleRatingBar.AnimationBuilder builder = myRatingBar.getAnimationBuilder()
                .setRatingTarget(mMyRating)
                .setDuration(1000)
                .setRepeatCount(0)
                .setInterpolator(new LinearInterpolator());
        builder.start();
    }

    public void getMyRating() {
        Request request = Routes.myRating(SeeMemeActivity.this, mMeme);
        MemeUtils.myRatingRequest(request, new OnRequestShowListener<Float>() {
            @Override
            public void OnSuccess(Float myRating) {
                mMyRating = myRating;
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequest(SeeMemeActivity.this, message);
            }
        });
    }

    public void rate(float rating) {
        Request request = Routes.ratingsCreate(SeeMemeActivity.this, mMeme, rating);
        MemeUtils.rateRequest(request, new OnRequestShowListener<Meme>() {
            @Override
            public void OnSuccess(final Meme meme) {
                SeeMemeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMeme = meme;
                        setMeme();
                    }
                });
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequest(SeeMemeActivity.this, message);
            }
        });
    }
}
