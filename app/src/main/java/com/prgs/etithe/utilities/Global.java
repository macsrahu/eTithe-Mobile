package com.prgs.etithe.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Region;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prgs.etithe.R;
import com.prgs.etithe.models.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.annotations.NonNull;

public class Global {
    public static String _ETITHE_REMEMBER_ME = "ETITHE_REMEMBER_ME";
    public static String USER_CODE = null;
    public static String DEVICE_REGISTRATION_ID = "";
    public static final String _URL_KEY = "UrlKey";
    public static String CURRENCY_SYMBOL = "₹ ";
    static Boolean isNetAvailable = false;
    public static UserDetails LOGIN_USER_DETAIL = null;
    public static AreaPerson LOGIN_BY_AREA_PERSON = null;
    public static FieldOfficer LOGIN_BY_FIELD_OFFICER = null;
    public static int USER_TYPE = 0;
    public static String DONOR_KEY = "";
    public static String DONOR_NAME = "";
    public static ArrayList<Slideshow> mImageSlide = null;
    public static Donor SELECTED_DONOR_MODEL = null;
    public static Dependent SELECTED_DEPENDENT_MODEL = null;
    public static Region SELECTED_REGION=null;
    public static Regions REGION_MODEL;
    public static List<FundType> FUND_TYPE_LIST;
    public static Receipt SELECTED_RECEIPT;
    public static ArrayList<ReceiptLine> SELECTED_RECEIPTS_LIST;
    public static String PDF_FILE="";
    public static ArrayList<Salutations> SALUTATIONS;
    public static Toolbar PrepareToolBar(final Activity context, boolean isBackButtonVisible, String title) {
        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        toolbar.setTitleTextColor(context.getResources().getColor(R.color.white));
        LayoutInflater li = LayoutInflater.from(context);
        View inflatedLayout = li.inflate(R.layout.toolbar, null, false);

        if (title.isEmpty() || title.equalsIgnoreCase(null)) {
            title = context.getResources().getString(R.string.app_name);
        }

        if (isBackButtonVisible) {
            toolbar.setNavigationIcon(R.drawable.left_arrow_plain);

        }
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(context.getResources().getColor(R.color.white));
        toolbar.setSubtitleTextColor(context.getResources().getColor(R.color.white));

        return toolbar;
    }

    @SuppressLint("WrongConstant")
    public static void ShowSnackMessage(Activity mActivity, String message) {
        View parentLayout = mActivity.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static String GetCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = mdformat.format(calendar.getTime());
        return strDate;
    }

