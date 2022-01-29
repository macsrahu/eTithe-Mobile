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
import com.prgs.etithe.adapter.DonorAdapter;
import com.prgs.etithe.adapter.ReceiptAdapter;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.models.Receipt;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReceiptsList extends AppCompatActivity {


    @BindView(R.id.rvReceipts)
    RecyclerView rvReceipts;

    @BindView(R.id.tvNoRecordFound)
    TextView tvNoRecordFound;

    ReceiptAdapter adapter;
    ArrayList<Receipt> mReceipts = new ArrayList<Receipt>();

    private ValueEventListener mDonorsValueListener;
    private DatabaseReference mDatabaseReference;
    Toolbar mToolbarView;
    String _NAVIGATE_FROM = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipts_list);
        ButterKnife.bind(this);

        mToolbarView = Global.PrepareToolBar(this, true, "Receipts");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _NAVIGATE_FROM = extras.getString("FROM");
        }
        if (_NAVIGATE_FROM != null) {
            if (_NAVIGATE_FROM.equals("MAIN")) {
                LoadReceiptsByReps();
            } else {

                if (Global.SELECTED_DONOR_MODEL != null) {
                    mToolbarView = Global.PrepareToolBar(this, true, Global.SELECTED_DONOR_MODEL.getDonor() + "'s Receipts");
                    LoadReceiptsByDonor();
                }
            }
        } else {
            LoadReceiptsByReps();
        }
        setSupportActionBar(mToolbarView);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDonorsValueListener != null && mDatabaseReference != null) {
            mDatabaseReference.removeEventListener(mDonorsValueListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent iMain = new Intent(ReceiptsList.this, _NAVIGATE_FROM.equals("MAIN") ? MainActivity.class : DonorsList.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }

    private void LoadReceiptsByReps() {

        Global.GET_OBJECT_FROM_MEMORY(getApplicationContext(), Global.USER_TYPE);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvReceipts.setLayoutManager(mLayoutManager);
        rvReceipts.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), true));
        rvReceipts.setItemAnimator(new DefaultItemAnimator());
        adapter = new ReceiptAdapter(getApplicationContext(), mReceipts, _NAVIGATE_FROM);
        rvReceipts.setAdapter(adapter);

        String lByKeyValueOf = Global.USER_TYPE == 3 ? Global.LOGIN_BY_AREA_PERSON.getKey() : Global.LOGIN_BY_FIELD_OFFICER.getKey();

        final ProgressDialog dialog = new ProgressDialog(ReceiptsList.this, R.style.MyAlertDialogStyle);
        dialog.setMessage("Loading..");
        dialog.show();
        mReceipts.clear();
        dialog.show();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_RECEIPTS);
        mDonorsValueListener = mDatabaseReference.orderByChild("repkey")
                .equalTo(lByKeyValueOf)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mReceipts.clear();
                            for (DataSnapshot donorSnapshot : dataSnapshot.getChildren()) {
                                Receipt receipt = donorSnapshot.getValue(Receipt.class);
                                receipt.setKey(donorSnapshot.getKey());
                                mReceipts.add(receipt);
                            }
                            if (mReceipts.size() > 0) {
                                //Sort By Latest Receipt
                                Collections.sort(mReceipts, new Comparator<Receipt>() {
                                    public int compare(Receipt obj1, Receipt obj2) {
                                        return (obj1.getCreatedon() > obj2.getCreatedon()) ? -1 : (obj1.getCreatedon() > obj2.getCreatedon()) ? 1 : 0;
                                    }
                                });
                                rvReceipts.setVisibility(View.VISIBLE);
                                tvNoRecordFound.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();
                            } else {
                                rvReceipts.setVisibility(View.GONE);
                                tvNoRecordFound.setVisibility(View.VISIBLE);
                            }
                            dialog.dismiss();
                        } else {
                            rvReceipts.setVisibility(View.GONE);
                            tvNoRecordFound.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                        rvReceipts.setVisibility(View.GONE);
                        tvNoRecordFound.setText(databaseError.getMessage());
                        tvNoRecordFound.setVisibility(View.VISIBLE);
                    }
                });

    }

    private void LoadReceiptsByDonor() {

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvReceipts.setLayoutManager(mLayoutManager);
        rvReceipts.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), true));
        rvReceipts.setItemAnimator(new DefaultItemAnimator());
        adapter = new ReceiptAdapter(getApplicationContext(), mReceipts, _NAVIGATE_FROM);
        rvReceipts.setAdapter(adapter);

        String lByKeyValueOf = Global.SELECTED_DONOR_MODEL.getKey();

        final ProgressDialog dialog = new ProgressDialog(ReceiptsList.this, R.style.MyAlertDialogStyle);
        dialog.setMessage("Loading..");
        dialog.show();
        mReceipts.clear();
        dialog.show();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_RECEIPTS);
        mDonorsValueListener = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_RECEIPTS).orderByChild("donorkey")
                .equalTo(lByKeyValueOf)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mReceipts.clear();
                            for (DataSnapshot donorSnapshot : dataSnapshot.getChildren()) {
                                Receipt receipt = donorSnapshot.getValue(Receipt.class);
                                receipt.setKey(donorSnapshot.getKey());
                                mReceipts.add(receipt);
                            }
                            if (mReceipts.size() > 0) {
                                //Sort By Latest Receipt
                                Collections.sort(mReceipts, new Comparator<Receipt>() {
                                    public int compare(Receipt obj1, Receipt obj2) {
                                        return (obj1.getCreatedon() > obj2.getCreatedon()) ? -1 : (obj1.getCreatedon() > obj2.getCreatedon()) ? 1 : 0;
                                    }
                                });
                                rvReceipts.setVisibility(View.VISIBLE);
                                tvNoRecordFound.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();
                            } else {
                                rvReceipts.setVisibility(View.GONE);
                                tvNoRecordFound.setVisibility(View.VISIBLE);
                            }
                            dialog.dismiss();
                        } else {
                            rvReceipts.setVisibility(View.GONE);
                            tvNoRecordFound.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                        rvReceipts.setVisibility(View.GONE);
                        tvNoRecordFound.setText(databaseError.getMessage());
                        tvNoRecordFound.setVisibility(View.VISIBLE);
                    }
                });

    }
}
