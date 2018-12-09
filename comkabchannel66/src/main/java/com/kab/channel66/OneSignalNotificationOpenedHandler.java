package com.kab.channel66;

import android.util.Log;

import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import java.util.Date;

class OneSignalNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    // This fires when a notification is opened by tapping on it.
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;
        String customKey;

        if (data != null) {
            customKey = data.optString("customkey", null);
            if (customKey != null)
                Log.i("OneSignalExample", "customkey set with value: " + customKey);
        }

        if (actionType == OSNotificationAction.ActionType.ActionTaken) {
            Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);
            OneSignal.sendTag((new Date(System.currentTimeMillis())).toString(), "clicked " + result.action.actionID);
        }
        else
        {
            //just opened
            OneSignal.sendTag((new Date(System.currentTimeMillis())).toString(), "opened");
        }
        // The following can be used to open an Activity of your choice.
        // Replace - getApplicationContext() - with any Android Context.
//         Intent intent = new Intent(MyApplication.getMyApp().getApplicationContext(), HanderActivity.class);
//         intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
//        MyApplication.getMyApp().getApplicationContext().startActivity(intent);


        // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
        //   if you are calling startActivity above.
     /* 
        <application ...>
          <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
        </application>
     */
    }
}