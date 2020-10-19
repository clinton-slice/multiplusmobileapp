package com.netformular.multiplusmobileapp.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.netformular.multiplusmobileapp.R;


public class SettingsListAdapter extends ArrayAdapter {

    private final Activity context;
    private final String[] itemName;
    private final Integer[] imgid;


    public SettingsListAdapter(Activity context, String[] itemName, Integer[] imgid) {
        super(context, R.layout.settings_list_view,itemName);

        this.context = context;
        this.itemName = itemName;
        this.imgid = imgid;
    }

    public View getView(int position,View view,ViewGroup parent)
    {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.settings_list_view, null, true);

        TextView textTitle = (TextView) rowView.findViewById(R.id.settingItem);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.settingIcon);

        textTitle.setText(itemName[position]);
        imageView.setImageResource(imgid[position]);
        return rowView;
    }
}
