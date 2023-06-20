package com.prgs.etithe.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.prgs.etithe.R;
import com.prgs.etithe.adapter.DonorAdapter;
import com.prgs.etithe.adapter.ReceiptAdapter;
import com.prgs.etithe.models.DateHolder;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.models.FundType;
import com.prgs.etithe.models.Receipt;
import com.prgs.etithe.models.ReceiptLine;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.GridSpacingItemDecoration;
import com.prgs.etithe.utilities.KeyboardUtil;
import com.prgs.etithe.utilities.Messages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
//import crl.android.pdfwriter.Array;
import io.reactivex.annotations.NonNull;

public class ReceiptsList extends AppCompatActivity {

    BottomNavigationView bottonNavigationView;
    @BindView(R.id.rvReceipts)
    RecyclerView rvReceipts;

    @BindView(R.id.tvNoRecordFound)
    TextView tvNoRecordFound;

    @BindView(R.id.txtTotalAmount)
    TextView tvTotalAmount;

    @BindView(R.id.lyTotal)
    LinearLayout lyTotal;

    ReceiptAdapter adapter;
    ArrayList<Receipt> mReceipts = new ArrayList<Receipt>();
    ArrayList<Receipt> mListFilter = new ArrayList<Receipt>();

    private ValueEventListener mDonorsValueListener;
    private DatabaseReference mDatabaseReference;
    Toolbar mToolbarView;
    String _NAVIGATE_FROM = null;
    String mFundType;
    FundType mSelectedFund;
    String FROM_DATE="";
    String TO_DATE="";
    RadioButton radio_dialog_all,radio_button_dialog_cheque, radio_button_dialog_cash,radio_button_dialog_neft;
    MaterialSpinner spinner_dialog_fund_type;
    TextView input_dialog_from_date, input_dialog_to_date;
    DatePickerDialog picker;
    CheckBox chkShowCancelled;
    String PAYMENT_MODE="ALL";
    DateHolder fromDate= null;
    DateHolder toDate= null;
    boolean IsCancelled=false;
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
        LoadNavigation();
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
        if (Global.FUND_TYPE_LIST == null) {
            Global.GET_FUND_TYPES();
        }
        setSupportActionBar(mToolbarView);

