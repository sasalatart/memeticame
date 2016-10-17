package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Views.GalleryAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryActivity extends AppCompatActivity {

    @BindView(R.id.list_view_gallery) ListView mGalleryListView;

    private ArrayList<Attachment> mAttachments;
    private GalleryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("Gallery");

        setGallery();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return true;
    }

    public void setGallery() {
        mAttachments = FileUtils.getAllDownloadedAttachments(GalleryActivity.this);
        mAdapter = new GalleryAdapter(GalleryActivity.this, R.layout.list_item_attachment, mAttachments);
        mGalleryListView.setAdapter(mAdapter);
        mGalleryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Attachment attachment = mAdapter.getItem(position);
                if (attachment != null && !FileUtils.openFile(GalleryActivity.this, attachment)) {
                    Toast.makeText(GalleryActivity.this, "Can't open this file.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
