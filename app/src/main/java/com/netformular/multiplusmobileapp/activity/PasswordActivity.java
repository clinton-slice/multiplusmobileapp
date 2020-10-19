package com.netformular.multiplusmobileapp.activity;

import android.content.Intent;
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

public class PasswordActivity extends AppCompatActivity {

    private EditText currentPass,newPass,confirmPass;
    private Button passButton;

    private final static String TAG = PasswordActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentPass = (EditText) findViewById(R.id.currentPassword);
        newPass = (EditText) findViewById(R.id.newPassword);
        confirmPass = (EditText) findViewById(R.id.confirmPassword);

        passButton = (Button) findViewById(R.id.passwordButton);

        passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmpty(currentPass) && !isEmpty(newPass) && !isEmpty(confirmPass))
                {
                    if(isMatch(newPass,confirmPass))
                    {
                        changePassword();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Your passwords do not match!",Toast.LENGTH_SHORT);
                    }
                }
                else
                {
                    Toast.makeText(PasswordActivity.this,"All fields are required",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public boolean isEmpty(EditText editText)
    {
        return String.valueOf(editText.getText().toString().trim()).equals("");
    }
    public boolean isMatch(EditText pass1,EditText pass2)
    {
        return pass1.getText().toString().trim().equals(pass2.getText().toString().trim());
    }
    public void changePassword()
    {
        final String currentPass = this.currentPass.getText().toString();
        final String newPass = this.newPass.getText().toString();

        StringRequest sReq = new StringRequest(Request.Method.POST, Endpoint.CHANGE_PASSWORD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        // user successfully logged in
                        Toast.makeText(getApplicationContext(),obj.getString("message"),Toast.LENGTH_LONG).show();

                    } else {
                        // login error - simply toast the message
                        Toast.makeText(getApplicationContext(), "Your password is incorrect", Toast.LENGTH_LONG).show();
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
                params.put("oldpass", currentPass);
                params.put("newpass", newPass);
                params.put("user_id", MyApplication.getInstance().getSession().getUser().getUserId());

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };
    }

}
