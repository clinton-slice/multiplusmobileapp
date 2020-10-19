package com.netformular.multiplusmobileapp.app;

/**
 * Created by mac on 4/25/16.
 */
public class Config
{
    public static boolean appendNotificationMessages = true;
    // broadcast receiver intent filters
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";
    // type of push messages
    public static final int PUSH_TYPE_GROUP = 1;
    public static final int PUSH_TYPE_USER = 2;

    // id to handle the notification in the notification try
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;
}
