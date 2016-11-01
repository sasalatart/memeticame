package com.salatart.memeticame.Activities;

import android.app.Activity;
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

import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.AttachmentUtils;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Views.MemeGalleryAdapter;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemeGalleryActivity extends AppCompatActivity {

    @BindView(R.id.grid_view_memes) GridView mGridView;

    private ArrayList<Attachment> mMemes = new ArrayList<>();
    private int mMode;

    public static Intent getIntent(Context context, int mode) {
        Intent intent = new Intent(context, MemeGalleryActivity.class);
        intent.putExtra(Meme.GALLERY_MODE_KEY, mode);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_gallery);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("Meme Gallery");

        mMemes.addAll(AttachmentUtils.attachmentsFromDir(MemeGalleryActivity.this, new File(FileUtils.getMemeticameMemeaudiosDirectory())));
        mMemes.addAll(AttachmentUtils.attachmentsFromDir(MemeGalleryActivity.this, new File(FileUtils.getMemeticameMemesDirectory())));

        mMode = getIntent().getExtras().getInt(Meme.GALLERY_MODE_KEY);
        setAdapter();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public void setAdapter() {
        MemeGalleryAdapter mAdapter = new MemeGalleryAdapter(MemeGalleryActivity.this, R.layout.grid_item_meme, mMemes);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Attachment attachment = mMemes.get(position);

                if (mMode == 0) {
                    FileUtils.openFile(MemeGalleryActivity.this, attachment);
                } else {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(Attachment.PARCELABLE_KEY, Uri.parse(attachment.getStringUri()));
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }
}
