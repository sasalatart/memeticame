package com.salatart.memeticame.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Message;

/**
 * Created by sasalatart on 10/17/16.
 */

public class MessageUtils {
    private static String PREFERENCES = "MESSAGE";
    private static String MESSAGE_CONTENT = "messageContent";
    private static String MESSAGE_ATTACHMENT_URI = "messageAttachmentUri";

    public static void copyMessage(Context context, Message message) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putString(MESSAGE_CONTENT, message.getContent());

        Attachment attachment = message.getAttachment();
        if (attachment != null && FileUtils.checkFileExistence(context, attachment.getName())) {
            String uriString = FileUtils.getUriFromFileName(context, attachment.getName()).toString();
            editor.putString(MESSAGE_ATTACHMENT_URI, uriString);
        } else {
            editor.putString(MESSAGE_ATTACHMENT_URI, null);
        }

        editor.commit();
    }

    public static String[] retrieveMessage(Context context) {
        String messageContent = context.getSharedPreferences(PREFERENCES, 0).getString(MESSAGE_CONTENT, null);
        String messageAttachmentUri = context.getSharedPreferences(PREFERENCES, 0).getString(MESSAGE_ATTACHMENT_URI, null);

        String[] messageData = {messageContent, messageAttachmentUri};

        if (messageContent == null && messageAttachmentUri == null) {
            return null;
        } else {
            return messageData;
        }
    }
}
