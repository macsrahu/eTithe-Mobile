package com.prgs.etithe.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.prgs.etithe.R;
import com.prgs.etithe.models.AreaPerson;
import com.prgs.etithe.models.FieldOfficer;
import com.prgs.etithe.models.Salutations;
import com.prgs.etithe.models.Slideshow;
import com.prgs.etithe.service.TrackingService;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.InternalStorage;
import com.prgs.etithe.utilities.Messages;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private static final int PERMISSIONS_REQUEST = 100;
    private boolean doubleBackToExitPressedOnce = false;

    private Toolbar mToolbar;
    private SliderLayout mDemoSlider;
    private Drawer result = null;
    private ValueEventListener mUserValueListener;
    private Bundle mSavedInstanceState;
    private String versionInfo;
    private ValueEventListener mOfficerValueListener, mPersonValueListener, mSlideValueListener;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            mSavedInstanceState = savedInstanceState;
            getVersionInfo();

            mToolbar = Global.PrepareToolBar(this, true, getResources().getString(R.string.app_name));
            mToolbar.setTitle(getResources().getString(R.string.app_name));

            if (Global.LOGIN_USER_DETAIL == null) {
                Global.GET_LOGIN_INFO_FROM_MEMORY(getApplicationContext());
            }
            if (Global.LOGIN_USER_DETAIL != null) {
                Global.GetRegionNameByRegionKey(Global.LOGIN_USER_DETAIL.getRegionkey());
                if (Global.FUND_TYPE_LIST == null) {
                    Global.GET_FUND_TYPES();
                }
            }

            SetUserDetailByUserType();
            InitDrawerMenu(mToolbar);
            SlideShowBuild();
            BuiltMenu();
            LocationAccessPermission();
            LoadSalutations();

        } catch (Exception ex) {
            Messages.ShowToast(getApplicationContext(), ex.getMessage());
        }

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void LocationAccessPermission() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Toast.makeText(getApplicationContext(), "GPS tracking is not enabled", Toast.LENGTH_LONG).show();
        }
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }

    }

    private void getVersionInfo() {
        String versionName = "";
        int versionCode = -1;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //textViewVersionInfo.setText(String.format("Version %s.%d", versionName, versionCode));
        versionInfo = String.format(" Version %s.%d", versionName, versionCode);
        //TextView textViewVersionInfo = (TextView) findViewById(R.id.textview_version_info);
        //textViewVersionInfo.setText(String.format("Version name = %s \nVersion code = %d", versionName, versionCode));
    }

    private void LoadSalutations() {
        Global.SALUTATIONS = Global.READ_SALUTATIONS_FROM_MEMORY(getApplicationContext());
        if (Global.SALUTATIONS == null) {
            final ProgressDialog dialog = ProgressDialog.show(this,
                    null,
                    "Loading data..",
                    true);

            dialog.show();

            FirebaseDatabase.getInstance().getReference().child(FirebaseTables.TBL_SALUTATION)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Global.SALUTATIONS = new ArrayList<Salutations>();
                                for (DataSnapshot salutationNode : dataSnapshot.getChildren()) {
                                    Salutations salutationModel = salutationNode.getValue(Salutations.class);
                                    if (salutationModel != null) {
                                        Global.SALUTATIONS.add(salutationModel);
                                        //Messages.ShowToast(getApplicationContext(), salutationModel.getCode());
                                    }
                                }
                                if (Global.SALUTATIONS != null && Global.SALUTATIONS.size() > 0) {
                                    Global.WRITE_SALUTATIONS_TO_MEMORY(getApplicationContext());
                                }
                                dialog.dismiss();
                            } else {
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    private void SetUserDetailByUserType() {
        if (Global.LOGIN_USER_DETAIL != null) {
            if (Global.USER_TYPE == 3) { //Area Person
                mToolbar.setSubtitle(Global.LOGIN_USER_DETAIL.getName() + " - [Area Person]");
                if (Global.LOGIN_BY_AREA_PERSON == null) {
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_AREA_PERSONS);
                    mPersonValueListener = mDatabaseReference.child(Global.LOGIN_USER_DETAIL.getUserkey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Global.LOGIN_BY_AREA_PERSON = dataSnapshot.getValue(AreaPerson.class);
                                Global.LOGIN_BY_AREA_PERSON.setKey(dataSnapshot.getKey());
                                Global.WRITE_OBJECT_TO_MEMORY(getApplicationContext(), 3);
                            }
                            //dialog.hide();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //dialog.hide();
                        }
                    });
                }
            } else {
                mToolbar.setSubtitle(Global.LOGIN_USER_DETAIL.getName() + " - [Field Officer]");
                if (Global.LOGIN_BY_FIELD_OFFICER == null) {
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_FIELD_OFFICERS);
                    mOfficerValueListener = mDatabaseReference.child(Global.LOGIN_USER_DETAIL.getUserkey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Global.LOGIN_BY_FIELD_OFFICER = dataSnapshot.getValue(FieldOfficer.class);
                                Global.LOGIN_BY_FIELD_OFFICER.setKey(dataSnapshot.getKey());
                                Global.WRITE_OBJECT_TO_MEMORY(getApplicationContext(), 2);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //dialog.hide();
                        }
                    });
                }

            }
        }
    }


    private void InitDrawerMenu(Toolbar toolbar) {


        String mImgUrl = (Global.USER_TYPE == 3 && Global.LOGIN_BY_AREA_PERSON != null) ? Global.LOGIN_BY_AREA_PERSON.getImgurl() :
                (Global.USER_TYPE == 2 && Global.LOGIN_BY_FIELD_OFFICER != null) ? Global.LOGIN_BY_FIELD_OFFICER.getImgurl() : "NA";
        AccountHeader headerResult = null;
        //Messages.ShowToast(getApplicationContext(),mImgUrl);
        if (mImgUrl != null && !mImgUrl.equals("NA") && !mImgUrl.isEmpty()) {
            //Messages.ShowToast(getApplicationContext(),"HERE");
            DrawerImageLoader.init(new AbstractDrawerImageLoader() {
                @Override
                public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                    Picasso.with(imageView.getContext()).load(mImgUrl).placeholder(placeholder).into(imageView);
                }

                @Override
                public void cancel(ImageView imageView) {
                    Picasso.with(imageView.getContext()).cancelRequest(imageView);
                }
            });
            headerResult = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.drawable.header)
                    .addProfiles(
                            new ProfileDrawerItem()
                                    .withTextColor(getResources().getColor(R.color.white))
                                    .withIsExpanded(false)
                                    .withName(Global.LOGIN_USER_DETAIL != null ? Global.LOGIN_USER_DETAIL.getName() : "eTithe")
                                    .withEmail(Global.LOGIN_USER_DETAIL != null ? Global.LOGIN_USER_DETAIL.getEmail() : "NA")
                                    .withIcon(mImgUrl)

                    )
                    .build();

        } else {
            headerResult = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.drawable.header)
                    .addProfiles(
                            new ProfileDrawerItem()
                                    .withTextColor(getResources().getColor(R.color.white))
                                    .withName(Global.LOGIN_USER_DETAIL != null ? Global.LOGIN_USER_DETAIL.getName() : "eTithe")
                                    .withEmail(Global.LOGIN_USER_DETAIL != null ? Global.LOGIN_USER_DETAIL.getEmail() : "NA")
                                    .withIcon(getResources().getDrawable(R.drawable.profile))
                    )
                    .build();
        }

        result = new DrawerBuilder(this)
                .withAccountHeader(headerResult)
                .withRootView(R.id.main)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withFullscreen(false)
                .withSelectedItem(getResources().getColor(R.color.colorPrimaryDark))
                .addDrawerItems(
                        new SecondaryDrawerItem().withName(R.string.drawer_item_profile).withIcon(FontAwesome.Icon.faw_user).withTextColor(getResources().getColor(R.color.dark_blue)),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_reset).withIcon(FontAwesome.Icon.faw_unlock).withTextColor(getResources().getColor(R.color.dark_blue)),
                        //new SecondaryDrawerItem().withName(R.string.drawer_item_printer).withIcon(FontAwesome.Icon.faw_print).withTextColor(getResources().getColor(R.color.dark_blue)),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_logout).withIcon(FontAwesome.Icon.faw_power_off).withTextColor(getResources().getColor(R.color.dark_blue)
                        )
                )
                .withSavedInstance(mSavedInstanceState)
                .build();
        //this is the line change icon color
        result.getActionBarDrawerToggle().getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        result.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                if (position == 1) {
                    Intent iProfile = new Intent(MainActivity.this, ProfileEntry.class);
                    iProfile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(iProfile);
                } else if (position == 2) {
                    Intent iResetPassword = new Intent(MainActivity.this, ChangePassword.class);
                    iResetPassword.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(iResetPassword);
                } else if (position == 3) {
                    Logout(true);
                }
                return false;
            }
        });

    }

    private void BuiltMenu() {


        findViewById(R.id.cardNewDonor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Global.DONOR_KEY = "";
                Global.SELECTED_DONOR_MODEL = null;
                if (Global.LOGIN_USER_DETAIL != null) {
                    Intent iDonorEntry = new Intent(MainActivity.this, DonorEntry.class);
                    iDonorEntry.putExtra("FROM", "MAIN");
                    iDonorEntry.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(iDonorEntry);
                    finish();
                } else {
                    if (Global.GET_LOGIN_INFO_FROM_MEMORY(getApplicationContext()) > 0) {
                        Intent iDonorEntry = new Intent(MainActivity.this, DonorEntry.class);
                        iDonorEntry.putExtra("FROM", "MAIN");
                        iDonorEntry.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(iDonorEntry);
                        finish();
                    }
                }
            }
        });

        findViewById(R.id.cardDonors).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iDonorsList = new Intent(MainActivity.this, DonorsList.class);
                iDonorsList.putExtra("FROM", "MAIN");
                iDonorsList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iDonorsList);
                finish();
            }
        });

        findViewById(R.id.cardLedger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.SELECTED_DONOR_MODEL = null;
                Intent iReceipts = new Intent(MainActivity.this, ReceiptsList.class);
                iReceipts.putExtra("FROM", "MAIN");
                iReceipts.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iReceipts);
                finish();

            }
        });
        findViewById(R.id.cardAnnouncement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iLocationList = new Intent(MainActivity.this, NotificationsActivity.class);
                iLocationList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iLocationList);
                finish();

            }
        });
    }

    private void Logout(final boolean isLogout) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(getResources().getDrawable(R.drawable.logo_spalsh_blue));
        builder.setMessage(getResources().getString(R.string.dialog_logout_message));
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        Global.USER_CODE = null;
                        Global.USER_TYPE = 0;
                        Global.LOGIN_USER_DETAIL = null;
                        Global.LOGIN_BY_FIELD_OFFICER = null;
                        Global.LOGIN_BY_AREA_PERSON = null;
                        try {
                            if (isLogout) {

                                InternalStorage.resetObject(getApplicationContext(), "USER_INFO");
                                SharedPreferences loginPreferences = getSharedPreferences(Global._ETITHE_REMEMBER_ME, MODE_PRIVATE);
                                SharedPreferences.Editor loginPrefsEditor = loginPreferences.edit();
                                loginPrefsEditor.remove("saveLogin");
                                loginPrefsEditor.remove("loginid");
                                loginPrefsEditor.apply();
                                loginPrefsEditor.commit();
                            }
                        } catch (Exception ex) {

                        }
                        finishAffinity();
                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();

    }


    private void SlideShowBuild() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_SLIDES_SHOW);
        //final TextSliderView textSliderView = new TextSliderView(MainActivity.this);
        mDemoSlider = (SliderLayout) findViewById(R.id.slider);
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this, R.style.MyAlertDialogStyle);
        dialog.setMessage("Loading..");
        dialog.show();
        if (Global.mImageSlide != null && Global.mImageSlide.size() > 0) {
            mDemoSlider.removeAllSliders();
            //mDemoSlider.getPagerIndicator().setBackgroundColor(getResources().getColor(R.color.primary_dark));
            //mDemoSlider.getCurrentSlider().getView().findViewById(R.id.description).setBackgroundColor(getResources().getColor(R.color.primary_dark));
            //Messages.ShowToast(getApplicationContext(), "Slide Count:" +String.valueOf(Global.mImageSlide.size()));
            for (Slideshow imgSld : Global.mImageSlide) {
                TextSliderView textSliderView = new TextSliderView(MainActivity.this);
                textSliderView
                        .description(imgSld.getImagedesc())
                        .image(imgSld.getImageurl())
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(MainActivity.this);

                //add your extra information
                textSliderView.bundle(new Bundle());
                textSliderView.getBundle()
                        .putString("extra", imgSld.getImagedesc());
                mDemoSlider.addSlider(textSliderView);
            }

            //mDemoSlider.setBackgroundColor(getResources().getColor(R.color.primary_dark));
            dialog.dismiss();
        } else {
            Global.mImageSlide = new ArrayList<Slideshow>();

            //Messages.ShowToast(getApplicationContext(), "Slide Count:" +String.valueOf(Global.mImageSlide.size()));

            mSlideValueListener = mDatabaseReference.orderByChild("isview").equalTo(1).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot storeSnapshot : dataSnapshot.getChildren()) {
                        Slideshow slide = storeSnapshot.getValue(Slideshow.class);
                        slide.setKey(storeSnapshot.getKey());
                        if (slide != null) {
                            Global.mImageSlide.add(slide);
                        }
                    }

                    mDemoSlider.removeAllSliders();
                    for (Slideshow imgSld : Global.mImageSlide) {
                        TextSliderView textSliderView = new TextSliderView(MainActivity.this);
                        textSliderView
                                .description(imgSld.getImagedesc())
                                .image(imgSld.getImageurl())
                                .setScaleType(BaseSliderView.ScaleType.Fit)
                                .setOnSliderClickListener(MainActivity.this);

                        //add your extra information
                        textSliderView.bundle(new Bundle());
                        textSliderView.getBundle()
                                .putString("extra", imgSld.getImagedesc());

                        mDemoSlider.addSlider(textSliderView);
                    }
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dialog.dismiss();
                }
            });
        }

        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);
        mDemoSlider.addOnPageChangeListener(this);
        mDemoSlider.setPresetTransformer("Accordion");

    }

    public void onStart() {
        super.onStart();
        if (Global.mImageSlide == null || Global.mImageSlide.size() == 0) {
            SlideShowBuild();
        } else {
            for (Slideshow imgSld : Global.mImageSlide) {
                TextSliderView textSliderView = new TextSliderView(MainActivity.this);
                textSliderView
                        .description(imgSld.getImagedesc())
                        .image(imgSld.getImageurl())
                        .setScaleType(BaseSliderView.ScaleType.FitCenterCrop)
                        .setOnSliderClickListener(MainActivity.this);

                //add your extra information
                textSliderView.bundle(new Bundle());
                textSliderView.getBundle()
                        .putString("extra", imgSld.getImagedesc());
                mDemoSlider.addSlider(textSliderView);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOfficerValueListener != null && mDatabaseReference != null) {
            mDatabaseReference.removeEventListener(mOfficerValueListener);
        }
        if (mPersonValueListener != null && mDatabaseReference != null) {
            mDatabaseReference.removeEventListener(mPersonValueListener);
        }
        if (mSlideValueListener != null && mDatabaseReference != null) {
            mDatabaseReference.removeEventListener(mSlideValueListener);
        }
    }

    public void onStop() {
        super.onStop();
        mDemoSlider.stopAutoCycle();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //android.os.Process.killProcess(android.os.Process.myPid());
            finishAffinity();
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Messages.ShowToast(this, "Please click BACK again to exit");
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 3000);

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            Toast.makeText(this, "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTrackerService() {
        if (!isMyServiceRunning(TrackingService.class)) {
            Intent myIntent = new Intent(MainActivity.this, TrackingService.class);
            startService(myIntent);
            Toast.makeText(this, "Tracking Service started..", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}