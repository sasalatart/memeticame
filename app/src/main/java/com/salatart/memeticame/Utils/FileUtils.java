package com.salatart.memeticame.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sasalatart on 9/13/16.
 */
public class FileUtils {
    public static String getName(Context context, Uri uri) {
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        return returnCursor.getString(nameIndex);
    }

    public static String getMimeType(Context context, Uri uri) {
        ContentResolver cR = context.getContentResolver();
        return cR.getType(uri);
    }

    public static String encodeToBase64FromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        byte[] file = getBytes(inputStream);
        return Base64.encodeToString(file, Base64.NO_WRAP);
    }

    public static Intent getSelectFileIntent() {
        Intent intent = new Intent();
        intent.setType("image/* video/* audio/*");
        String[] mimetypes = {"image/*", "video/*", "audio/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return Intent.createChooser(intent, "Select file");
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

    public static File createMediaFile(Context context, String extension) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = extension + "_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File file = null;
        try {
            file = File.createTempFile(
                    fileName,           /* prefix */
                    "." + extension,    /* suffix */
                    storageDir          /* directory */
            );
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
        }

        return file;
    }
}
