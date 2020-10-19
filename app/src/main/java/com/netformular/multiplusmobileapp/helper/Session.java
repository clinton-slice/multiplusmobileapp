package com.netformular.multiplusmobileapp.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.netformular.multiplusmobileapp.model.User;

public class Session {

    private String TAG = Session.class.getSimpleName();

    SharedPreferences pref;

    SharedPreferences.Editor editor;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    Context context;

    private static final String KEY_NOTIFICATIONS = "notifications";

    // Sharedpref file name
    private static final String PREF_NAME = "multiplusmobile_app";

    // All Shared Preferences Keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_FIRSTNAME = "user_firstname";
    private static final String KEY_USER_LASTNAME = "user_lastname";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHOTO = "user_photo";

    // Constructor
    public Session(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
    public void storeUser(User user) {
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USER_FIRSTNAME, user.getFirstname());
        editor.putString(KEY_USER_LASTNAME, user.getLastname());
        editor.putString(KEY_USER_PHOTO, user.getPhoto());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.commit();

        Log.e(TAG, "User is stored in shared preferences. " + user.getUserId() + ", " + user.getEmail() + ", "+  user.getFirstname());
    }
    public void addNotification(String notification) {

        // get old notifications
        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }
    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }
    public User getUser() {
        if (pref.getString(KEY_USER_ID, null) != null) {
            String id, firstname,lastname, email,photo;
            id = pref.getString(KEY_USER_ID, null);
            firstname = pref.getString(KEY_USER_FIRSTNAME, null);
            lastname = pref.getString(KEY_USER_LASTNAME, null);
            email = pref.getString(KEY_USER_EMAIL, null);
            photo = pref.getString(KEY_USER_PHOTO, null);

            User user = new User(id, firstname,lastname, email,photo);
            return user;
        }
        return null;
    }
}
