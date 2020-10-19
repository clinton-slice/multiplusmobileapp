package com.netformular.multiplusmobileapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.netformular.multiplusmobileapp.R;
import com.netformular.multiplusmobileapp.adapter.UsersAdapter;
import com.netformular.multiplusmobileapp.app.Config;
import com.netformular.multiplusmobileapp.app.Endpoint;
import com.netformular.multiplusmobileapp.app.MyApplication;
import com.netformular.multiplusmobileapp.gcm.GcmIntentService;
import com.netformular.multiplusmobileapp.gcm.NotificationUtils;
import com.netformular.multiplusmobileapp.helper.SimpleDividerItemDecoration;
import com.netformular.multiplusmobileapp.model.ChatBox;
import com.netformular.multiplusmobileapp.model.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ArrayList<ChatBox> chatBoxArrayList;
    private UsersAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check if user is login in
        if(MyApplication.getInstance().getSession().getUser().getUserId() == null)
        {
            //user is not login send the  user to login screen.
            launchLoginActivity();
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    //subscribeToGlobalTopic();

                } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                    // gcm registration id is stored in our server's MySQL

                    Log.e(TAG, "GCM registration id is sent to our server");

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    //Toast.makeText(MainActivity.this,"push notification received",Toast.LENGTH_LONG).show();
                    handlePushNotification(intent);
                }
            }
        };
        chatBoxArrayList = new ArrayList<>();
        mAdapter = new UsersAdapter(this, chatBoxArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new UsersAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new UsersAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // when chat is clicked, launch full chat thread activity
                ChatBox chatBox = chatBoxArrayList.get(position);
                chatBox.setUnreadCount(0);
                Intent intent = new Intent(MainActivity.this, ChatBoxActivity.class);
                intent.putExtra("receiver_id", chatBox.getId());
                intent.putExtra("name", chatBox.getName());
                intent.putExtra("photo", chatBox.getPhoto());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        /**
         * Always check for google play services availability before
         * proceeding further with GCM
         * */
        if (checkPlayServices()) {
            registerGCM();
            fetchUsers();
        }
    }
    /**
     * Handles new push notification
     */
    private void handlePushNotification(Intent intent) {
        int type = intent.getIntExtra("type", -1);

        Toast.makeText(MainActivity.this,"type of notification: "+type,Toast.LENGTH_LONG);

        // if the push is of chat room message
        // simply update the UI unread messages count
        if (type == Config.PUSH_TYPE_GROUP) {
            Message message = (Message) intent.getSerializableExtra("message");
            //Toast.makeText(getApplicationContext(), "New push: " + message.getMessage(), Toast.LENGTH_LONG).show();

        } else if (type == Config.PUSH_TYPE_USER) {
            // push belongs to user alone
            // just showing the message in a toast
            //todo show message count
            Message message = (Message) intent.getSerializableExtra("message");
            String userId = intent.getStringExtra("sender_id");
            //Toast.makeText(getApplicationContext(), "New user push: " + message.getMessage(), Toast.LENGTH_LONG).show();

            if (message != null && userId != null) {
                //update the list of users with message count
                updateRow(userId, message);
            }
        }


    }
    /**
     * Updates the chat list unread count and the last message
     */
    private void updateRow(String userId, Message message) {
        for (ChatBox cb : chatBoxArrayList) {
            if (cb.getId().equals(userId)) {
                int index = chatBoxArrayList.indexOf(cb);
                cb.setLastMessage(message.getMessage());
                cb.setUnreadCount(cb.getUnreadCount() + 1);
                chatBoxArrayList.remove(index);
                chatBoxArrayList.add(index, cb);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }
    /**
     * fetching the chat userslist by making http call
     */
    private void fetchUsers() {
        final String URL = Endpoint.LIST_USERS.replace("_ID_",MyApplication.getInstance().getSession().getUser().getUserId());
        StringRequest strReq = new StringRequest(Request.Method.GET,
                URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        JSONArray usersArray = obj.getJSONArray("users");
                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject usersObj = (JSONObject) usersArray.get(i);
                            ChatBox cb = new ChatBox();
                            cb.setId(usersObj.getString("id"));
                            cb.setName(usersObj.getString("firstname")+ " "+usersObj.getString("lastname"));
                            cb.setLastMessage("");
                            cb.setUnreadCount(0);
                            cb.setPhoto(usersObj.getString("photo"));
                            cb.setTimestamp(usersObj.getString("created_at"));

                            chatBoxArrayList.add(cb);
                        }

                    } else {
                        // error in fetching chat rooms
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                mAdapter.notifyDataSetChanged();

                // subscribing to all chat room topics
                //subscribeToAllTopics();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }
    // starting the service to register with GCM
    private void registerGCM() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra("key", "register");
        startService(intent);
    }
    private void launchLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clearing the notification tray
        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_settings:
                //todo settings activity
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                logout();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    public void logout() {
        MyApplication.getInstance().getSession().clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
