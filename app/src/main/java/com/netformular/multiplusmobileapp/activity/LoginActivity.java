package com.netformular.multiplusmobileapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringDef;
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
import com.android.volley.toolbox.Volley;
import com.netformular.multiplusmobileapp.R;
import com.netformular.multiplusmobileapp.app.Endpoint;
import com.netformular.multiplusmobileapp.app.MyApplication;
import com.netformular.multiplusmobileapp.helper.Session;
import com.netformular.multiplusmobileapp.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText email,password;
    private Button loginButton;

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(MyApplication.getInstance().getSession().getUser() != null)
        {
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmpty(email) && !isEmpty(password))
                {
                    login();
                }
                else
                {
                    Toast.makeText(LoginActivity.this,"Please enter your email and password",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean isEmpty(EditText editText)
    {
        return String.valueOf(editText.getText().toString().trim()).equals("");
    }
    public void login()
    {
        final String email = this.email.getText().toString();
        final String password = this.password.getText().toString();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Endpoint.LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        // user successfully logged in

                        JSONObject userObj = obj.getJSONObject("user");
                        User user = new User(userObj.getString("id"),
                                userObj.getString("firstname"),
                                userObj.getString("lastname"),
                                userObj.getString("photo"),
                                userObj.getString("email"));

                        // storing user in shared preferences
                        MyApplication.getInstance().getSession().storeUser(user);

                        // start main activity
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    } else {
                        // login error - simply toast the message
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }
}
