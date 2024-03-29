package com.salatart.memeticame.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.MemeUtils;

import java.util.ArrayList;

public class MemesFromCategoryActivity extends MemesActivity {

    public static String CATEGORY_ID_KEY = "categoryIdKey";

    private int mCategoryId;

    public static Intent getIntent(Context context, ArrayList<Meme> memes, String title, int categoryId) {
        Intent intent = new Intent(context, MemesFromCategoryActivity.class);
        intent.putExtra(Meme.PARCELABLE_ARRAY_KEY, memes);
        intent.putExtra(TITLE_KEY, title);
        intent.putExtra(CATEGORY_ID_KEY, categoryId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle data = getIntent().getExtras();
        mCategoryId = data.getInt(CATEGORY_ID_KEY);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_upload_meme) {
            startActivityForResult(MemeGalleryActivity.getIntent(MemesFromCategoryActivity.this, MemeGalleryActivity.Mode.PickTextMeme), FilterUtils.REQUEST_PICK_MEME);
        } else if (id == R.id.action_search_memes) {
            MemeUtils.onSearchClick(MemesFromCategoryActivity.this);
        } else {
            finish();
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilterUtils.REQUEST_PICK_MEME && resultCode == RESULT_OK && data != null) {
            Uri memeUri = (Uri) data.getExtras().get(Meme.URI_KEY);
            startActivityForResult(UploadMemeActivity.getIntent(MemesFromCategoryActivity.this, memeUri, mCategoryId), FilterUtils.REQUEST_UPLOAD_MEME);
        } else if (requestCode == FilterUtils.REQUEST_UPLOAD_MEME && resultCode == RESULT_OK && data != null) {
            MemesFromCategoryActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Meme meme = (Meme) data.getExtras().get(Meme.PARCELABLE_KEY);
                    mMemes.add(meme);
                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(MemesFromCategoryActivity.this, "Meme successfully created.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
