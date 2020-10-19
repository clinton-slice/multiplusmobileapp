package com.netformular.multiplusmobileapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.netformular.multiplusmobileapp.R;
import com.netformular.multiplusmobileapp.app.Endpoint;
import com.netformular.multiplusmobileapp.app.MyApplication;
import com.netformular.multiplusmobileapp.model.ChatBox;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<ChatBox> usersArrayList;
    private static String today;

    ImageLoader mImageLoader;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, message, timestamp, count;
        NetworkImageView mNetworkImageView;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            message = (TextView) view.findViewById(R.id.message);
            timestamp = (TextView) view.findViewById(R.id.timestamp);
            count = (TextView) view.findViewById(R.id.count);

            mNetworkImageView = (NetworkImageView) view.findViewById(R.id.networkImageView);
        }
    }


    public UsersAdapter(Context mContext, ArrayList<ChatBox> userArrayList) {
        this.mContext = mContext;
        this.usersArrayList = userArrayList;

        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_list, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatBox chatBox = usersArrayList.get(position);
        holder.name.setText(chatBox.getName());
        holder.message.setText(chatBox.getLastMessage());
        if (chatBox.getUnreadCount() > 0) {
            holder.count.setText(String.valueOf(chatBox.getUnreadCount()));
            holder.count.setVisibility(View.VISIBLE);
        } else {
            holder.count.setVisibility(View.GONE);
        }

        holder.timestamp.setText(getTimeStamp(chatBox.getTimestamp()));

        final String imageUrl = Endpoint.USER_IMAGE.replace("_ID_",chatBox.getPhoto());
        // Get the ImageLoader through your singleton class.
        mImageLoader = MyApplication.getInstance().getImageLoader();
        holder.mNetworkImageView.setImageUrl(imageUrl, mImageLoader);

    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static String getTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "";

        today = today.length() < 2 ? "0" + today : today;

        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            String date1 = format.format(date);
            timestamp = date1.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private UsersAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final UsersAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
