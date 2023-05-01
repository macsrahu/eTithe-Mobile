package com.prgs.etithe.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.adapters.ToolbarBindingAdapter;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.prgs.etithe.R;
import com.prgs.etithe.adapter.PaymentAdapter;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.models.FundType;
import com.prgs.etithe.models.OnGetDataListener;
import com.prgs.etithe.models.ReceiptLine;
import com.prgs.etithe.models.Receipt;
import com.prgs.etithe.models.ReceiptSettings;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.GridSpacingItemDecoration;
import com.prgs.etithe.utilities.InternalStorage;
import com.prgs.etithe.utilities.KeyboardUtil;
import com.prgs.etithe.utilities.Messages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;

public class ReceiptEntry extends AppCompatActivity {

    @BindView(R.id.text_view_donor)
    TextView text_view_donor;

    @BindView(R.id.text_view_address)
    TextView text_view_address;

    @BindView(R.id.input_amount)
    TextInputEditText input_amount;

    @BindView(R.id.input_note)
    TextInputEditText input_note;

    @BindView(R.id.input_bank_name)
    TextInputEditText input_bank_name;

    @BindView(R.id.input_cheque_no)
    TextInputEditText input_cheque_no;

    @BindView(R.id.text_view_total)
    public TextView text_view_total;

    @BindView(R.id.radio_button_cash)
    RadioButton radio_button_cash;

    @BindView(R.id.radio_button_cheque)
    RadioButton radio_button_cheque;

    @BindView(R.id.spinner_fund_type)
    MaterialSpinner spinner_fund_type;

    @BindView(R.id.spinner_month)
    MaterialSpinner spinner_month;

    @BindView(R.id.layBankDetail)
    LinearLayout layBankDetail;

    @BindView(R.id.button_add_payment)
    MaterialButton button_add_payment;

    @BindView(R.id.rvReceipts)
    public RecyclerView rvReceipts;

    @BindView(R.id.text_view_no_payment)
    TextView text_view_no_payment;

    BottomNavigationView bottonNavigationView;
    Donor mDonor;

    Receipt mReceipt;
    ProgressDialog mProgressDialog = null;
    String mFundType, mMonth, mPayMode, mChequeNo, mBankName, mReceiptAmount, mNotes;
    FundType mSelectedFund;

    TextInputEditText input_dialog_cheque_date, input_cheque_amount,input_dialog_neft_ref, input_dialog_cheque_no, input_cheque_date, input_dialog_bank_name;
    RadioButton radio_button_dialog_cheque, radio_button_dialog_cash,radio_button_dialog_neft;
    LinearLayout layBankDetail_dialog,layNeftDetail_dialog;
    MaterialSpinner spinner_dialog_fund_type;
    View positiveAction;

    double dblTotalReceipt = 0;

