package com.salatart.memeticame.Listeners;

import com.salatart.memeticame.Models.Message;

/**
 * Created by sasalatart on 10/28/16.
 */

public interface OnSendMessageListener {
    public void OnSuccess(Message message);

    public void OnFailure();
}
