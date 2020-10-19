package com.netformular.multiplusmobileapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.netformular.multiplusmobileapp.R;
import com.netformular.multiplusmobileapp.adapter.SettingsListAdapter;

public class SettingsActivity extends AppCompatActivity {

    ListView listView;
    private String[] settings = {
            "Help",
            "Profile Photo",
            "Change Password",
            "Update Info"
    };

    Integer[] imgid = {
            R.drawable.help,
            R.drawable.user,
            R.drawable.password1,
            R.drawable.settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SettingsListAdapter adapter = new SettingsListAdapter(this,settings,imgid);
        listView = (ListView) findViewById(R.id.settingsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = settings[+position];
                switch (selectedItem)
                {
                    case "Help":
                        Toast.makeText(getApplicationContext(),"There is nothing here for now.",Toast.LENGTH_LONG).show();
                        break;
                    case "Profile Photo":
                        Intent photoIntent = new Intent(SettingsActivity.this,ChangePhoto.class);
                        startActivity(photoIntent);
                        break;
                    case "Change Password":
                        Intent passwordIntent = new Intent(SettingsActivity.this,PasswordActivity.class);
                        startActivity(passwordIntent);
                        break;
                    case "Update Info":
                        Intent userdataIntent = new Intent(SettingsActivity.this,UserdataActivity.class);
                        startActivity(userdataIntent);
                        break;
                    default:
                        break;
                }
            }
        });

    }

}
