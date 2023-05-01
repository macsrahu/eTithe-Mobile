package com.prgs.etithe.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.florent37.expansionpanel.ExpansionLayout;
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.prgs.etithe.BuildConfig;
import com.prgs.etithe.R;
import com.prgs.etithe.models.Area;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.KeyboardUtil;
import com.prgs.etithe.utilities.Messages;
import com.prgs.etithe.utilities.CommonList;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;

public class DonorEntry extends AppCompatActivity {
    @BindView(R.id.input_donor_name)
    TextInputEditText input_donor_name;

    @BindView(R.id.input_aadhar_no)
    TextInputEditText input_aadhar_no;

    @BindView(R.id.input_pan)
    TextInputEditText input_pan;

    @BindView(R.id.input_address_line1)
    TextInputEditText input_address_line1;

    @BindView(R.id.input_address_line2)
    TextInputEditText input_address_line2;

    @BindView(R.id.input_city)
    TextInputEditText input_city;

    @BindView(R.id.input_district)
    TextInputEditText input_district;

    @BindView(R.id.imgBrithDate)
    ImageButton imgBirthDate;

    @BindView(R.id.imgWedDate)
    ImageButton imgWedDate;

    @BindView(R.id.input_pincode)
    TextInputEditText input_pincode;

    @BindView(R.id.input_mobile_no)
    TextInputEditText input_mobile_no;

    @BindView(R.id.input_whatsapp_no)
    TextInputEditText input_whatsapp_no;

    @BindView(R.id.input_email)
    TextInputEditText input_email;

    @BindView(R.id.radio_button_male)
    RadioButton radio_button_male;
    @BindView(R.id.radio_button_female)
    RadioButton radio_button_female;

    @BindView(R.id.radio_button_unknown)
    RadioButton radio_button_unknown;

    @BindView(R.id.input_birth_date)
    TextView input_birth_date;

    @BindView(R.id.input_web_date)
    TextView input_web_date;

    @BindView(R.id.radio_button_married)
    RadioButton radio_button_married;

    @BindView(R.id.radio_button_single)
    RadioButton radio_button_single;

    @BindView(R.id.radio_button_member)
    RadioButton radio_button_member;

    @BindView(R.id.radio_button_non_member)
    RadioButton radio_button_non_member;


    @BindView(R.id.spinner_area)
    MaterialSpinner spinner_area;

    @BindView(R.id.spinner_state)
    MaterialSpinner spinner_state;

    @BindView(R.id.spinner_salutaion)
    MaterialSpinner spinner_salutation;



    BottomNavigationView buttonNavigationView;

    Toolbar mToolbarView = null;
    Donor mDonor = new Donor();
    boolean ENTRY_MODE_NEW = true;
    Boolean isValid = false;

    String mDonorName, mEmail, mMobile, mAadharNo, mPanNo, mMarried, mWhatsAppNo, mAddressLine1, mAddressLine2, mCity, mPincode;
    String mDistrict, mState, mWedDate, mBirthDate, mGender, mMemberType;
    String _NAVIGATE_FROM = "";
    String mPrimaryKey = "";
    String mSalutation="Mr.";

