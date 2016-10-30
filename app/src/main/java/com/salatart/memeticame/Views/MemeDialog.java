package com.salatart.memeticame.Views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.rtugeek.android.colorseekbar.ColorSeekBar;
import com.salatart.memeticame.Enums.TypefaceConverter;
import com.salatart.memeticame.Listeners.OnConfirmMemetextListener;
import com.salatart.memeticame.Models.Memetext;
import com.salatart.memeticame.R;

/**
 * Created by gio on 30-10-16.
 */

public class MemeDialog extends Dialog {

    OnConfirmMemetextListener mListener;
    private int mColorInt;

    public MemeDialog (final Context context, final float x, final float y) {
        super(context);

        this.setContentView(R.layout.dialog_add_text_memetext);
        this.setTitle("Set meme text");
        final EditText text = (EditText) MemeDialog.this.findViewById(R.id.memetext);

        final Spinner fontFamilySpinner =
                (Spinner) MemeDialog.this.findViewById(R.id.font_family_spinner);

        ArrayAdapter<TypefaceConverter> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, TypefaceConverter.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontFamilySpinner.setAdapter(adapter);

        final ColorSeekBar colorSeekBar = (ColorSeekBar) MemeDialog.this.findViewById(R.id.color_slider);
        colorSeekBar.setColors(R.array.material_colors);
        colorSeekBar.setMaxValue(100);
        colorSeekBar.setColorBarValue(100);
        colorSeekBar.setBarHeight(7);
        mColorInt = colorSeekBar.getColor();
        text.setTextColor(mColorInt);



        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarValue, int alphaBarValue, int color) {
                text.setTextColor(color);
                mColorInt = color;
            }
        });

        Button okButton = (Button) this.findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText text = (EditText) MemeDialog.this.findViewById(R.id.memetext);
                if(mListener != null && !text.getText().toString().isEmpty()) {
                    final SeekBar fontSizeBar =
                            (SeekBar) MemeDialog.this.findViewById(R.id.font_size_bar);

                    Memetext memetext = new Memetext(text.getText().toString(), x, y );
                    memetext.setFontSize(fontSizeBar.getProgress() + 24F);
                    memetext.setPaintColor(mColorInt);

                    TypefaceConverter typefaceConverter =
                            (TypefaceConverter) fontFamilySpinner.getSelectedItem();
                    memetext.setFontFamily(typefaceConverter.getTypeface());
                    mListener.onConfirm(memetext);
                }

                MemeDialog.this.dismiss();
            }
        });

        Button cancelButton = (Button) this.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemeDialog.this.dismiss();
            }
        });
    }

    @Override
    public void show() {
        final EditText text = (EditText) this.findViewById(R.id.memetext);
        text.setText("");
        super.show();
    }

    public void setConfirmMemetextListener(OnConfirmMemetextListener listener){
        mListener = listener;
    }
}
