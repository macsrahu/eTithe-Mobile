package com.prgs.etithe.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.prgs.etithe.R;
import com.prgs.etithe.adapter.DonorAdapter;
import com.prgs.etithe.models.Area;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.models.FieldOfficer;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.GridSpacingItemDecoration;
import com.prgs.etithe.utilities.Messages;
import com.viethoa.RecyclerViewFastScroller;
import com.viethoa.models.AlphabetItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DonorsList extends AppCompatActivity {


    @BindView(R.id.rvDonors)
    RecyclerView rvDonors;

    @BindView(R.id.tvNoRecordFound)
    TextView tvNoRecordFound;

    @BindView(R.id.spinner_area)
    MaterialSpinner spinner_area;

    DonorAdapter adapter;
    ArrayList<Donor> mDonors = new ArrayList<Donor>();

    private ValueEventListener mDonorsValueListener;
    private DatabaseReference mDatabaseReference;

    Area mArea=null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donors_list);
        ButterKnife.bind(this);

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "Donors");
        setSupportActionBar(mToolbarView);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvDonors.setLayoutManager(mLayoutManager);
        rvDonors.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), true));
        rvDonors.setItemAnimator(new DefaultItemAnimator());
        adapter = new DonorAdapter(getApplicationContext(), mDonors);
        rvDonors.setAdapter(adapter);


        //LoadDonorsByReps();
        LoadArea();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mDonorsValueListener != null && mDatabaseReference != null) {
//            mDatabaseReference.removeEventListener(mDonorsValueListener);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSearch:

                break;
            default:
                onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        Intent iMain = new Intent(DonorsList.this, MainActivity.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // below line is to get our inflater
        MenuInflater inflater = getMenuInflater();

        // inside inflater we are inflating our menu file.
        inflater.inflate(R.menu.search_menu, menu);

        // below line is to get our menu item.
        MenuItem searchItem = menu.findItem(R.id.actionSearch);

        // getting search view of our item.
        SearchView searchView = (SearchView) searchItem.getActionView();
        EditText txtSearch = ((EditText)searchView.findViewById(androidx.appcompat.R.id.search_src_text));
        txtSearch.setTextColor(Color.WHITE);

        // below line is to call set on query text listener method.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                if (mDonors!=null && mDonors.size()>0) {
                    filter(newText);
                }
                return false;
            }
        });
        return true;
    }

    private void filter(String text) {
        // creating a new array list to filter our data.
        ArrayList<Donor> filteredlist = new ArrayList<>();
        // running a for loop to compare elements.
        for (Donor item : mDonors) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.getDonor().toLowerCase().contains(text.toLowerCase())
               || item.getMobile().toLowerCase().contains(text.toLowerCase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Messages.ShowToast(getApplicationContext(), "No Data Found..");
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            adapter.filterList(filteredlist);
        }
    }

    private void LoadArea(){
        if (Global.LOGIN_USER_DETAIL!=null) {

            final ProgressDialog dialog = new ProgressDialog(DonorsList.this, R.style.MyAlertDialogStyle);
            dialog.setMessage("Loading areas..");
            dialog.show();
            ArrayList<Area> areas = new ArrayList<Area>();
            FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_AREAS).orderByChild("regionkey")
                    .equalTo(Global.LOGIN_USER_DETAIL.getRegionkey())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                areas.clear();
                                Area areaMessage= new Area();
                                areaMessage.setArea("All Area");
                                areaMessage.setKey("all");
                                areaMessage.setIsactive(true);
                                areaMessage.setRegionkey("all");
                                areas.add(areaMessage);
                                mArea = areaMessage;
                                for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                                    Area area = areaSnapshot.getValue(Area.class);
                                    area.setKey(areaSnapshot.getKey());
                                    areas.add(area);
                                }
                                if (areas.size() > 0) {
                                    MaterialSpinnerAdapter adpArea = new MaterialSpinnerAdapter<Area>(getBaseContext(), areas);
                                    spinner_area.setAdapter(adpArea);
                                    spinner_area.setHint("Select Area");
                                    spinner_area.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Area>() {
                                        @Override
                                        public void onItemSelected(MaterialSpinner view, int position, long id, Area item) {
                                            view.setSelectedIndex(position);
                                            if (view != null) {
                                                mArea = item;
                                                LoadDonorsByReps();
                                            }
                                        }
                                    });
                                    LoadDonorsByReps();
                                }
                                dialog.dismiss();
                            } else {
                                dialog.dismiss();
                                Messages.ShowToast(getApplicationContext(), "Please check area created for this region");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Messages.ShowToast(getApplicationContext(), "Please check area created for this region");
                            dialog.dismiss();
                        }
                    });

        }else{
            Messages.ShowToast(getApplicationContext(), "User details not binded");
        }
    }

    private void LoadDonorsByReps() {

        Global.GET_OBJECT_FROM_MEMORY(getApplicationContext(), Global.USER_TYPE);


        String loadByKeyName = Global.USER_TYPE == 3 ? "personkey" : "officerkey";
        String lByKeyValueOf = Global.USER_TYPE == 3 ? Global.LOGIN_BY_AREA_PERSON.getKey() : Global.LOGIN_BY_FIELD_OFFICER.getKey();

        final ProgressDialog dialog = new ProgressDialog(DonorsList.this, R.style.MyAlertDialogStyle);
        dialog.setMessage("Loading donor..");
        dialog.show();
        // mDatabaseReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DONORS);
        FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DONORS).orderByChild(loadByKeyName)
                .equalTo(lByKeyValueOf)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mDonors.clear();
                            for (DataSnapshot donorSnapshot : dataSnapshot.getChildren()) {
                                Donor donor = donorSnapshot.getValue(Donor.class);
                                if (mArea!=null) {
                                    if (mArea.getKey().equals("all")) {
                                        donor.setKey(donorSnapshot.getKey());
                                        mDonors.add(donor);
                                    } else {
                                        if (donor.getAreakey() != null) {
                                            if (donor.getAreakey().equals(mArea.getKey())) {
                                                donor.setKey(donorSnapshot.getKey());
                                                mDonors.add(donor);
                                            }
                                        }
                                    }
                                }
                            }
                            if (mDonors.size() > 0) {
                                Collections.sort(mDonors, new Comparator<Donor>() {
                                    public int compare(Donor o1, Donor o2) {
                                        return o1.getDonor().compareTo(o2.getDonor());
                                    }
                                });
                                rvDonors.setVisibility(View.VISIBLE);
                                tvNoRecordFound.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();
                            } else {
                                rvDonors.setVisibility(View.GONE);
                                tvNoRecordFound.setVisibility(View.VISIBLE);
                            }
                            dialog.dismiss();
                        } else {
                            rvDonors.setVisibility(View.GONE);
                            tvNoRecordFound.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                        rvDonors.setVisibility(View.GONE);
                        tvNoRecordFound.setText(databaseError.getMessage());
                        tvNoRecordFound.setVisibility(View.VISIBLE);
                    }
                });

    }

}
