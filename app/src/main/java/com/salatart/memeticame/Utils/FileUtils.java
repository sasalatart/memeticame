package com.salatart.memeticame.Utils;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import com.salatart.memeticame.Models.Attachment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by sasalatart on 9/13/16.
 */
public class FileUtils {
    public static String getMemeticameDirectory() {
        return Environment.getExternalStorageDirectory() + "/Memeticame";
    }

    public static String getMemeticameDownloadsDirectory() {
        return getMemeticameDirectory() + "/Downloads";
    }

    public static ArrayList<Attachment> getAllDownloadedAttachments(Context context) {
        ArrayList<Attachment> attachments = new ArrayList<>();

        File parentDir = new File(getMemeticameDownloadsDirectory());
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                attachments.add(ParserUtils.attachmentFromUri(context, Uri.fromFile(file)));
            }
        }

        return attachments;
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

    public static Intent getSelectFileIntent(String type) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(type);
        return intent;
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
        File file3 = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name);
        File file4 = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + name);

        return file1.exists() || file2.exists() || file3.exists() || file4.exists();
    }

    public static Uri getUriFromFileName(Context context, String name) {
        File file1 = new File(getMemeticameDirectory() + "/" + name);
        File file2 = new File(getMemeticameDownloadsDirectory() + "/" + name);
        File file3 = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name);
        File file4 = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + name);

        if (file1.exists()) {
            return Uri.fromFile(file1);
        } else if (file2.exists()) {
            return Uri.fromFile(file2);
        } else if (file3.exists()) {
            return Uri.fromFile(file3);
        } else if (file4.exists()) {
            return Uri.fromFile(file4);
        } else {
            return null;
        }
    }

    public static void downloadFile(Context context, Attachment attachment) {
        if (!URLUtil.isValidUrl(attachment.getStringUri())) {
            return;
        }

        File dir = new File(getMemeticameDownloadsDirectory());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(attachment.getStringUri());
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(attachment.getName())
                .setDescription("Downloaded with Memeticame")
                .setDestinationInExternalPublicDir("/Memeticame/Downloads", attachment.getName());

        downloadManager.enqueue(request);
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
                        downloadFile(context, attachment);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    public static Intent getOpenFileIntent(Uri uri, String mimeType) {
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        openIntent.setDataAndType(uri, mimeType);
        return openIntent;
    }

    public static boolean openFile(Context context, Attachment attachment) {
        boolean fileExists = checkFileExistence(context, attachment.getName());

        if (!fileExists) {
            downloadAttachment(context, attachment);
        } else if (attachment.isAudio()) {
            RingtoneManager.getRingtone(context, Uri.parse(attachment.getStringUri())).play();
        } else if (attachment.isMemeaudio()) {
            RingtoneManager.getRingtone(context, attachment.getMemeaudioPartUri(context, false)).play();
        } else {
            try {
                context.startActivity(getOpenFileIntent(Uri.parse(attachment.getStringUri()), attachment.getMimeType()));
            } catch (ActivityNotFoundException e) {
                return false;
            }
        }

        return true;
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }
}
