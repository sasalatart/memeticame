package com.salatart.memeticame.Views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;

import java.util.ArrayList;

/**
 * Created by sasalatart on 11/16/16.
 */

public class MemesAdapter extends ArrayAdapter {

    private Context mContext;
    private ArrayList<Meme> mMemes;

    public MemesAdapter(Context context, int layoutResourceId, ArrayList<Meme> memes) {
        super(context, layoutResourceId, memes);

        mContext = context;
        mMemes = memes;
    }

    @Override
    public int getCount() {
        return mMemes.size();
    }

    @Override
    public Object getItem(int position) {
        return mMemes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Meme meme = mMemes.get(position);

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.grid_item_meme, parent, false);
        }

        ImageView imageMeme = (ImageView) convertView.findViewById(R.id.image_meme);
        setImage(imageMeme, meme.getThumbUrl());

        TextView textMemeName = (TextView) convertView.findViewById(R.id.label_meme_name);
        textMemeName.setText(meme.getName());

        com.iarcuschin.simpleratingbar.SimpleRatingBar ratingBar = (com.iarcuschin.simpleratingbar.SimpleRatingBar) convertView.findViewById(R.id.label_meme_rating_bar);
        SimpleRatingBar.AnimationBuilder builder = ratingBar.getAnimationBuilder()
                .setRatingTarget((float) meme.getRating())
                .setDuration(1000)
                .setRepeatCount(0)
                .setInterpolator(new LinearInterpolator());
        builder.start();
        ratingBar.setRating((float) meme.getRating());

        return convertView;
    }

    private void setImage(ImageView view, String uri) {
        Glide.with(mContext)
                .load(uri)
                .placeholder(R.drawable.ic_access_time_black_24dp)
                .crossFade()
                .into(view);
    }
}
