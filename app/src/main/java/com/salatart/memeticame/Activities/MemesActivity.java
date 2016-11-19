package com.salatart.memeticame.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.MemeUtils;
import com.salatart.memeticame.Views.MemesAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemesActivity extends AppCompatActivity {

    public static String TITLE_KEY = "title";

    protected ArrayList<Meme> mMemes;
    protected MemesAdapter mAdapter;

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
                startActivityForResult(SeeMemeActivity.getIntent(MemesActivity.this, (Meme) mAdapter.getItem(position)), FilterUtils.REQUEST_SEE_MEME);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilterUtils.REQUEST_SEE_MEME && resultCode == RESULT_OK && data != null) {
            MemeUtils.replaceMeme(mMemes, (Meme) data.getParcelableExtra(Meme.PARCELABLE_KEY));
            mAdapter.notifyDataSetChanged();
        }
    }
}
