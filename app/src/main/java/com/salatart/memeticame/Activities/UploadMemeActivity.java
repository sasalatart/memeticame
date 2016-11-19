package com.salatart.memeticame.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.MemeUtils;
import com.salatart.memeticame.Utils.Routes;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class UploadMemeActivity extends AppCompatActivity {

    @BindView(R.id.image_meme) ImageView mMemeImage;
    @BindView(R.id.tags_edit_text) mabbas007.tagsedittext.TagsEditText mTagsEditText;

    private Uri mMemeUri;
    private int mCategoryId;

    private ProgressDialog mProgressDialog;

    public static Intent getIntent(Context context, Uri memeUri, int categoryId) {
        Intent intent = new Intent(context, UploadMemeActivity.class);
        intent.putExtra(Meme.URI_KEY, memeUri);
        intent.putExtra(MemesFromCategoryActivity.CATEGORY_ID_KEY, categoryId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_meme);

        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        mMemeUri = data.getParcelable(Meme.URI_KEY);
        mCategoryId = data.getInt(MemesFromCategoryActivity.CATEGORY_ID_KEY);

        setTitle("Upload meme");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setMeme();
    }

    public void setMeme() {
        Glide.with(UploadMemeActivity.this)
                .load(mMemeUri)
                .placeholder(R.drawable.ic_access_time_black_24dp)
                .crossFade()
                .into(mMemeImage);
    }

    public void onUploadClick(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(UploadMemeActivity.this);
        View promptView = layoutInflater.inflate(R.layout.prompt_meme_name, null);

        final EditText memeNameInput = (EditText) promptView.findViewById(R.id.input_meme_name);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UploadMemeActivity.this);
        alertDialogBuilder.setView(promptView);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String memeName = memeNameInput.getText().toString();
                if (memeName.isEmpty()) {
                    Toast.makeText(UploadMemeActivity.this, "You must insert a name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mProgressDialog = ProgressDialog.show(UploadMemeActivity.this, "Please wait", "Uploading meme...", true);

                uploadMeme(memeName);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void uploadMeme(String memeName) {
        Request request = Routes.memesCreate(UploadMemeActivity.this, mCategoryId, memeName, mTagsEditText.getText().toString().split(" "), mMemeUri);
        MemeUtils.createRequest(request, new OnRequestShowListener<Meme>() {
            @Override
            public void OnSuccess(Meme meme) {
                mProgressDialog.dismiss();
                Intent returnIntent = new Intent();
                returnIntent.putExtra(Meme.PARCELABLE_KEY, meme);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }

            @Override
            public void OnFailure(String message) {
                mProgressDialog.dismiss();
                CallbackUtils.onUnsuccessfulRequest(UploadMemeActivity.this, message);
            }
        });
    }
}
