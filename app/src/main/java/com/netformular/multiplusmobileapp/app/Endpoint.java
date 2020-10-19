package com.netformular.multiplusmobileapp.app;

/**
 * Created by mac on 4/25/16.
 */
public class Endpoint {


    public static final String BASE_URL = "http://netformular.net/webservice";
    public static final String ROOT_URL = "http://netformular.net";
    public static final String LOGIN = BASE_URL + "/login";
    public static final String USER = BASE_URL + "/user/_ID_";
    public static final String USER_GCM = BASE_URL + "/update_gcm";
    public static final String LIST_USERS = BASE_URL + "/user_lists/_ID_";
    public static final String CHAT_THREAD = BASE_URL + "/conversation/_ID1_/_ID_";
    public static final String CHAT_BOX_MESSAGE = BASE_URL + "/add_message";

    public static final String CHANGE_PASSWORD = BASE_URL + "/password";
    public static final String SAVE_DATA = BASE_URL + "/save_data";

    public static final String USER_IMAGE = ROOT_URL + "/uploads/profile/_ID_";


}
