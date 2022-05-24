package com.prgs.etithe.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prgs.etithe.R;
import com.prgs.etithe.adapter.NotificationAdapter;
import com.prgs.etithe.models.Notifications;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.GridSpacingItemDecoration;
import com.prgs.etithe.utilities.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsActivity extends AppCompatActivity {

    @BindView(R.id.rvNotifications)
    RecyclerView rvNotifications;

    @BindView(R.id.tvNoRecordFound)
    TextView tvNoRecordFound;

    NotificationAdapter adapter;
    ArrayList<Notifications> mNotifications = new ArrayList<Notifications>();

    private ValueEventListener mNotifyListener;
    private DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_list);
        ButterKnife.bind(this);

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "Notifications");
        setSupportActionBar(mToolbarView);
        if (Global.LOGIN_USER_DETAIL != null) {
            LoadNotification();
        }else{
            Messages.ShowToast(getApplicationContext(),"User detail not found");
        }
    }

    private void LoadNotification() {
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvNotifications.setLayoutManager(mLayoutManager);
        rvNotifications.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), true));
        rvNotifications.setItemAnimator(new DefaultItemAnimator());

        String mUSER_TYPE = Global.LOGIN_USER_DETAIL.getUsertype() == 2 ? "FO" : "AR";


        final ProgressDialog dialog = new ProgressDialog(NotificationsActivity.this, R.style.MyAlertDialogStyle);
        dialog.setMessage("Loading..");
        dialog.show();
        mNotifications.clear();
        FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_NOTIFICATIONS).child(Global.LOGIN_USER_DETAIL.getRegionkey())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            mNotifications.clear();
                            for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                                Notifications notification = noteSnapshot.getValue(Notifications.class);
//                                if (notification.getMessageto().equalsIgnoreCase(mUSER_TYPE)) {
//                                    notification.setKey(noteSnapshot.getKey());
//                                    mNotifications.add(notification);
//                                }
                                notification.setKey(noteSnapshot.getKey());
                                mNotifications.add(notification);
                            }
                            if (mNotifications.size() > 0) {
                                Collections.sort(mNotifications, new Comparator<Notifications>() {
                                    public int compare(Notifications obj1, Notifications obj2) {
                                        return (obj1.getMessagedon() > obj2.getMessagedon()) ? -1 : (obj1.getMessagedon() > obj2.getMessagedon()) ? 1 : 0;
                                    }
                                });
                                rvNotifications.setVisibility(View.VISIBLE);
                                tvNoRecordFound.setVisibility(View.GONE);
                                adapter = new NotificationAdapter(getApplicationContext(), mNotifications);
                                rvNotifications.setAdapter(adapter);
                            } else {
                                rvNotifications.setVisibility(View.GONE);
                                tvNoRecordFound.setVisibility(View.VISIBLE);
                            }
                            dialog.dismiss();
                        } else {
                            rvNotifications.setVisibility(View.GONE);
                            tvNoRecordFound.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                        rvNotifications.setVisibility(View.GONE);
                        tvNoRecordFound.setText(databaseError.getMessage());
                        tvNoRecordFound.setVisibility(View.VISIBLE);
                    }
                });

    }


    @Override
    public void onBackPressed() {
        Intent iMain = new Intent(NotificationsActivity.this, MainActivity.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            default:
                onBackPressed();
                break;
        }
        return true;
    }
}
