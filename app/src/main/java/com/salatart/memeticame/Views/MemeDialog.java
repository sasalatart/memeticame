package com.salatart.memeticame.Views;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.rtugeek.android.colorseekbar.ColorSeekBar;
import com.salatart.memeticame.Enums.TypefaceConverter;
import com.salatart.memeticame.Listeners.OnConfirmMemeListener;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;

/**
 * Created by gio on 30-10-16.
 */

public class MemeDialog extends Dialog {

    OnConfirmMemeListener mListener;
    private int mColorInt;

    public MemeDialog(final Context context, final float x, final float y) {
        super(context);

        this.setContentView(R.layout.dialog_add_text_meme);
        this.setTitle("Set meme text");
        final EditText text = (EditText) MemeDialog.this.findViewById(R.id.input_meme_name);

        final Spinner fontFamilySpinner = (Spinner) MemeDialog.this.findViewById(R.id.font_family_spinner);

        ArrayAdapter<TypefaceConverter> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, TypefaceConverter.values());
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
                final EditText text = (EditText) MemeDialog.this.findViewById(R.id.input_meme_name);
                if (mListener != null && !text.getText().toString().isEmpty()) {
                    final SeekBar fontSizeBar = (SeekBar) MemeDialog.this.findViewById(R.id.font_size_bar);

                    Meme meme = new Meme(text.getText().toString(), x, y);
                    meme.setFontSize(fontSizeBar.getProgress() + 24F);
                    meme.setPaintColor(mColorInt);

                    TypefaceConverter typefaceConverter = (TypefaceConverter) fontFamilySpinner.getSelectedItem();
                    meme.setFontFamily(typefaceConverter.getTypeface());
                    mListener.onConfirm(meme);
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
        final EditText text = (EditText) this.findViewById(R.id.input_meme_name);
        text.setText("");
        super.show();
    }

    public void setConfirmMemeListener(OnConfirmMemeListener listener) {
        mListener = listener;
    }
}
