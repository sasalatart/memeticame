package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.salatart.memeticame.Models.Category;
import com.salatart.memeticame.R;

import java.util.ArrayList;

/**
 * Created by sasalatart on 11/16/16.
 */

public class CategoriesAdapter extends ArrayAdapter<Category> {
    private LayoutInflater mLayoutInflater;

    public CategoriesAdapter(Context context, int resource, ArrayList<Category> categories) {
        super(context, resource, categories);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Category category = getItem(position);

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.list_item_category, parent, false);
        }

        setTextViews(view, category);

        return view;
    }

    public void setTextViews(View view, Category category) {
        ImageView identificationView = (ImageView) view.findViewById(R.id.label_category_identification);
        TextDrawable categoryIdentification = TextDrawable.builder()
                .beginConfig()
                .withBorder(5)
                .toUpperCase()
                .endConfig()
                .buildRoundRect(category.getName().charAt(0) + "", ColorGenerator.MATERIAL.getColor(category.getName()), 10);
        identificationView.setImageDrawable(categoryIdentification);

        TextView categoryNameLabel = (TextView) view.findViewById(R.id.label_category_name);
        categoryNameLabel.setText(category.getName());

        TextView numberOfMemesLabel = (TextView) view.findViewById(R.id.label_number_of_memes);
        numberOfMemesLabel.setText(category.getMemes().size() + "");
    }
}
