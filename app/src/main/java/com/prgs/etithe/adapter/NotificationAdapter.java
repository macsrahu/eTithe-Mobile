package com.prgs.etithe.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.prgs.etithe.R;
import com.prgs.etithe.activities.DependentEntry;
import com.prgs.etithe.models.Dependent;
import com.prgs.etithe.models.Notifications;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {
    private Context mContext;
    private Activity mActivity;
    private List<Notifications> notificationsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView text_view_title, text_view_date, text_view_message;
        public CardView cardView;

        public MyViewHolder(View view) {
            super(view);
            text_view_title = (TextView) view.findViewById(R.id.text_view_title);
            text_view_date = (TextView) view.findViewById(R.id.text_view_date);
            text_view_message = (TextView) view.findViewById(R.id.text_view_message);
        }

    }

    @Override
    public NotificationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_row, parent, false);

        return new NotificationAdapter.MyViewHolder(itemView);
    }

    public NotificationAdapter(Context mContext, List<Notifications> _notificationsList) {
        this.mContext = mContext;
        this.notificationsList = _notificationsList;
    }


    @Override
    public void onBindViewHolder(final NotificationAdapter.MyViewHolder holder, final int position) {
        final Notifications notification = notificationsList.get(position);
        holder.text_view_title.setText(notification.getTitle());
        //holder.text_view_date.setText(notification.getMessagedon());
        holder.text_view_date.setText(Global.GetDateStringByLong(notification.getMessagedon()));
        holder.text_view_message.setText(notification.getMessage());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }
}
