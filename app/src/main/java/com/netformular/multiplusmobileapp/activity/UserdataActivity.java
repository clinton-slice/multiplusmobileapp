package com.netformular.multiplusmobileapp.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.netformular.multiplusmobileapp.R;
import com.netformular.multiplusmobileapp.app.Endpoint;
import com.netformular.multiplusmobileapp.app.MyApplication;
import com.netformular.multiplusmobileapp.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserdataActivity extends AppCompatActivity {

    private EditText firstname,lastname,email,phone;
    private Button saveButton;

    private static final String TAG = UserdataActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdata);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firstname =  (EditText) findViewById(R.id.settingFirstname);
        lastname =  (EditText) findViewById(R.id.settingLastname);
        email =  (EditText) findViewById(R.id.settingEmail);
        //phone =  (EditText) findViewById(R.id.settingPhone);

        saveButton = (Button) findViewById(R.id.savedataButton);

        if(savedInstanceState == null)
        {
            User user = MyApplication.getInstance().getSession().getUser();
            firstname.setText(user.getFirstname());
            lastname.setText(user.getLastname());
            email.setText(user.getEmail());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmpty(firstname) && !isEmpty(lastname) && !isEmpty(email))
                {
                    saveUserData();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"All fields are required",Toast.LENGTH_SHORT);
                }
            }
        });
    }
    public boolean isEmpty(EditText editText)
    {
        return String.valueOf(editText.getText().toString().trim()).equals("");
    }
    public void saveUserData()
    {
        final String firstname = this.firstname.getText().toString();
        final String lastname = this.lastname.getText().toString();
        final String email = this.lastname.getText().toString();

        StringRequest sReq = new StringRequest(Request.Method.POST, Endpoint.SAVE_DATA, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {

                        JSONObject userObj = obj.getJSONObject("user");
                        User user = new User(userObj.getString("id"),
                                userObj.getString("firstname"),
                                userObj.getString("lastname"),
                                userObj.getString("photo"),
                                userObj.getString("email"));

                        // storing user in shared preferences
                        MyApplication.getInstance().getSession().storeUser(user);
                        Toast.makeText(getApplicationContext(),"Your data was updated successfully",Toast.LENGTH_LONG).show();

                    } else {
                        // login error - simply toast the message
                        Toast.makeText(getApplicationContext(), "A network error occured", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("firstname", firstname);
                params.put("lastname", lastname);
                params.put("email", email);
                params.put("user_id", MyApplication.getInstance().getSession().getUser().getUserId());

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };
    }

}
