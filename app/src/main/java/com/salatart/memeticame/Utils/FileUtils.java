package com.salatart.memeticame.Utils;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;

import com.salatart.memeticame.Models.Attachment;

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

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

    public static Intent getSelectFileIntent() {
        Intent intent = new Intent();
        intent.setType("image/* video/* audio/*");
        String[] mimetypes = {"image/*", "video/*", "audio/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return Intent.createChooser(intent, "Select file");
    }

    public static File createMediaFile(Context context, String extension, String directory) {
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = context.getExternalFilesDir(directory);

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
        File file3 = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + name);

        return file1.exists() || file2.exists() || file3.exists();
    }

    public static Uri getUriFromFileName(Context context, String name) {
        File file1 = new File(Environment.getExternalStorageDirectory() + "/Memeticame/" + name);
        File file2 = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name);
        File file3 = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + name);

        if (file1.exists()) {
            return Uri.fromFile(file1);
        } else if (file2.exists()) {
            return Uri.fromFile(file2);
        } else if (file3.exists()) {
            return Uri.fromFile(file3);
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

    public static void downloadAttachment(final Context context, final Attachment attachment) {
        if (attachment == null || !URLUtil.isValidUrl(attachment.getUri())) {
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Download file")
                .setMessage("Do you really want to download this file (" + attachment.getName() + ")?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        downloadFile(context, attachment);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    public static Intent getOpenImageOrVideoIntent(Uri uri, String mimeType) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        return intent;
    }

    public static void openMedia(Context context, Attachment attachment) {
        boolean fileExists = FileUtils.checkFileExistence(context, attachment.getName());
        String mimeType = attachment.getMimeType();
        if (!fileExists) {
            downloadAttachment(context, attachment);
        } else if (mimeType.contains("image") || mimeType.contains("video")) {
            context.startActivity(FileUtils.getOpenImageOrVideoIntent(Uri.parse(attachment.getUri()), attachment.getMimeType()));
        } else if (mimeType.contains("audio")) {
            RingtoneManager.getRingtone(context, Uri.parse(attachment.getUri())).play();
        }
    }
}
