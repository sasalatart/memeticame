package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.Routes;

import java.util.ArrayList;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * Created by sasalatart on 10/26/16.
 */

public class MemeGalleryAdapter extends BaseAdapter {
    private Context mContext;

    private ArrayList<String[]> mUrls;

    public MemeGalleryAdapter(Context context, ArrayList<String[]> urls) {
        mContext = context;
        mUrls = urls;
    }

    @Override
    public int getCount() {
        return mUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return mUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            int size = parent.getWidth() / 2;
            if (mContext.getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
                size = (int) (parent.getWidth() / 2.5);
            }

            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(size, size));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        setImage(imageView, mUrls.get(position)[0]);
        return imageView;
    }

    private void setImage(ImageView view, String url) {
        Glide.with(mContext)
                .load(Routes.DOMAIN + url)
                .placeholder(R.drawable.ic_access_time_black_24dp)
                .crossFade()
                .into(view);
    }
}