    DatabaseReference mDataRref;
    ValueEventListener mValuelistener;
    ArrayList<ReceiptLine> mReceiptLineList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt_entry);
        ButterKnife.bind(this);

        text_view_total.setText("");
        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "New Receipt");
        setSupportActionBar(mToolbarView);

        mChequeNo = "NA";
        mBankName = "NA";
        if (Global.SELECTED_DONOR_MODEL != null) {
            mDonor = Global.SELECTED_DONOR_MODEL;
        }
        if (Global.FUND_TYPE_LIST == null) {
            Global.GET_FUND_TYPES();
        }
        InitializeControls();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent iMain = new Intent(ReceiptEntry.this, DonorsList.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDataRref != null && mValuelistener != null) {
            mDataRref.removeEventListener(mValuelistener);
        }
    }

    private void PaymentEntry() {

        final MaterialDialog dialogCheque = new MaterialDialog.Builder(ReceiptEntry.this)
                .title("Amount Detail")
                .autoDismiss(false)
                .customView(R.layout.dialog_cheque_entry, true)
                .positiveText("OK")
                .positiveColor(getBaseContext().getResources().getColor(R.color.primary_dark))
                .negativeColor(getBaseContext().getResources().getColor(R.color.primary_dark))
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
                        String amount = input_amount.getText().toString();
                        String bank_name = input_dialog_bank_name.getText().toString();
                        String cheque_no = input_dialog_cheque_no.getText().toString();
                        String neft_refer = input_dialog_neft_ref.getText().toString();
                        String cheque_date = ""; //input_dialog_cheque_date.getText().toString();
                        KeyboardUtil.hideKeyboard(ReceiptEntry.this);
                        if (amount != null) {
                            if (!amount.isEmpty()) {
                                if (!CheckFund(mSelectedFund.getKey())) {
                                    ReceiptLine receiptLine = new ReceiptLine();
                                    receiptLine.setAmount(Double.parseDouble(amount));
                                    receiptLine.setFundkey(mSelectedFund.getKey());
                                    receiptLine.setFundtype(mSelectedFund.getFundtype());
                                    receiptLine.setBankname("NA");
                                    receiptLine.setChequeno("NA");
                                    receiptLine.setChequedate("NA");
                                    receiptLine.setPaymode(radio_button_dialog_cash.isChecked() ? "CASH" : radio_button_dialog_cheque.isChecked()? "CHEQUE" : "NEFT");
                                    if (radio_button_dialog_cheque.isChecked()) {
                                        if (!bank_name.isEmpty() && !cheque_no.isEmpty()) {
                                            receiptLine.setBankname(bank_name);
                                            receiptLine.setChequeno(cheque_no);
                                            receiptLine.setChequedate(cheque_date);
                                            mReceiptLineList.add(receiptLine);
                                            LoadReceiptList();
                                            KeyboardUtil.hideKeyboard(ReceiptEntry.this);
                                            dialog.dismiss();
                                        } else {
                                            input_dialog_bank_name.setError("Cannot be empty");
                                            input_dialog_cheque_no.setError("Cannot be empty");
                                           // input_dialog_cheque_date.setError("Cannot be empty");
                                        }
                                    }else if(radio_button_dialog_neft.isChecked()){
                                        if (!neft_refer.isEmpty()) {
                                            receiptLine.setPaymode("NEFT");
                                            receiptLine.setBankname("NEFT");
                                            receiptLine.setChequeno(neft_refer);
                                            mReceiptLineList.add(receiptLine);
                                            LoadReceiptList();
                                            KeyboardUtil.hideKeyboard(ReceiptEntry.this);
                                            dialog.dismiss();
                                        } else {
                                            input_dialog_neft_ref.setError("Cannot be empty");
                                        }
                                    } else {
                                        mReceiptLineList.add(receiptLine);
                                        LoadReceiptList();
                                        dialog.dismiss();
                                        KeyboardUtil.hideKeyboard(ReceiptEntry.this);
                                    }
                                } else {
                                    Messages.ShowToast(getApplicationContext(), "This fund already payed,select other one");
                                }
                            } else {
                                Messages.ShowToast(getApplicationContext(), "Enter Amount!!");
                                input_amount.setError("Enter Amount !!");
                            }
                        } else {
                            Messages.ShowToast(getApplicationContext(), "Enter Amount!!");
                            input_amount.setError("Enter Amount !!");
                        }
                    }
                }).build();

        positiveAction = dialogCheque.getActionButton(DialogAction.POSITIVE);
        layBankDetail_dialog = (LinearLayout) dialogCheque.findViewById(R.id.layBankDetail);
        layNeftDetail_dialog = (LinearLayout) dialogCheque.findViewById(R.id.layNeftDetail);
        input_amount = (TextInputEditText) dialogCheque.findViewById(R.id.input_amount);
        radio_button_dialog_cash = (RadioButton) dialogCheque.findViewById(R.id.radio_button_cash);
        radio_button_dialog_neft = (RadioButton) dialogCheque.findViewById(R.id.radio_button_neft);

        radio_button_dialog_cheque = (RadioButton) dialogCheque.findViewById(R.id.radio_button_cheque);
        spinner_dialog_fund_type = (MaterialSpinner) dialogCheque.findViewById(R.id.spinner_fund_type);
        input_dialog_cheque_no = (TextInputEditText) dialogCheque.findViewById(R.id.input_dialog_cheque_no);
        input_dialog_bank_name = (TextInputEditText) dialogCheque.findViewById(R.id.input_dialog_bank_name);
        //input_dialog_cheque_date = (TextInputEditText) dialogCheque.findViewById(R.id.input_dialog_cheque_date);
        input_dialog_neft_ref = (TextInputEditText) dialogCheque.findViewById(R.id.input_dialog_neft_ref);

        radio_button_dialog_cash.setChecked(true);

        layBankDetail_dialog.setVisibility(View.GONE);
        layNeftDetail_dialog.setVisibility(View.GONE);
        radio_button_dialog_cheque.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layBankDetail_dialog.setVisibility(View.VISIBLE);
                } else {
                    layBankDetail_dialog.setVisibility(View.GONE);
                }
            }
        });
        radio_button_dialog_neft.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layNeftDetail_dialog.setVisibility(View.VISIBLE);
                } else
                {
                    layNeftDetail_dialog.setVisibility(View.GONE);
                }
            }
        });
        radio_button_dialog_cash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layBankDetail_dialog.setVisibility(View.GONE);
                    layNeftDetail_dialog.setVisibility(View.GONE);
                }
            }
        });

        MaterialSpinnerAdapter adpFundType = new MaterialSpinnerAdapter<FundType>(getBaseContext(), Global.FUND_TYPE_LIST);
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

        dialogCheque.show();
    }

    public void LoadReceiptList() {

        if (mReceiptLineList != null && mReceiptLineList.size() > 0) {
            text_view_no_payment.setVisibility(View.GONE);
            double dblTotal = 0;
            for (ReceiptLine receiptLine : mReceiptLineList) {
                dblTotal = dblTotal + Double.parseDouble(receiptLine.getAmount().toString());
                //Messages.ShowToast(getApplicationContext(), String.valueOf(receiptLine.getAmount()));
            }
            //Messages.ShowToast(getApplicationContext(), String.valueOf(dblTotal));
            //mReceipt.setAmount(dblTotal);
            dblTotalReceipt = dblTotal;
            text_view_total.setText("Receipt Amount: " + Global.GetFormatedAmountWithCurrency(String.valueOf(dblTotal)));

            PaymentAdapter mAdapter = new PaymentAdapter(getApplicationContext(), ReceiptEntry.this, mReceiptLineList);
            rvReceipts.setAdapter(mAdapter);
            rvReceipts.setVisibility(View.VISIBLE);
            KeyboardUtil.hideKeyboard(ReceiptEntry.this);

        }
        //tvNoRecordFound.setVisibility(View.GONE);
    }

    private boolean CheckFund(String fundkey) {
        for (ReceiptLine receiptLine : mReceiptLineList) {
            if (receiptLine.getFundkey().equalsIgnoreCase(fundkey)) {
                return true;
            }
        }
        return false;
    }

    private void InitializeControls() {

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvReceipts.setLayoutManager(mLayoutManager);
        rvReceipts.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(2, getApplicationContext()), false));
        rvReceipts.setItemAnimator(new DefaultItemAnimator());
        if (Global.SELECTED_RECEIPT != null) {
            mReceipt = Global.SELECTED_RECEIPT;
            mReceiptLineList = Global.SELECTED_RECEIPTS_LIST;
            LoadReceiptList();
        }

        button_add_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Global.FUND_TYPE_LIST != null) {
                    if (Global.FUND_TYPE_LIST.size() > 0) {
                        PaymentEntry();
                    } else {
                        Messages.ShowToast(getApplicationContext(), "Fund Type not found.. Please check");
                    }
                } else {
                    Messages.ShowToast(getApplicationContext(), "Fund Type not found.. Please try again");
                }
            }
        });
        if (mDonor != null) {
            String mDonerName = mDonor.getSalutation()!=null? mDonor.getSalutation() +" "+  mDonor.getDonor() : mDonor.getDonor();
            text_view_donor.setText(mDonerName);
            String sAddress = mDonor.getAddrline1() + "\n" + mDonor.getAddrline2() + "\n" + mDonor.getCity() + "\n" + mDonor.getPincode();
            text_view_address.setText(sAddress);
        }
        DateFormat dateFormat = new SimpleDateFormat("MMMM");
        Date date = new Date();
        //Messages.ShowToast (getApplicationContext(), "Month: " + );
        String CURRENT_MONTH = dateFormat.format(date).toUpperCase();
        int CURRENT_MONTH_INDEX = 0;
        ArrayList<String> months = new ArrayList<String>();
        for (int i = 0; i < 12; i++) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
            cal.set(Calendar.MONTH, i);
            String month_name = month_date.format(cal.getTime());
            if (month_name.toUpperCase().equalsIgnoreCase(CURRENT_MONTH)) {
                CURRENT_MONTH_INDEX = i;
            }
            months.add(month_name.toUpperCase());
        }


        MaterialSpinnerAdapter adpMonth = new MaterialSpinnerAdapter<String>(getBaseContext(), months);
        spinner_month.setAdapter(adpMonth);
        spinner_month.setSelectedIndex(CURRENT_MONTH_INDEX);
        mMonth = months.get(CURRENT_MONTH_INDEX);
        spinner_month.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                view.setSelectedIndex(position);
                if (view != null) {
                    mMonth = item.toLowerCase().toString();
                }
            }

        });


        bottonNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottonNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.btnAddPay:

                    if (Global.FUND_TYPE_LIST != null) {
                        if (Global.FUND_TYPE_LIST.size() > 0) {
                            PaymentEntry();
                        } else {
                            Messages.ShowToast(getApplicationContext(), "Fund Type not found.. Please check");
                        }
                    } else {
                        Messages.ShowToast(getApplicationContext(), "Fund Type not found.. Please try again");
                    }
                    break;
                case R.id.btnCreate:
                    //Messages.ShowToast(getApplicationContext(),"Before Validation");
                    if (Validate()) {
                        //Messages.ShowToast(getApplicationContext(),"Validation Pass");
                        CreateReceiptNoAndMoveToSignature(mDonor.getRegionkey());
                    }
                    break;

                case R.id.btnCancel:
                    if (Global.SELECTED_RECEIPTS_LIST != null && Global.SELECTED_RECEIPTS_LIST.size() > 0) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(R.string.app_name);
                        builder.setIcon(getResources().getDrawable(R.drawable.logo_spalsh_blue));
                        builder.setMessage(getResources().getString(R.string.dialog_cancel));
                        builder.setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog,
                                                        final int which) {
                                        onBackPressed();

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
                    } else {
                        onBackPressed();
                        break;
                    }
            }
            return true;
        });
    }

    private boolean Validate() {
        boolean isvalid = true;
        mReceiptAmount = input_amount.getText().toString();
        mBankName = input_bank_name.getText().toString();
        mNotes = input_note.getText().toString();
        mChequeNo = input_cheque_no.getText().toString();
        mPayMode = radio_button_cash.isChecked() ? "CASH" : "CHEQUE";
        if (mReceiptAmount.isEmpty()) {
            input_amount.setError("Cannot be empty");
            isvalid = false;
        } else {
            if (Double.parseDouble(mReceiptAmount) > 0) {
                input_amount.setError(null);
            } else {
                input_amount.setError("Must be valid amount");
            }
        }
        if (radio_button_cheque.isChecked()) {
            if (mBankName.isEmpty()) {
                input_bank_name.setError("Cannot be empty");
                isvalid = false;
            } else {
                input_bank_name.setError(null);
            }
            if (mChequeNo.isEmpty()) {
                input_cheque_no.setError("Cannot be empty");
                isvalid = false;
            } else {
                input_cheque_no.setError(null);
            }
        }
        if (mReceiptLineList == null || mReceiptLineList.size() == 0) {
            Messages.ShowToast(getApplicationContext(), "Enter receipt amount");
            isvalid = false;
        }
        return isvalid;
    }

    private void SaveRecord(String receiptNo) {
        mReceipt = new Receipt();
        mReceipt.setAmount(dblTotalReceipt);
        mReceipt.setReceiptdate(Global.GetCurrentDate());
        mReceipt.setReceiptno(receiptNo);
        mReceipt.setDonorkey(mDonor.getKey());
        if (mDonor.getSalutation()!=null) {
            mReceipt.setDonor(mDonor.getSalutation() +" " + mDonor.getDonor());
        }else{
            mReceipt.setDonor(mDonor.getDonor());
        }
        mReceipt.setPaymonth(mMonth + "-" + Global.GetCurrentYear());
        mReceipt.setNotes(mNotes != null ? "Donation for the month of " + (mMonth + "-" + Global.GetCurrentYear()) : mNotes);
        mReceipt.setAddress(mDonor.getAddrline1() + "\n" + mDonor.getAddrline2() + "\n" + mDonor.getCity() + "\n" + mDonor.getPincode());
        mReceipt.setRegionkey(mDonor.getRegionkey());

        if(mReceiptLineList.size()>0){
            mReceipt.setPaymode(mReceiptLineList.get(0).getPaymode());
        }
        if (Global.USER_TYPE == 3) {
            mReceipt.setRepkey(Global.LOGIN_BY_AREA_PERSON.getKey());
            mReceipt.setReptype("AO");
        } else {
            mReceipt.setRepkey(Global.LOGIN_BY_FIELD_OFFICER.getKey());
            mReceipt.setReptype("OF");
        }
        Global.SELECTED_RECEIPT = mReceipt;
        Global.SELECTED_RECEIPTS_LIST = mReceiptLineList;
        //Messages.ShowToast(getApplicationContext(),"Receipt:" + Global.SELECTED_RECEIPT.getReceiptno());
        Intent intent = new Intent(ReceiptEntry.this, Signature.class);
        startActivity(intent);
        finish();
    }

    public void GetReceiptNoByRegionKey(String regionkey, final OnGetDataListener listener) {
        listener.onStart();
        mDataRref = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_REGION_SETTINGS).child(regionkey);
        mValuelistener = mDataRref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailed(databaseError);
            }
        });
    }

    private void CreateReceiptNoAndMoveToSignature(String child) {
        GetReceiptNoByRegionKey(child, new OnGetDataListener() {
            @Override
            public void onStart() {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(ReceiptEntry.this, R.style.MyAlertDialogStyle);
                    mProgressDialog.setMessage("Initiating Receipt..");
                    mProgressDialog.setIndeterminate(true);
                }
                mProgressDialog.show();
            }

            @Override
            public void onSuccess(DataSnapshot data) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                //final String sReceiptNo = "ETH" + String.valueOf(new Date().getTime());
                if (data.exists()) {
                    ReceiptSettings receiptSettings = data.getValue(ReceiptSettings.class);
                    //long increament = receiptSettings.getReceiptno() + 1;
                    //final String sReceiptNo = receiptSettings.getPrefix() + "-" + String.valueOf(String.format("%06d", increament));
                    final String sReceiptNo = receiptSettings.getPrefix() + "-" + new Date().getTime();
                    if (!sReceiptNo.isEmpty()) {
                        SaveRecord(sReceiptNo);
                    }
                } else {
                    Messages.ShowToast(getApplicationContext(), "Unable to create new receipt");
                    Intent intent = new Intent(ReceiptEntry.this, DonorsList.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                Messages.ShowToast(getApplicationContext(), "Unable to create new receipt");
                Intent intent = new Intent(ReceiptEntry.this, DonorsList.class);
                startActivity(intent);
                finish();
            }
        });

    }
}