package com.netformular.multiplusmobileapp.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;


import com.netformular.multiplusmobileapp.activity.ChatBoxActivity;
import com.netformular.multiplusmobileapp.activity.MainActivity;
import com.netformular.multiplusmobileapp.app.Config;
import com.netformular.multiplusmobileapp.app.MyApplication;
import com.netformular.multiplusmobileapp.model.Message;
import com.netformular.multiplusmobileapp.model.User;

import org.json.JSONException;
import org.json.JSONObject;

public class MyGcmPushReceiver extends GcmListenerService {

    private static final String TAG = MyGcmPushReceiver.class.getSimpleName();

    private NotificationUtils notificationUtils;

    /**
     * Called when message is received.
     *
     * @param from   SenderID of the sender.
     * @param bundle Data bundle containing message data as key/value pairs.
     *               For Set of keys use data.keySet().
     */

    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        String title = bundle.getString("title");
        Boolean isBackground = Boolean.valueOf(bundle.getString("is_background"));
        String flag = bundle.getString("flag");
        String data = bundle.getString("data");

        Log.e(TAG, "From: " + from);
        Log.e(TAG, "Title: " + title);
        Log.e(TAG, "DATA: " + data);

        if (flag == null)
            return;

        if(MyApplication.getInstance().getSession().getUser() == null){
            // user is not logged in, skipping push notification
            Log.e(TAG, "user is not logged in, skipping push notification");
            return;
        }
        switch (Integer.parseInt(flag)) {
            case Config.PUSH_TYPE_GROUP:
                // push notification belongs to a chat room
                //processChatBoxPush(title, isBackground, data);
                Log.e(TAG,"logging group message");
                break;
            case Config.PUSH_TYPE_USER:
                // push notification is specific to user
                processChatBoxPush(title, isBackground, data);
                break;
        }
    }
    /**
     * Processing chat room push message
     * this message will be broadcasts to all the activities registered
     * */
    private void processChatBoxPush(String title, boolean isBackground, String data) {
        if (!isBackground) {

            try {
                JSONObject datObj = new JSONObject(data);

                JSONObject mObj = datObj.getJSONObject("message");

                String senderId = mObj.getString("sender_id");
                String receiverId = mObj.getString("receiver_id");
                //String msg = mObj.getString("message");

                Message message = new Message();
                message.setMessage(mObj.getString("message"));
                message.setId(mObj.getString("id"));
                message.setReceiver(mObj.getString("receiver_id"));
                message.setSender(mObj.getString("sender_id"));
                message.setCreatedAt(mObj.getString("created_at"));

                JSONObject uObj = datObj.getJSONObject("user");

                // skip the message if the message belongs to same user as
                // the user would be having the same message when he was sending
                // but it might differs in your scenario
                if (uObj.getString("id").equals(MyApplication.getInstance().getSession().getUser().getUserId())) {
                    Log.e(TAG, "Skipping the push message as it belongs to same user");
                    //return;
                }

                User user = new User();
                user.setUserId(uObj.getString("id"));
                user.setEmail(uObj.getString("email"));
                user.setFirstname(uObj.getString("firstname"));
                user.setLastname(uObj.getString("lastname"));
                user.setPhoto(uObj.getString("photo"));
                message.setUser(user);

                // verifying whether the app is in background or foreground
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_TYPE_USER);
                    pushNotification.putExtra("message", message);
                    pushNotification.putExtra("sender_id", senderId);
                    pushNotification.putExtra("receiver_id", receiverId);
                    Log.e(TAG,"push type: "+Config.PUSH_TYPE_USER);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                    // play notification sound
                    NotificationUtils notificationUtils = new NotificationUtils();
                    notificationUtils.playNotificationSound();
                } else {

                    // app is in background. show the message in notification try
                    Intent resultIntent = new Intent(getApplicationContext(), ChatBoxActivity.class);
                    resultIntent.putExtra("sender_id", senderId);
                    resultIntent.putExtra("type", Config.PUSH_TYPE_USER);
                    showNotificationMessage(getApplicationContext(), title, user.getFirstname() + " "+ user.getLastname() +" : " + message.getMessage(), message.getCreatedAt(), resultIntent);
                }

            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}