        FROM_DATE=GetToday();
        TO_DATE=FROM_DATE;

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
    private String GetToday(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        return  formattedDate;
    }
    private void LoadNavigation(){
        bottonNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottonNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.btnFilter:
                    ShowFilterDialog();
                    break;
                case R.id.btnReset:
                    ResetList();
                    break;
                case R.id.btnBack:
                   onBackPressed();
                    break;
            }
            return true;
        });
    }

    private void ShowFilterDialog() {

        final MaterialDialog dialogCheque = new MaterialDialog.Builder(ReceiptsList.this)
                .title("Filter")
                .autoDismiss(false)
                .customView(R.layout.dialog_receipts_filter, true)
                .positiveText("Apply")
                .positiveColor(getBaseContext().getResources().getColor(R.color.md_green_800))
                .negativeColor(getBaseContext().getResources().getColor(R.color.md_red_700))
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (radio_dialog_all.isChecked()){
                            PAYMENT_MODE="ALL";
                        }else if (radio_button_dialog_cash.isChecked()){
                            PAYMENT_MODE="CASH";
                        }else if (radio_button_dialog_cheque.isChecked()) {
                            PAYMENT_MODE="CHEQUE";
                        }else{
                            PAYMENT_MODE="NEFT";
                        }
                        LoadFilter();
                        dialog.dismiss();
                    }
                }).build();
        radio_dialog_all = (RadioButton) dialogCheque.findViewById(R.id.radio_dialog_all);

        radio_button_dialog_cash = (RadioButton) dialogCheque.findViewById(R.id.radio_dialog_cash);
        radio_button_dialog_neft = (RadioButton) dialogCheque.findViewById(R.id.radio_dialog_neft);
        radio_button_dialog_cheque = (RadioButton) dialogCheque.findViewById(R.id.radio_dialog_cheque);
        spinner_dialog_fund_type = (MaterialSpinner) dialogCheque.findViewById(R.id.spinner_fund_type);
        input_dialog_from_date = (TextView) dialogCheque.findViewById(R.id.input_from_date);
        input_dialog_to_date = (TextView) dialogCheque.findViewById(R.id.input_to_date);
        input_dialog_from_date.setText(FROM_DATE);
        input_dialog_to_date.setText(TO_DATE);
        chkShowCancelled =(CheckBox)dialogCheque.findViewById(R.id.chkShowCancelled);
        chkShowCancelled.setChecked(IsCancelled);

        if(PAYMENT_MODE.equals("ALL")){
            radio_dialog_all.setChecked(true);
        }else if(PAYMENT_MODE.equals("CASH")){
            radio_button_dialog_cash.setChecked(true);
        }else if(PAYMENT_MODE.equals("CHEQUE")){
            radio_button_dialog_cheque.setChecked(true);
        }else{
            radio_button_dialog_neft.setChecked(true);
        }

        ArrayList<FundType> mFundTypes = new ArrayList<FundType>();
        FundType mLocalFundType= new FundType();
        mLocalFundType.setCode("ALL");
        mLocalFundType.setFundtype("ALL");
        mFundTypes.add(mLocalFundType);
        for(FundType mItem : Global.FUND_TYPE_LIST){
            mFundTypes.add(mItem);
        }
        MaterialSpinnerAdapter adpFundType = new MaterialSpinnerAdapter<FundType>(getBaseContext(), mFundTypes);
        spinner_dialog_fund_type.setAdapter(adpFundType);
        spinner_dialog_fund_type.setSelectedIndex(0);
        if (Global.FUND_TYPE_LIST != null) {
            mSelectedFund = Global.FUND_TYPE_LIST.get(0);
        }
        spinner_dialog_fund_type.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<FundType>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, FundType item) {
                view.setSelectedIndex(position);
                if (view != null) {
                    mFundType = item.getKey();
                    mSelectedFund = item;
                    // Messages.ShowToast(getApplicationContext(), mSelectedFund.getKey());
                }
            }
        });

        input_dialog_from_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                if (fromDate!=null){
                    day=fromDate.getDay();
                    month=fromDate.getMonth();
                    year=fromDate.getYear();
                }
                // date picker dialog
                picker = new DatePickerDialog(ReceiptsList.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String day = String.valueOf(dayOfMonth).length()==1? "0"+ String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
                                String month = String.valueOf(monthOfYear+1).length()==1? "0"+ String.valueOf(monthOfYear+1) : String.valueOf(monthOfYear);
                                FROM_DATE = day + "/" + month + "/" + year;
                                input_dialog_from_date.setText(FROM_DATE);
                                fromDate=new DateHolder();
                                fromDate.setDay(dayOfMonth);
                                fromDate.setMonth(monthOfYear);
                                fromDate.setYear(year);
                                fromDate.setFulldate(FROM_DATE);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
        input_dialog_to_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);


                if (toDate!=null) {
                    day = toDate.getDay();
                    month = toDate.getMonth();
                    year = toDate.getYear();
                }
                picker = new DatePickerDialog(ReceiptsList.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String day = String.valueOf(dayOfMonth).length()==1? "0"+ String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
                                String month = String.valueOf(monthOfYear+1).length()==1? "0"+ String.valueOf(monthOfYear+1) : String.valueOf(monthOfYear);
                                TO_DATE = day + "/" + month + "/" + year;
                                input_dialog_to_date.setText(TO_DATE);

                                toDate=new DateHolder();
                                toDate.setDay(dayOfMonth);
                                toDate.setMonth(monthOfYear);
                                toDate.setYear(year);

                                toDate.setFulldate(TO_DATE);
                            }
                        }, year, month, day);

                picker.show();
            }
        });
        dialogCheque.show();
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
                            Double dblAmount=0d;
                            for (DataSnapshot donorSnapshot : dataSnapshot.getChildren()) {
                                Receipt receipt = donorSnapshot.getValue(Receipt.class);
                                receipt.setKey(donorSnapshot.getKey());
                                if (receipt.getCancel()==0) {
                                    dblAmount += receipt.getAmount();
                                }
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
                                tvTotalAmount.setText(Global.GetFormatedAmountWithCurrency(String.valueOf(dblAmount)));

                                //LoadFilter();
                            } else {
                                tvTotalAmount.setText("");
                                rvReceipts.setVisibility(View.GONE);
                                tvNoRecordFound.setVisibility(View.VISIBLE);
                            }
                            dialog.dismiss();
                        } else {
                            rvReceipts.setVisibility(View.GONE);
                            tvNoRecordFound.setVisibility(View.VISIBLE);
                            dialog.dismiss();
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
                            Double dblAmount=0d;
                            for (DataSnapshot donorSnapshot : dataSnapshot.getChildren()) {
                                Receipt receipt = donorSnapshot.getValue(Receipt.class);
                                receipt.setKey(donorSnapshot.getKey());
                                if (receipt.getCancel()==0) {
                                    dblAmount += receipt.getAmount();
                                }
                                mReceipts.add(receipt);
                            }
                           // Messages.ShowToast(getApplicationContext(),String.valueOf((mReceipts.size())));
                            if (mReceipts.size() > 0) {
                                //Sort By Latest Receipt
                                Collections.sort(mReceipts, new Comparator<Receipt>() {
                                    public int compare(Receipt obj1, Receipt obj2) {
                                        return (obj1.getCreatedon() > obj2.getCreatedon()) ? -1 : (obj1.getCreatedon() > obj2.getCreatedon()) ? 1 : 0;
                                    }
                                });
                                tvTotalAmount.setText(Global.GetFormatedAmountWithCurrency(String.valueOf(dblAmount)));
                                rvReceipts.setVisibility(View.VISIBLE);
                                tvNoRecordFound.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();

                               // LoadFilter();
                            } else {
                                rvReceipts.setVisibility(View.GONE);
                                tvNoRecordFound.setVisibility(View.VISIBLE);
                                tvTotalAmount.setText("");

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

    private void LoadFilter() {


        if (mReceipts != null && mReceipts.size() > 0) {
            //Messages.ShowToast(getApplicationContext(),String.valueOf((mReceipts.size())));

            if (!input_dialog_from_date.getText().toString().isEmpty() && !input_dialog_to_date.getText().toString().isEmpty()) {
                Double dblAmount=0d;
                mListFilter.clear();
                try {

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date fromDate = sdf.parse(input_dialog_from_date.getText().toString());
                    long startDate = fromDate.getTime();

                    Date toDate = sdf.parse(input_dialog_to_date.getText().toString());
                    long endDate = toDate.getTime();

                    if (startDate > endDate) {
                        Messages.ShowToast(getApplicationContext(), "ToDate must be grater than FromDate");
                        return;
                    }
                    int totalReceipts=0;
                    for (Receipt receipt : mReceipts) {
                        //Messages.ShowToast(getApplicationContext(), receipt.getPaymode());
                        //if (receipt.getPaymode() == "CHEQUE") {
                            SimpleDateFormat sdfRecet = new SimpleDateFormat("dd/MM/yyyy");
                            Date recept = sdfRecet.parse(receipt.getReceiptdate());
                            long rectDate = recept.getTime();
                            if (PAYMENT_MODE!="ALL"){
                                if (receipt.getPaymode().equals(PAYMENT_MODE)) {
                                    if (rectDate >= startDate && rectDate <= endDate) {

                                        if (receipt.getCancel() == 0) {
                                            dblAmount += receipt.getAmount();
                                        }
                                        if (chkShowCancelled.isChecked()) {
                                            IsCancelled = true;
                                            mListFilter.add(receipt);
                                        } else {
                                            IsCancelled = false;
                                            if (receipt.getCancel() == 0) {
                                                mListFilter.add(receipt);
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                if (rectDate >= startDate && rectDate <= endDate) {

                                    if (receipt.getCancel() == 0) {
                                        dblAmount += receipt.getAmount();
                                    }
                                    if (chkShowCancelled.isChecked()) {
                                        IsCancelled = true;
                                        mListFilter.add(receipt);
                                    } else {
                                        IsCancelled = false;
                                        if (receipt.getCancel() == 0) {
                                            mListFilter.add(receipt);
                                        }
                                    }
                                }
                            }
                        //}
                    }
                } catch (Exception ex) {

                }
                //Messages.ShowToast(getApplicationContext(),String.valueOf(mListFilter));
                if (mListFilter.size()==0){
                    tvNoRecordFound.setText("No record(s) found for the filtered criteria");
                    tvNoRecordFound.setVisibility(View.VISIBLE);
                    rvReceipts.setVisibility(View.INVISIBLE);
                    lyTotal.setVisibility(View.INVISIBLE);
                }else{
                    tvNoRecordFound.setVisibility(View.GONE);
                    lyTotal.setVisibility(View.VISIBLE);
                    rvReceipts.setVisibility(View.VISIBLE);
                }
                tvTotalAmount.setText(Global.GetFormatedAmountWithCurrency(String.valueOf(dblAmount)));
                adapter = new ReceiptAdapter(getApplicationContext(), mListFilter, _NAVIGATE_FROM);
                rvReceipts.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                //Messages.ShowToast(getApplicationContext(), String.valueOf(mListFiter.size()));
            }
        }
    }

    private void ResetList(){
        adapter = new ReceiptAdapter(getApplicationContext(), mReceipts, _NAVIGATE_FROM);
        rvReceipts.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if (mReceipts!=null) {
            Double dblAmount=0d;
            for (Receipt receipt : mReceipts) {
                if (receipt.getCancel() == 0) {
                    dblAmount += receipt.getAmount();
                }
            }
            tvTotalAmount.setText(Global.GetFormatedAmountWithCurrency(String.valueOf(dblAmount)));
        }
    }


}
