package com.salatart.memeticame.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.FilterUtils;

import java.io.File;

import ly.img.android.sdk.models.state.CameraSettings;
import ly.img.android.sdk.models.state.EditorLoadSettings;
import ly.img.android.sdk.models.state.EditorSaveSettings;
import ly.img.android.sdk.models.state.manager.SettingsList;
import ly.img.android.ui.activities.CameraPreviewActivity;
import ly.img.android.ui.activities.CameraPreviewBuilder;
import ly.img.android.ui.activities.PhotoEditorBuilder;

public class MemeEditorActivity extends AppCompatActivity {
    public static Intent getIntent(Context context, Uri fileUri) {
        Intent intent = new Intent(context, MemeEditorActivity.class);
        intent.putExtra(Meme.URI_KEY, fileUri);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_editor);

        Bundle data = getIntent().getExtras();
        if (data != null && data.getParcelable(Meme.URI_KEY) != null) {
            memeEditorWithPlainMeme(((Uri) data.getParcelable(Meme.URI_KEY)).getPath());
        } else {
            memeEditorWithCamera();
        }
    }

    private void memeEditorWithCamera() {
        SettingsList settingsList = new SettingsList();
        settingsList.getSettingsModel(CameraSettings.class)
                .setExportDir(FileUtils.getMemeticameTempDirectory())
                .setExportPrefix("camera_")
                .getSettingsModel(EditorSaveSettings.class)
                .setExportDir(FileUtils.getMemeticameTempDirectory())
                .setExportPrefix("result_")
                .setSavePolicy(EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT);

        new CameraPreviewBuilder(this)
                .setSettingsList(settingsList)
                .startActivityForResult(this, FilterUtils.REQUEST_EDITED_MEME);
    }


    private void memeEditorWithPlainMeme(String myPicturePath) {
        SettingsList settingsList = new SettingsList();
        settingsList.getSettingsModel(EditorLoadSettings.class)
                .setImageSourcePath(myPicturePath, true) // Load with delete protection true!
                .getSettingsModel(EditorSaveSettings.class)
                .setExportDir(FileUtils.getMemeticameTempDirectory())
                .setExportPrefix("result_")
                .setSavePolicy(EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT);

        new PhotoEditorBuilder(this)
                .setSettingsList(settingsList)
                .startActivityForResult(this, FilterUtils.REQUEST_EDITED_MEME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == FilterUtils.REQUEST_EDITED_MEME) {
            String path = data.getStringExtra(CameraPreviewActivity.RESULT_IMAGE_PATH);

            File mMediaFolder = new File(path);
            MediaScannerConnection.scanFile(this, new String[]{mMediaFolder.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(Meme.PATH_KEY, path);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            });
        } else {
            finish();
        }
    }
}
