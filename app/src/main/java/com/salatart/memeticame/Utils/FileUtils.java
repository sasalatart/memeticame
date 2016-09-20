package com.salatart.memeticame.Utils;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;

import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.R;

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
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

    public static boolean checkFileExistence(Context context, String name) {
        File file1 = new File(Environment.getExternalStorageDirectory() + "/Memeticame/" + name);
        File file2 = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name);

        return file1.exists() || file2.exists();
    }

    public static Uri getUriFromFileName(Context context, String name) {
        File file1 = new File(Environment.getExternalStorageDirectory() + "/Memeticame/" + name);
        File file2 = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name);

        if (file1.exists()) {
            return Uri.fromFile(file1);
        } else if (file2.exists()) {
            return Uri.fromFile(file2);
        } else {
            return null;
        }
    }

    public static void downloadFile(Context context, Attachment attachment) {
        if (!URLUtil.isValidUrl(attachment.getUri())) {
            return;
        }

        File dir = new File(Environment.getExternalStorageDirectory() + "/Memeticame");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(attachment.getUri());
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(attachment.getName())
                .setDescription("Downloaded with Memeticame")
                .setDestinationInExternalPublicDir("/Memeticame", attachment.getName());

        downloadManager.enqueue(request);
    }
}
