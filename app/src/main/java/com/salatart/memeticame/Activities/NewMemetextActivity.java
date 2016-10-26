package com.salatart.memeticame.Activities;

import android.app.Dialog;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.salatart.memeticame.R;
import com.salatart.memeticame.Views.CanvasView;

public class NewMemetextActivity extends AppCompatActivity {

    private CanvasView canvas = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memetext);

        canvas = (CanvasView) findViewById(R.id.canvas);
       // canvas.setMode(CanvasView.Mode.TEXT);
        canvas.setText("hola");


    }

    public void showDialog(View view){

        System.out.println("hola");
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_text_memetext);
        dialog.setTitle("Set meme text");

        final EditText text = (EditText) dialog.findViewById(R.id.memetext);
        Button okButton = (Button) dialog.findViewById(R.id.ok);
        Button cancelButton = (Button) dialog.findViewById(R.id.cancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("");
                dialog.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvas.setText(text.getText().toString());
                text.setText("");
                dialog.dismiss();
            }
        });

        dialog.show();

    }
}
