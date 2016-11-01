package com.salatart.memeticame.Views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.MemeUtils;

import java.util.ArrayList;

/**
 * Created by sasalatart on 10/31/16.
 */

public class MemeGalleryAdapter extends ArrayAdapter {

    private Context mContext;
    private int mLayoutResourceId;
    private ArrayList<Attachment> mMemes;

    public MemeGalleryAdapter(Context context, int layoutResourceId, ArrayList<Attachment> memes) {
        super(context, layoutResourceId, memes);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
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
        Attachment memeAttachment = mMemes.get(position);

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, parent, false);
        }

        ImageView imageMeme = (ImageView) convertView.findViewById(R.id.image_meme);
        setImage(imageMeme, memeAttachment.getShowableStringUri(mContext));

        ImageView imageAudioLabel = (ImageView) convertView.findViewById(R.id.label_contains_audio);
        imageAudioLabel.setVisibility(memeAttachment.isMemeaudio() ? View.VISIBLE : View.GONE);

        TextView textMemeName = (TextView) convertView.findViewById(R.id.label_meme_name);
        textMemeName.setText(MemeUtils.cleanName(memeAttachment.getName()));

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
