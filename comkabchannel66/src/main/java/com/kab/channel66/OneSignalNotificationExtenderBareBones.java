package com.kab.channel66;

import android.util.Log;

import com.kab.channel66.db.MessagesDataSource;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

public class OneSignalNotificationExtenderBareBones extends NotificationExtenderService {
    private static final String TAG = "NotificationExtender";

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        // Read properties from result.
        Log.d(TAG, "Message data payload: " + receivedResult.payload.rawPayload);

        MessagesDataSource datasource = new MessagesDataSource(this);
        datasource.open();
        datasource.createComment(receivedResult.payload.body);
        datasource.close();
        //sendNotification(receivedResult.payload.body);

        // Return true to stop the notification from displaying.

        return false;
    }
}