    Area mArea=null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donor_entry);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _NAVIGATE_FROM = extras.getString("FROM");
        }
        

        if (Global.LOGIN_USER_DETAIL==null){
            Global.GET_LOGIN_INFO_FROM_MEMORY(getApplicationContext());
        }
        InitControls();
        InitLocation();
        if (Global.SELECTED_DONOR_MODEL != null) {
            ENTRY_MODE_NEW = false;
            mDonor = Global.SELECTED_DONOR_MODEL;
            Global.DONOR_KEY = Global.SELECTED_DONOR_MODEL.getKey();
            mToolbarView = Global.PrepareToolBar(this, true, "Update Donor");
            setSupportActionBar(mToolbarView);
            BindDonorData();
        } else {
            Global.DONOR_KEY = "";
            mToolbarView = Global.PrepareToolBar(this, true, "New Donor");
            setSupportActionBar(mToolbarView);
        }


        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void BindDonorData() {
        input_donor_name.setText(mDonor.getDonor());
        Global.DONOR_KEY = mDonor.getKey();
        if (mDonor.getGender() != null) {
            if (mDonor.getGender().equals("Male")) {
                radio_button_male.setChecked(true);
            } else if (mDonor.getGender().equals("Female")) {
                radio_button_female.setChecked(true);
            } else {
                radio_button_unknown.setChecked(true);
            }
        }
        if (mDonor.getMarried() != null) {
            if (mDonor.getMarried().toLowerCase().equals("married")) {
                radio_button_married.setChecked(true);
            } else {
                radio_button_single.setChecked(true);
            }
        }
        if (mDonor.getMembertype() != null) {
            if (mDonor.getMembertype().equals("Member")) {
                radio_button_member.setChecked(true);
            } else {
                radio_button_non_member.setChecked(true);
            }
        }
        if (mDonor.getAadhar() != null && !mDonor.getAadhar().equals("NA")) {
            input_aadhar_no.setText(mDonor.getAadhar());
        }
        if (mDonor.getSalutation() != null && !mDonor.getSalutation().equals("NA")) {
            spinner_salutation.setText(mDonor.getSalutation());
        }
        if (mDonor.getPan() != null) {
            input_pan.setText(mDonor.getPan());
        }
        if (mDonor.getBirthdate() != null && !mDonor.getBirthdate().equals("NA")) {
            input_birth_date.setText(mDonor.getBirthdate());
        }
        if (mDonor.getWeddate() != null && !mDonor.getWeddate().equals("NA")) {
            input_web_date.setText(mDonor.getWeddate());
        }
        if (mDonor.getAddrline1() != null && !mDonor.getAddrline1().equals("NA")) {
            input_address_line1.setText(mDonor.getAddrline1());
        }
        if (mDonor.getAddrline2() != null && !mDonor.getAddrline2().equals("NA")) {
            input_address_line2.setText(mDonor.getAddrline2());
        }
        if (mDonor.getCity() != null && !mDonor.getCity().equals("NA")) {
            input_city.setText(mDonor.getCity());
        }
        if (mDonor.getPincode() != null && !mDonor.getPincode().equals("NA")) {
            input_pincode.setText(mDonor.getPincode());
        }
        if (mDonor.getPincode() != null && !mDonor.getPincode().equals("NA")) {
            input_pincode.setText(mDonor.getPincode());
        }
        if (mDonor.getDistrict() != null && !mDonor.getDistrict().equals("NA")) {
            input_district.setText(mDonor.getDistrict());
        }
        if (mDonor.getState() != null && !mDonor.getState().equals("NA")) {
            spinner_state.setText(mDonor.getState());
        }

        if (mDonor.getMobile() != null && !mDonor.getMobile().equals("NA")) {
            input_mobile_no.setText(mDonor.getMobile());
        }
        if (mDonor.getWhatsapp() != null && !mDonor.getWhatsapp().equals("NA")) {
            input_whatsapp_no.setText(mDonor.getWhatsapp());
        }
        if (mDonor.getEmail() != null && !mDonor.getEmail().equals("NA")) {
            input_email.setText(mDonor.getEmail());
        }
//        if (mDonor.getAreakey()!=null && mDonor.getArea()!=null){
//            spinner_area.setHint(mDonor.getArea());
//        }
        buttonNavigationView.getMenu().findItem(R.id.btnSave).setTitle("Update");
        buttonNavigationView.getMenu().findItem(R.id.btnPhoto).setEnabled(true);
    }


    private void InitControls() {
        ExpansionLayout expAddr = findViewById(R.id.expansionLayoutAddr);
        ExpansionLayout expPer = findViewById(R.id.expansionLayout);
        final ExpansionLayoutCollection expansionLayoutCollection = new ExpansionLayoutCollection();
        expansionLayoutCollection.add(expPer);
        expansionLayoutCollection.add(expAddr);
        expansionLayoutCollection.openOnlyOne(true);
        radio_button_single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    input_web_date.setText("");
                } else {
                    if (mDonor.getWeddate() != null) {
                        input_web_date.setText(mDonor.getWeddate());
                    }
                }
            }
        });
        buttonNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        buttonNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.btnSave:
                    if (Validate()) {
                        if (ENTRY_MODE_NEW) {
                            KeyboardUtil.hideKeyboard(DonorEntry.this);
                            ValidateEmailAddress();
                        } else {
                            SaveRecord();
                            KeyboardUtil.hideKeyboard(DonorEntry.this);
                        }
                    }
                    break;
                case  R.id.btnLocation:

                    startLocationButtonClick();
                    break;
                case R.id.btnPhoto:
                    if (Global.DONOR_KEY.isEmpty()) {
                        Messages.ShowToast(getApplicationContext(), "Before upload picture, add donor details");
                    } else {
                        Intent iUpload = new Intent(DonorEntry.this, UploadPhoto.class);
                        iUpload.putExtra("FROM", "DEPENTRY");
                        iUpload.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(iUpload);
                    }
                    break;

                case R.id.btnDependent:
                    if (Global.DONOR_KEY.isEmpty()) {
                        Messages.ShowToast(getApplicationContext(), "Before add dependents, save donor details");
                    } else {
                        if (ENTRY_MODE_NEW) {
                            Intent iUpload = new Intent(DonorEntry.this, DependentEntry.class);
                            iUpload.putExtra("FROM", "DONOR_ENTRY");
                            iUpload.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(iUpload);
                        } else {
                            Intent iUpload = new Intent(DonorEntry.this, DependentsList.class);
                            iUpload.putExtra("FROM", "DONOR_ENTRY");
                            iUpload.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(iUpload);
                        }
                    }
                    break;
                case R.id.btnCancel:
                    onBackPressed();
                    break;
            }
            return true;
        });
        if (ENTRY_MODE_NEW == false) {
            buttonNavigationView.getMenu().findItem(R.id.btnSave).setTitle("Update");
        }

        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setCalendarConstraints(limitRange().build());

        final MaterialDatePicker materialDatePicker = materialDateBuilder.build();

        input_birth_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
                materialDatePicker.addOnPositiveButtonClickListener(
                        new MaterialPickerOnPositiveButtonClickListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onPositiveButtonClick(Object selection) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                input_birth_date.setText(dateFormat.format(selection));
                            }
                        });

            }
        });

        MaterialDatePicker.Builder materialDateBuilderWed = MaterialDatePicker.Builder.datePicker();
        materialDateBuilderWed.setCalendarConstraints(limitRange().build());
        final MaterialDatePicker materialDatePickerWed = materialDateBuilder.build();
        input_web_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radio_button_married.isChecked()) {
                    materialDatePickerWed.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
                    materialDatePickerWed.addOnPositiveButtonClickListener(
                            new MaterialPickerOnPositiveButtonClickListener() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onPositiveButtonClick(Object selection) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                    input_web_date.setText(dateFormat.format(selection));
                                }
                            });
                } else {
                    Messages.ShowToast(getApplicationContext(), "Please select Married option to set wedding date");
                }
            }
        });
        input_birth_date.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                input_birth_date.setText("");
                return false;
            }
        });
        input_web_date.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                input_web_date.setText("");
                return false;
            }
        });
        LoadArea();
        LoadState();
        KeyboardUtil.hideKeyboard(this);

    }

    private void LoadState(){
        ArrayList<String> stateList = CommonList.GetStateList();
        if (stateList.size()>0) {
            MaterialSpinnerAdapter adpState = new MaterialSpinnerAdapter<String>(getBaseContext(), stateList);
            spinner_state.setAdapter(adpState);
            mState = "Tamil Nadu";
            spinner_state.setHint(mState);
            spinner_state.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                    view.setSelectedIndex(position);
                    if (view != null) {
                        mState = item.toLowerCase().toString();
                    }
                }

            });
        }
        ArrayList<String> salutationList = CommonList.GetSalutationList();
        if (salutationList.size()>0) {
            MaterialSpinnerAdapter adpSalutation = new MaterialSpinnerAdapter<String>(getBaseContext(), salutationList);
            spinner_salutation.setAdapter(adpSalutation);
            mSalutation = "Mr.";
            spinner_salutation.setHint(mSalutation);
            spinner_salutation.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                    view.setSelectedIndex(position);
                    if (view != null) {
                        mSalutation = item;
                    }
                }

            });
        }
    }

    private void LoadArea(){
        if (Global.LOGIN_USER_DETAIL!=null) {

            final ProgressDialog dialog = new ProgressDialog(DonorEntry.this, R.style.MyAlertDialogStyle);
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
                                areaMessage.setArea("Select Area");
                                areaMessage.setKey("-1");
                                areaMessage.setIsactive(true);
                                areaMessage.setRegionkey("-1");
                                areas.add(areaMessage);

                                for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                                    Area area = areaSnapshot.getValue(Area.class);
                                    if (area.getDeleted()==0) {
                                        area.setKey(areaSnapshot.getKey());
                                        areas.add(area);
                                    }
                                }
                                if (areas.size() > 0) {
                                    MaterialSpinnerAdapter adpArea = new MaterialSpinnerAdapter<Area>(getBaseContext(), areas);
                                    spinner_area.setAdapter(adpArea);
                                    if (!ENTRY_MODE_NEW){
                                        spinner_area.setText(mDonor.getArea());
                                        mArea = new Area();
                                        if (mDonor.getRegionkey()!=null){
                                            mArea.setRegionkey(mDonor.getRegionkey());
                                        }
                                        mArea.setIsactive(true);
                                        mArea.setKey(mDonor.getAreakey());
                                    }
                                    spinner_area.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Area>() {
                                        @Override
                                        public void onItemSelected(MaterialSpinner view, int position, long id, Area item) {
                                            view.setSelectedIndex(position);
                                            if (view != null) {
                                                mArea = item;
                                            }
                                        }

                                    });
                                }
                                dialog.dismiss();
                            } else {
                                dialog.dismiss();
                                Messages.ShowToast(getApplicationContext(), "Please check area created for this region");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });

        }else{
            Messages.ShowToast(getApplicationContext(), "Selected region not found");
        }
    }

    private CalendarConstraints.Builder limitRange() {

        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();

        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();

        int year = 2020;
        int startMonth = 2;
        int startDate = 15;

        int endMonth = 3;
        int endDate = 20;
        calendarStart.set(1920, startMonth - 1, startDate - 1);
        calendarEnd.set(year - 1, endMonth - 1, endDate);

        long minDate = calendarStart.getTimeInMillis();
        long maxDate = calendarEnd.getTimeInMillis();


        constraintsBuilderRange.setStart(minDate);
        constraintsBuilderRange.setEnd(maxDate);
        //constraintsBuilderRange.setValidator(new RangeValidator(minDate, maxDate));

        return constraintsBuilderRange;
    }

    @Override
    public void onBackPressed() {
        Intent iMain = new Intent(DonorEntry.this, _NAVIGATE_FROM.equals("MAIN") ? MainActivity.class : DonorsList.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void SaveRecord() {

        final ProgressDialog dialog = ProgressDialog.show(DonorEntry.this, null, "Updating..", true);
        dialog.show();

        DatabaseReference mDataRef = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DONORS);
        mDonor = new Donor();
        if (ENTRY_MODE_NEW) {
            mPrimaryKey = mDataRef.push().getKey();
        } else {
            mDonor = Global.SELECTED_DONOR_MODEL;
            mPrimaryKey = Global.DONOR_KEY;
        }

        mSalutation = mSalutation!="" || mSalutation.isEmpty() ? mSalutation : "Mr.";
        mDonor.setDonor(mDonorName);
        mDonor.setSalutation(mSalutation);
        mDonor.setAadhar(mAadharNo);
        mDonor.setPan(mPanNo);
        mDonor.setBirthdate(mBirthDate);
        mDonor.setGender(mGender);
        mDonor.setMarried(mMarried);
        mDonor.setWeddate(mWedDate);
        mDonor.setAddrline1(mAddressLine1);
        mDonor.setAddrline2(mAddressLine2);
        mDonor.setCity(mCity);
        mDonor.setPincode(mPincode);
        mDonor.setEmail(mEmail);
        mDonor.setDistrict(mDistrict);
        mDonor.setState(mState);
        mDonor.setCountry("INDIA");
        mDonor.setMobile(mMobile);
        mDonor.setWhatsapp(mWhatsAppNo);
        mDonor.setMembertype(mMemberType);
        mDonor.setAreakey(mArea.getKey());
        mDonor.setArea(mArea.getArea());
        if (ENTRY_MODE_NEW) {
            mDonor.setTransactionon(new Date().getTime());
        }
        mDonor.setUpdatedon(ENTRY_MODE_NEW ? "NA" : Global.GetCurrentDateAsString());
        if (ENTRY_MODE_NEW) {
            mDonor.setIsactive(true);
            mDonor.setCreatedon(Global.GetCurrentDateAsString());
            if (Global.LOGIN_USER_DETAIL.getRegionkey() != null) {
                mDonor.setRegionkey(Global.LOGIN_USER_DETAIL.getRegionkey());
            } else {
                mDonor.setRegionkey("NA");
            }
            mDonor.setAreakey("NA");
            if (Global.LOGIN_USER_DETAIL.getUsertype() == 3) {// AreaPerson
                //Field Person Comes Under
                mDonor.setPersonkey(Global.LOGIN_BY_AREA_PERSON.getKey());
                mDonor.setOfficerkey(Global.LOGIN_BY_AREA_PERSON.getOfficerkey());
                mDonor.setEnrolledby("AP");
                if (Global.LOGIN_BY_AREA_PERSON.getAreakey() != null) {
                    mDonor.setAreakey(Global.LOGIN_BY_AREA_PERSON.getAreakey());
                }
            } else {
                //Field Officer Directly Entrolled the Donor
                mDonor.setEnrolledby("FO");
                mDonor.setOfficerkey(Global.LOGIN_BY_FIELD_OFFICER.getKey());
                mDonor.setPersonkey(Global.LOGIN_BY_FIELD_OFFICER.getKey()); //
                if (Global.LOGIN_BY_FIELD_OFFICER.getAreakey() != null) {
                    mDonor.setAreakey(Global.LOGIN_BY_FIELD_OFFICER.getAreakey());
                }
            }
        }
        mDonor.setKey(mPrimaryKey);
        mDataRef.child(mPrimaryKey).setValue(mDonor).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dialog.dismiss();
                if (ENTRY_MODE_NEW) {
                    Messages.ShowToast(getApplicationContext(), "New donor has been added successfully");
                    Global.DONOR_KEY = mPrimaryKey;
                    Global.DONOR_NAME = mDonorName;
                    Global.SELECTED_DONOR_MODEL = mDonor;
                    Intent iMain = new Intent(DonorEntry.this, UploadPhoto.class);
                    iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(iMain);
                } else {
                    Messages.ShowToast(getApplicationContext(), "Donor detail  has been updated successfully");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Messages.ShowToast(getApplicationContext(), "Unable to add new donoor");
                dialog.dismiss();
            }
        });
    }

    private boolean Validate() {

        boolean isvalid = true;
        mDonorName = input_donor_name.getText().toString();
        mGender = radio_button_female.isChecked() ? "Female" : radio_button_male.isChecked() ? "Male" : "Unknown";
        mMarried = radio_button_married.isChecked() ? "Married" : "Single";

        mBirthDate = input_birth_date.getText().toString();
        mWedDate = input_web_date.getText().toString();

        mMemberType = radio_button_member.isChecked() ? "MEMBER" : "NONMEMBER";

        mAadharNo = input_aadhar_no.getText().toString() != null ? input_aadhar_no.getText().toString() : "NA";
        mPanNo = input_pan.getText().toString() != null ? input_pan.getText().toString() : "NA";


        mAddressLine1 = input_address_line1.getText().toString();
        mAddressLine2 = input_address_line2.getText().toString();
        mCity = input_city.getText().toString();
        mPincode = input_district.getText().toString();
        mDistrict = input_district.getText().toString();
        //mState = input_state.getText().toString();

        mEmail = input_email.getText().toString() != null ? input_email.getText().toString() : "NA";
        mMobile = input_mobile_no.getText().toString();
        mWhatsAppNo = input_whatsapp_no.getText().toString() != null ? input_whatsapp_no.getText().toString() : "NA";
        mPincode = input_pincode.getText().toString();

        if (mDonorName.isEmpty()) {
            input_donor_name.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_donor_name.setError(null);
        }
//        if (mAadharNo.isEmpty()) {
//            input_aadhar_no.setError("Cannot be empty");
//            isvalid = false;
//        } else {
//            if (mAadharNo.length() != 12) {
//                input_aadhar_no.setError("Length must be 12");
//                isvalid = false;
//            } else {
//                input_aadhar_no.setError(null);
//            }
//        }

//        if (mPanNo.isEmpty()) {
//            input_pan.setError("Cannot be empty");
//            isvalid = false;
//        } else {
//            if (input_pan.length() != 6) {
//                input_pan.setError("Length must be 6");
//                isvalid = false;
//            } else {
//                input_pan.setError(null);
//            }
//        }
        spinner_area.setError(null);
        if (mArea==null){
            spinner_area.setError("Select Area");
            isvalid=false;
        }else{
            if (mArea.getKey().equals("-1")){
                spinner_area.setError("Select Area");
                Messages.ShowToast(getApplicationContext(),"Select Donor area");
                isvalid=false;
            }
        }
        if (mAddressLine1.isEmpty()) {
            input_address_line1.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_address_line1.setError(null);
        }

        if (mCity.isEmpty()) {
            input_city.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_city.setError(null);
        }

        if (mPincode.isEmpty()) {
            input_pincode.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_pincode.setError(null);
        }
        if (mDistrict.isEmpty()) {
            input_district.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_district.setError(null);
        }
        if (mState.isEmpty()) {
            spinner_state.setError("Cannot be empty");
            isvalid = false;
        } else {
            spinner_state.setError(null);
        }
        if (mMobile.isEmpty() || mMobile.length() != 10) {
            input_mobile_no.setError("Enter valid Mobile No");
            isvalid = false;
        } else {
            input_mobile_no.setError(null);
        }

//        if (mWhatsAppNo.isEmpty() || mWhatsAppNo.length() != 10) {
//            input_whatsapp_no.setError("Enter valid WhatsApp No");
//            isvalid = false;
//        } else {
//            input_whatsapp_no.setError(null);
//        }
//
//        if (mEmail.isEmpty() || mEmail.length() < 5) {
//            input_email.setError("Enter valid Email");
//            isvalid = false;
//        }

        return isvalid;
    }

    public void ValidateEmailAddress() {

        if (!mEmail.isEmpty()) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            final ProgressDialog dialog = ProgressDialog.show(DonorEntry.this, null, "Validating email id", true);
            dialog.show();
            auth.fetchSignInMethodsForEmail(mEmail).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    if (task.getResult().getSignInMethods().size() > 0) {
                        dialog.dismiss();
                        input_email.setError("Email id already exist. Please try new email id");
                        Global.ShowSnackMessage(DonorEntry.this, "Email id already exist. Please try new email id");
                        isValid = false;
                    } else {
                        SaveRecord();
                        dialog.dismiss();
                        isValid = true;
                    }
                }
            });

        }
    }

    //Location
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;
    private static final int INITIAL_REQUEST = 100;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private void InitLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }
    private void updateLocationUI() {

        //Messages.ShowToast(getApplicationContext(),"Coming");
        if (mCurrentLocation != null) {
            getCompleteAddressString(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
            stopLocationUpdates();
        }
        //toggleButtons();
    }
    public void startLocationButtonClick() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }
    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location captured", Toast.LENGTH_SHORT).show();
                        //button_location.setEnabled(true);
                        //toggleButtons();
                    }
                });
    }
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //Log.i(TAG, "All location settings are satisfied.");

                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                //        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(DonorEntry.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    // Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                //s  Log.e(TAG, errorMessage);

                                Toast.makeText(DonorEntry.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI();
                    }
                });
    }
    private void getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                //strAdd = strReturnedAddress.toString();
                //Log.w("My Current loction address", strReturnedAddress.toString());
                new MaterialDialog.Builder(DonorEntry.this)
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (returnedAddress.getAddressLine(0).split(",").length>3) {
                                    String sFlat=  returnedAddress.getAddressLine(0).split(",")[0] + returnedAddress.getAddressLine(0).split(",")[1];
                                    input_address_line1.setText(sFlat);
                                    input_address_line2.setText(returnedAddress.getAddressLine(0).split(",")[2]);
                                }
                                input_address_line1.setText(returnedAddress.getLocality());
                                input_city.setText(returnedAddress.getSubAdminArea());
                                input_pincode.setText(returnedAddress.getPostalCode());
                                //String knownName = returnedAddress.getFeatureName();
                                //String state = returnedAddress.getAdminArea();
                                //String country = returnedAddress.getCountryName();
                                //
                            }
                        })
                        .title("Captured Address")
                        .content(strReturnedAddress.toString() +"\n\n" +" Do you want to add this to address ?").show();
            } else {
                //Log.w("My Current loction address", "No Address returned!");
                Messages.ShowToast(getApplicationContext(),"No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log.w("My Current loction address", "Canont get Address!");
        }
    }


}
