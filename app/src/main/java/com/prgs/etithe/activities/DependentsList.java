package com.prgs.etithe.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


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
import com.prgs.etithe.adapter.DependentAdapter;
import com.prgs.etithe.models.Dependent;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.GridSpacingItemDecoration;

import java.util.ArrayList;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;

public class DependentsList extends AppCompatActivity {

    @BindView(R.id.rvDependents)
    RecyclerView rvDependents;

    @BindView(R.id.tvNoRecordFound)
    TextView tvNoRecordFound;

    DependentAdapter adapter;
    ArrayList<Dependent> mDependents = new ArrayList<Dependent>();

    private ValueEventListener mDependentValueListener;
    private DatabaseReference mDatabaseReference;

    String _NAVIGATE_FROM = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dependent_list);
        ButterKnife.bind(this);

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "Dependents");
        setSupportActionBar(mToolbarView);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _NAVIGATE_FROM = extras.getString("FROM");
        }
        LoadDependentsByDonor();
    }

    @Override
    public void onBackPressed() {
        Intent iMain = new Intent(DependentsList.this, _NAVIGATE_FROM.equals("DONOR_ENTRY") ? DonorEntry.class : DonorsList.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dependent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_add_dependent:

                Global.SELECTED_DEPENDENT_MODEL = null;
                Intent iDepEntry = new Intent(DependentsList.this, DependentEntry.class);
                iDepEntry.putExtra("FROM", "DEP");
                iDepEntry.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iDepEntry);
                break;

            default:
                onBackPressed();
                break;
        }
        return true;
    }

    private void LoadDependentsByDonor() {

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvDependents.setLayoutManager(mLayoutManager);
        rvDependents.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), true));
        rvDependents.setItemAnimator(new DefaultItemAnimator());


        adapter = new DependentAdapter(getApplicationContext(), DependentsList.this, mDependents);
        rvDependents.setAdapter(adapter);

        final ProgressDialog dialog = ProgressDialog.show(this,
                null,
                "Loading data..",
                true);

        mDependents.clear();
        dialog.show();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DEPENDENTS).child(Global.DONOR_KEY);

        mDependentValueListener = mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot depenSnapshot : dataSnapshot.getChildren()) {
                        Dependent dependent = depenSnapshot.getValue(Dependent.class);
                        dependent.setKey(depenSnapshot.getKey());
                        mDependents.add(dependent);
                    }
                    if (mDependents.size() > 0) {
                        rvDependents.setVisibility(View.VISIBLE);
                        tvNoRecordFound.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    } else {
                        rvDependents.setVisibility(View.GONE);
                        tvNoRecordFound.setVisibility(View.VISIBLE);
                    }
                    dialog.dismiss();
                } else {
                    rvDependents.setVisibility(View.GONE);
                    tvNoRecordFound.setVisibility(View.VISIBLE);
                    tvNoRecordFound.setText("NO DEPENDENT(S) FOUND");
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();
                rvDependents.setVisibility(View.GONE);
                tvNoRecordFound.setText(databaseError.getMessage());
                tvNoRecordFound.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDependentValueListener != null && mDatabaseReference != null) {
            mDatabaseReference.removeEventListener(mDependentValueListener);
        }
    }

}
