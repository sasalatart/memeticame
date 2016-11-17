package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.salatart.memeticame.Models.Channel;
import com.salatart.memeticame.R;

import java.util.ArrayList;

/**
 * Created by sasalatart on 11/12/16.
 */

public class ChannelsAdapter extends ArrayAdapter<Channel> {
    private LayoutInflater mLayoutInflater;

    public ChannelsAdapter(Context context, int resource, ArrayList<Channel> channels) {
        super(context, resource, channels);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Channel channel = getItem(position);

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.list_item_channel, parent, false);
        }

        setTextViews(view, channel);

        return view;
    }

    private void setTextViews(View view, Channel channel) {
        ImageView identificationView = (ImageView) view.findViewById(R.id.label_channel_identification);
        TextDrawable channelIdentification = TextDrawable.builder()
                .beginConfig()
                .withBorder(5)
                .toUpperCase()
                .endConfig()
                .buildRoundRect(channel.getName().charAt(0) + "", ColorGenerator.MATERIAL.getColor(channel.getName()), 10);
        identificationView.setImageDrawable(channelIdentification);

        TextView channelNameLabel = (TextView) view.findViewById(R.id.label_channel_name);
        channelNameLabel.setText(channel.getName());

        com.iarcuschin.simpleratingbar.SimpleRatingBar ratingBar = (com.iarcuschin.simpleratingbar.SimpleRatingBar) view.findViewById(R.id.label_channel_rating);
        SimpleRatingBar.AnimationBuilder builder = ratingBar.getAnimationBuilder()
                .setRatingTarget((float) channel.getRating())
                .setDuration(1000)
                .setRepeatCount(0)
                .setInterpolator(new LinearInterpolator());
        builder.start();
        ratingBar.setRating((float) channel.getRating());

        TextView channelOwnerLabel = (TextView) view.findViewById(R.id.label_channel_owner);
        String creatorText = "Created by: " + channel.getOwner().getName();
        channelOwnerLabel.setText(creatorText);
    }
}
