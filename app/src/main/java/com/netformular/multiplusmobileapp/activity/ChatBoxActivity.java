package com.netformular.multiplusmobileapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.netformular.multiplusmobileapp.R;
import com.netformular.multiplusmobileapp.adapter.ChatBoxThreadAdapter;
import com.netformular.multiplusmobileapp.app.Config;
import com.netformular.multiplusmobileapp.app.Endpoint;
import com.netformular.multiplusmobileapp.app.MyApplication;
import com.netformular.multiplusmobileapp.gcm.NotificationUtils;
import com.netformular.multiplusmobileapp.model.Message;
import com.netformular.multiplusmobileapp.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatBoxActivity extends AppCompatActivity {

    private String TAG = ChatBoxActivity.class.getSimpleName();

    private String receiverId;
    private RecyclerView recyclerView;
    private ChatBoxThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private EditText inputMessage;
    private Button btnSend;
    ImageLoader mImageLoader;
    NetworkImageView mNetworkImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inputMessage = (EditText) findViewById(R.id.message);
        btnSend = (Button) findViewById(R.id.btn_send);


        Intent intent = getIntent();
        receiverId = intent.getStringExtra("receiver_id");
        String title = intent.getStringExtra("name");
        String photo = intent.getStringExtra("photo");
        ActionBar mActionbar = getSupportActionBar();

        LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);;
        View header = inflater.inflate(R.layout.chatbox_action_bar,null);
        TextView name = (TextView) header.findViewById(R.id.chatbox_title);
        name.setText(title);
        //user image from remote server
        mNetworkImageView = (NetworkImageView) header.findViewById(R.id.chatbox_photo);
        mImageLoader = MyApplication.getInstance().getImageLoader();
        final String imageUrl = Endpoint.USER_IMAGE.replace("_ID_",photo);
        mNetworkImageView.setImageUrl(imageUrl, mImageLoader);

        mActionbar.setCustomView(header);

        //mActionbar.setTitle(title);
        mActionbar.setDisplayShowTitleEnabled(false);
        mActionbar.setDisplayShowCustomEnabled(true);
        mActionbar.setDisplayHomeAsUpEnabled(true);
        mActionbar.setHomeButtonEnabled(true);

        if (receiverId == null) {
            Toast.makeText(getApplicationContext(), "Chat Box not found!", Toast.LENGTH_SHORT).show();
            finish();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();

        // self user id is to identify the message owner
        String senderId = MyApplication.getInstance().getSession().getUser().getUserId();

        mAdapter = new ChatBoxThreadAdapter(this, messageArrayList, senderId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received
                    handlePushNotification(intent);
                }
            }
        };
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        fetchChatThread();

    }
    @Override
    protected void onResume() {
        super.onResume();

        // registering the receiver for new notification
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /* Handling new push message, will add the message to
    * recycler view and scroll it to bottom
    * */
    private void handlePushNotification(Intent intent) {

        Message message =  (Message)intent.getSerializableExtra("message");
        String receiverId = intent.getStringExtra("receiver_id");
        //Message message = new Message(null,)

        if (message != null && receiverId != null) {
            messageArrayList.add(message);
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 1) {
                //scrool to the bottom of the message
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
            }
        }
    }

    /**
     * Posting a new message in chat box
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void sendMessage() {
        final String message = this.inputMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        String endPoint = Endpoint.CHAT_BOX_MESSAGE;

        Log.e(TAG, "endpoint: " + endPoint);

        this.inputMessage.setText("");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                endPoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error
                    if (obj.getBoolean("error") == false) {
                        JSONObject commentObj = obj.getJSONObject("message");

                        String commentId = commentObj.getString("id");
                        String commentText = commentObj.getString("message");
                        String createdAt = commentObj.getString("created_at");

                        JSONObject userObj = obj.getJSONObject("user");
                        String senderId = userObj.getString("id");
                        String firstname = userObj.getString("firstname");
                        String lastname = userObj.getString("lastname");
                        String email = userObj.getString("email");
                        String photo = userObj.getString("photo");
                        User user = new User(senderId, firstname, lastname,email,photo);

                        Message message = new Message();
                        message.setId(commentId);
                        message.setMessage(commentText);
                        message.setSender(senderId);
                        message.setReceiver(receiverId);
                        message.setCreatedAt(createdAt);
                        message.setUser(user);

                        messageArrayList.add(message);

                        mAdapter.notifyDataSetChanged();
                        if (mAdapter.getItemCount() > 1) {
                            // scrolling to bottom of the recycler view
                            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    //Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                //Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                inputMessage.setText(message);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", MyApplication.getInstance().getSession().getUser().getUserId());
                params.put("message", message);
                params.put("receiver", receiverId);

                Log.e(TAG, "Params: " + params.toString());

                return params;
            };
        };


        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }
    /**
     * Fetching all the messages of a single chat room
     * */
    private void fetchChatThread() {

        String endPoint = Endpoint.CHAT_THREAD.replace("_ID1_", receiverId).replace("_ID_", MyApplication.getInstance().getSession().getUser().getUserId());
        Log.e(TAG, "endPoint: " + endPoint);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                endPoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error
                    if (obj.getBoolean("error") == false) {
                        JSONArray commentsObj = obj.getJSONArray("messages");

                        for (int i = 0; i < commentsObj.length(); i++) {
                            JSONObject commentObj = (JSONObject) commentsObj.get(i);

                            String commentId = commentObj.getString("id");
                            String commentText = commentObj.getString("message");
                            String createdAt = commentObj.getString("created_at");

                            JSONObject userObj = commentObj.getJSONObject("user");
                            String userId = userObj.getString("id");
                            String firstname = userObj.getString("firstname");
                            String lastname = userObj.getString("lastname");
                            String email = userObj.getString("email");
                            String photo = userObj.getString("photo");
                            User user = new User(userId, firstname, lastname,email,photo);

                            Message message = new Message();
                            message.setId(commentId);
                            message.setMessage(commentText);
                            message.setCreatedAt(createdAt);
                            message.setUser(user);

                            messageArrayList.add(message);
                        }
                        mAdapter.notifyDataSetChanged();
                        if (mAdapter.getItemCount() > 1) {
                            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    //Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                //Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

}