    public static String GetCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy");
        String strDate = mdformat.format(calendar.getTime());
        return strDate;
    }

    public static String GetDateStringByLong(long timestamp) {
        java.util.Date dateObj = new java.util.Date(timestamp);
        SimpleDateFormat dateFormatDDMMYYY = new SimpleDateFormat("dd MMMM-yyyy hh:mm a");
        return dateFormatDDMMYYY.format(dateObj);

    }

    public static String GetFormatedAmount(String amount) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        return decimalFormat.format(Double.parseDouble(amount));
    }

    public static String GetFormatedAmountWithCurrency(String amount) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String fromatedAmount = decimalFormat.format(Double.parseDouble(amount));

        return Global.CURRENCY_SYMBOL + " " + fromatedAmount;

    }

    public static int dpToPx(int dp, Context appContext) {
        Resources r = appContext.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static boolean CheckInternetConnection(final View layout, final Context context) {

        if (!AppStatus.getInstance(context).isOnline()) {

            Snackbar snackbar = Snackbar.make(layout, "No network/wifi connection found!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!AppStatus.getInstance(context).isOnline()) {
                                isNetAvailable = false;
                                //Toast.makeText(context,"No internet connection!",Toast.LENGTH_LONG).show();
                                CheckInternetConnection(layout, context);
                            } else {
                                isNetAvailable = true;
                            }
                        }
                    });

            snackbar.setActionTextColor(Color.RED);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
            isNetAvailable = false;
        } else {
            isNetAvailable = true;
        }
        return isNetAvailable;
    }

    public static String getRegitrationDeviceId(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Config.SHARED_PREF, 0);
        return prefs.getString("regId", "NA");
    }

    public static String GetCurrentDateAsString() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);

        return formattedDate;
    }

    public static int WRITE_LOGIN_INFO_TO_MEMORY(Context context) {
        try {
            InternalStorage.writeObject(context, "LOGIN_INFO", Global.LOGIN_USER_DETAIL);

        } catch (Exception ex) {
            return -111;
        }
        return 1;
    }

    public static int GET_LOGIN_INFO_FROM_MEMORY(Context context) {
        try {
            Global.LOGIN_USER_DETAIL = (UserDetails) InternalStorage.readObject(context, "LOGIN_INFO");
        } catch (Exception ex) {
            return -111;
        }
        return 1;
    }

    public static int WRITE_OBJECT_TO_MEMORY(Context context, int userType) {
        try {
            if (userType == 3) {
                InternalStorage.writeObject(context, "USER_INFO", Global.LOGIN_BY_AREA_PERSON);
            } else {
                InternalStorage.writeObject(context, "USER_INFO", Global.LOGIN_BY_FIELD_OFFICER);
            }
        } catch (Exception ex) {
            return -111;
        }
        return 1;
    }
    public static int WRITE_SALUTATIONS_TO_MEMORY(Context context) {
        try {
            InternalStorage.writeObject(context, "SALUTATIONS", Global.SALUTATIONS);

        } catch (Exception ex) {
            return -111;
        }
        return 1;
    }
    public static ArrayList<Salutations> READ_SALUTATIONS_FROM_MEMORY(Context context) {
        ArrayList<Salutations> mSalutation = null;
        try {
            mSalutation = new ArrayList<Salutations>();
            mSalutation = (ArrayList<Salutations>) InternalStorage.readObject(context, "SALUTATIONS");

        } catch (Exception ex) {
            mSalutation = null;
        }
        return mSalutation;
    }

    public static int GET_OBJECT_FROM_MEMORY(Context context, int userType) {
        try {
            if (userType == 3) {
                Global.LOGIN_BY_AREA_PERSON = (AreaPerson) InternalStorage.readObject(context, "USER_INFO");
            } else {
                Global.LOGIN_BY_FIELD_OFFICER = (FieldOfficer) InternalStorage.readObject(context, "USER_INFO");
            }
        } catch (Exception ex) {
            return -111;
        }
        return 1;
    }

    public static ArrayList<Relations> GET_RELATIONS() {
        Relations mRelation = new Relations();
        ArrayList<Relations> mRelations = new ArrayList<Relations>();

        mRelation.setKey("Spouse");
        mRelation.setPosition(0);
        mRelation.setRelation("Spouse");
        mRelations.add(mRelation);

        mRelation = new Relations();
        mRelation.setKey("Son");
        mRelation.setPosition(1);
        mRelation.setRelation("Son");
        mRelations.add(mRelation);

        mRelation = new Relations();
        mRelation.setKey("Daughter");
        mRelation.setPosition(2);
        mRelation.setRelation("Daughter");
        mRelations.add(mRelation);

        mRelation = new Relations();
        mRelation.setKey("Father");
        mRelation.setPosition(3);
        mRelation.setRelation("Father");
        mRelations.add(mRelation);

        mRelation = new Relations();
        mRelation.setKey("Mother");
        mRelation.setPosition(4);
        mRelation.setRelation("Mother");
        mRelations.add(mRelation);


        return mRelations;
    }

    public static String GET_AGE(int year, int month, int day) {


        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }

    public static CalendarConstraints.Builder DateLimitRange(String selectedDate) {

        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();

        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();
        int year = 2020;
        int startMonth = 1;
        int startDate = 1;
        if (!selectedDate.isEmpty()) {
            if (selectedDate.split("/").length > 0) {
                year = Integer.parseInt(selectedDate.split("/")[2]);
                startMonth = Integer.parseInt(selectedDate.split("/")[1]);
                startDate = Integer.parseInt(selectedDate.split("/")[0]);
            }
        }


        int endMonth = 3;
        int endDate = 20;

        calendarStart.set(year, startMonth - 1, startDate - 1);
        calendarEnd.set(year - 5, endMonth - 1, endDate);

        long minDate = calendarStart.getTimeInMillis();
        long maxDate = calendarEnd.getTimeInMillis();


        constraintsBuilderRange.setStart(minDate);
        constraintsBuilderRange.setEnd(maxDate);
        //constraintsBuilderRange.setValidator(new RangeValidator(minDate, maxDate));

        return constraintsBuilderRange;
    }

    public static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static void GetRegionNameByRegionKey(String regionKey) {

        //Toast.makeText(getApplicationContext(), key, Toast.LENGTH_LONG).show();
        FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_REGIONS).child(regionKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Global.REGION_MODEL = dataSnapshot.getValue(Regions.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void GET_FUND_TYPES() {

        FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_FUND_TYPES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    FUND_TYPE_LIST = new ArrayList<FundType>();
                    for (DataSnapshot donorSnapshot : dataSnapshot.getChildren()) {
                        FundType fundType = donorSnapshot.getValue(FundType.class);
                        fundType.setKey(donorSnapshot.getKey());
                        FUND_TYPE_LIST.add(fundType);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static String GetRegionAddress(Regions mRegion){
        String mAddress="";
        if (mRegion!=null){
            mAddress = mAddress + mRegion.getRegion().trim();
            if (mRegion.getAddressline1()!="NA" && mRegion.getAddressline1() !="" && !mRegion.getAddressline1().isEmpty()){
                mAddress = mAddress + mRegion.getAddressline1().trim() +",";
            }
            if (mRegion.getAddressline2()!="NA" && mRegion.getAddressline2() !="" && !mRegion.getAddressline2().isEmpty()){
                if (mAddress!="") {
                    mAddress = mAddress + mRegion.getAddressline2().trim() + ",";
                }
            }
            if (mRegion.getCity()!="NA" && mRegion.getCity() !="" && !mRegion.getCity().isEmpty()){
                mAddress = mAddress + mRegion.getCity().trim();
            }
            if (mRegion.getPincode()!="NA" && mRegion.getPincode() !="" && !mRegion.getPincode().isEmpty()){
                mAddress = mAddress + "-" + mRegion.getPincode();
            }
            if (mRegion.getPhone()!=null) {
                if (mRegion.getPhone() != "NA" && mRegion.getPhone() != "" && !mRegion.getPhone().isEmpty()) {
                    mAddress = mAddress + "\nPhone: " + mRegion.getPhone();
                }
            }
        }
        return mAddress;
    }

//    public static String Receipt_No = "";
//
//    public static String GetReceiptNoByRegionKey(String regionkey) {
//
//        FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_REGION_SETTINGS).child(regionkey).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    ReceiptSettings receiptSettings = dataSnapshot.getValue(ReceiptSettings.class);
//                    if (receiptSettings != null) {
//                        Receipt_No = receiptSettings.getPrefix() + "-" + String.valueOf(String.format("%03d", receiptSettings.getReceiptno() + 1));
//                    }
//                } else {
//                    return Receipt_No = "ETH" + String.valueOf(new Date().getTime());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
//        return Receipt_No;
//    }

}



