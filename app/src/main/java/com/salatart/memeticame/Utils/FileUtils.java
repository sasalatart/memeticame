package com.salatart.memeticame.Utils;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import com.salatart.memeticame.Activities.MemeaudioActivity;
import com.salatart.memeticame.Models.Attachment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sasalatart on 9/13/16.
 */
public class FileUtils {
    public static String getMemeticameDirectory() {
        return checkAndReturnDir(Environment.getExternalStorageDirectory() + "/Memeticame");
    }

    public static String getMemeticameDownloadsDirectory() {
        return checkAndReturnDir(getMemeticameDirectory() + "/Downloads");
    }

    public static String getMemeticameMemeaudiosDirectory() {
        return checkAndReturnDir(getMemeticameDirectory() + "/Memeaudios");
    }

    public static String getMemeticameMemesDirectory() {
        return checkAndReturnDir(getMemeticameDirectory() + "/Memes");
    }

    public static String getMemeticameUnzipsDirectory() {
        return checkAndReturnDir(getMemeticameDirectory() + "/Unzips");
    }

    public static boolean hasMediaPermissions(Context context) {
        boolean canRecordAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean canUseCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean canWriteToStorage = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return canRecordAudio && canUseCamera && canWriteToStorage;
    }

    private static String checkAndReturnDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return path;
    }

    public static Intent getOpenFileIntent(Uri uri, String mimeType) {
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        openIntent.setDataAndType(uri, mimeType);
        return openIntent;
    }

    public static Intent getSelectFileIntent(String type) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(type);
        return intent;
    }

    public static boolean openFile(Context context, Attachment attachment) {
        boolean fileExists = attachment.exists(context);

        if (!fileExists) {
            downloadAttachment(context, attachment);
        } else if (attachment.isMemeaudio()) {
            context.startActivity(MemeaudioActivity.getIntent(context, attachment));
        } else {
            try {
                context.startActivity(getOpenFileIntent(Uri.parse(attachment.getStringUri()), attachment.getMimeType()));
            } catch (ActivityNotFoundException e) {
                return false;
            }
        }

        return true;
    }

    public static String getName(Context context, Uri uri) {
        String result = null;

        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    public static String getMimeType(Context context, Uri uri) {
        String type = null;

        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        if (type == null) {
            ContentResolver cR = context.getContentResolver();
            type = cR.getType(uri);
        }

        if (type != null && type.contains("video")) {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(context, uri);
            if (metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) == null) {
                type = type.replace("video", "audio");
            }
        }

        return type;
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
        File file1 = new File(getMemeticameDirectory() + "/" + name);
        File file2 = new File(getMemeticameDownloadsDirectory() + "/" + name);
        File file3 = new File(getMemeticameMemeaudiosDirectory() + "/" + name);
        File file4 = new File(getMemeticameMemesDirectory() + "/" + name);
        File file5 = new File(getMemeticameUnzipsDirectory() + "/" + name);
        File file6 = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name);
        File file7 = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + name);

        return file1.exists() || file2.exists() || file3.exists() || file4.exists() || file5.exists() || file6.exists() || file7.exists();
    }

    public static Uri getUriFromFileName(Context context, String name) {
        File file1 = new File(getMemeticameDirectory() + "/" + name);
        File file2 = new File(getMemeticameDownloadsDirectory() + "/" + name);
        File file3 = new File(getMemeticameMemeaudiosDirectory() + "/" + name);
        File file4 = new File(getMemeticameMemesDirectory() + "/" + name);
        File file5 = new File(getMemeticameUnzipsDirectory() + "/" + name);
        File file6 = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name);
        File file7 = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + name);

        if (file1.exists()) {
            return Uri.fromFile(file1);
        } else if (file2.exists()) {
            return Uri.fromFile(file2);
        } else if (file3.exists()) {
            return Uri.fromFile(file3);
        } else if (file4.exists()) {
            return Uri.fromFile(file4);
        } else if (file5.exists()) {
            return Uri.fromFile(file5);
        } else if (file6.exists()) {
            return Uri.fromFile(file6);
        } else if (file7.exists()) {
            return Uri.fromFile(file7);
        } else {
            return null;
        }
    }

    public static long downloadFile(Context context, Uri downloadUri, String name) {
        File dir = new File(getMemeticameDownloadsDirectory());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(name)
                .setDescription("Downloaded with Memeticame")
                .setDestinationInExternalPublicDir("/Memeticame/Downloads", name);

        return downloadManager.enqueue(request);
    }

    public static void downloadAttachment(final Context context, final Attachment attachment) {
        if (attachment == null || !URLUtil.isValidUrl(attachment.getStringUri())) {
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Download file")
                .setMessage("Do you really want to download this file (" + attachment.getName() + ")?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        attachment.setDownloadId(downloadFile(context, Uri.parse(attachment.getStringUri()), attachment.getName()));
                        attachment.setProgress(0);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

}
