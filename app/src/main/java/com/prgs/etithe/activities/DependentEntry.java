package com.prgs.etithe.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.prgs.etithe.R;
import com.prgs.etithe.models.Dependent;
import com.prgs.etithe.models.Relations;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.Messages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;

import static android.widget.Toast.LENGTH_LONG;

public class DependentEntry extends AppCompatActivity {

    @BindView(R.id.spinner_relations)
    MaterialSpinner spinner_relations;

    @BindView(R.id.input_dependent_name)
    TextInputEditText input_dependent_name;

    @BindView(R.id.input_birth_date)
    TextView input_birth_date;


    @BindView(R.id.input_age)
    TextView input_age;


    ArrayList<Relations> mRelations = new ArrayList<Relations>();
    Dependent mDependent;
    String mDepName, mDepBirthDate, mRelation;
    int mAge;
    String _MODE = "ADD";
    String _NAVIGATE_FROM = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dependent_entry);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _NAVIGATE_FROM = extras.getString("FROM");
        }

        String sTitle = Global.SELECTED_DEPENDENT_MODEL != null ? "Update Dependent" : "New Dependent";

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, sTitle);
        setSupportActionBar(mToolbarView);

        if (Global.SELECTED_DEPENDENT_MODEL != null) {
            mDependent = Global.SELECTED_DEPENDENT_MODEL;
            _MODE = "EDIT";
            BindData();
        }
        BindRelations();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void BindData() {
        input_dependent_name.setText(mDependent.getDependent());
        input_birth_date.setText(mDependent.getDepbirthdate());
        input_age.setText(String.valueOf(mDependent.getAge()));
    }

    private int FindPosition(String relation) {
        for (Relations relations : mRelations) {
            if (relations.getKey().toLowerCase().equals(relation.toLowerCase())) {
                return relations.getPosition();
            }
        }
        return 0;
    }

    private void BindRelations() {

        mRelations = Global.GET_RELATIONS();
        MaterialSpinnerAdapter adapterRelation = new MaterialSpinnerAdapter<Relations>(getBaseContext(), mRelations);
        spinner_relations.setAdapter(adapterRelation);
        spinner_relations.setSelected(true);
        if (_MODE == "EDIT" && mDependent != null) {
            //spinner_relations.setText(mDependent.getRelation());
            Toast.makeText(getApplicationContext(), String.valueOf(FindPosition(mDependent.getRelation())), LENGTH_LONG).show();
            spinner_relations.setSelectedIndex(FindPosition(mDependent.getRelation()));
        }
        spinner_relations.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Relations>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Relations item) {
                view.setSelectedIndex(position);
                if (view != null) {
                    mRelation = item.getKey();
                }
            }

        });
        BottomNavigationView bottonNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottonNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.btnSave:
                    if (Validate()) {
                        SaveRecord();
                    }
                    break;
                case R.id.btnCancel:
                    onBackPressed();
                    break;
            }
            return true;
        });

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
                                String[] arrDate = input_birth_date.getText().toString().split("/");
                                if (arrDate.length > 0) {
                                    String age = Global.GET_AGE(Integer.parseInt(arrDate[2]), Integer.parseInt(arrDate[1]), Integer.parseInt(arrDate[0]));
                                    input_age.setText(age);
                                }
                            }
                        });

            }
        });
        input_birth_date.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                input_birth_date.setText("");
                input_age.setText("");
                return false;
            }
        });
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
        calendarStart.set(1950, startMonth - 1, startDate - 1);
        calendarEnd.set(year - 5, endMonth - 1, endDate);

        long minDate = calendarStart.getTimeInMillis();
        long maxDate = calendarEnd.getTimeInMillis();


        constraintsBuilderRange.setStart(minDate);
        constraintsBuilderRange.setEnd(maxDate);
        //constraintsBuilderRange.setValidator(new RangeValidator(minDate, maxDate));

        return constraintsBuilderRange;
    }

    private boolean Validate() {

        boolean isvalid = true;

        mDepName = input_dependent_name.getText().toString();
        mDepBirthDate = input_birth_date.getText().toString();
        //mRelation = spinner_relations.getItems().toString();
        mAge = Integer.parseInt(input_age.getText().toString());

        if (mDepName.isEmpty()) {
            input_dependent_name.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_dependent_name.setError(null);
        }

        if (mDepBirthDate.isEmpty()) {
            input_birth_date.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_birth_date.setError(null);
        }

        if (mRelation.isEmpty()) {
            spinner_relations.setError("Cannot be empty");
            isvalid = false;
        } else {
            spinner_relations.setError(null);
        }

        return isvalid;
    }

    private void SaveRecord() {

        final ProgressDialog dialog = ProgressDialog.show(DependentEntry.this, null, "Updating detail", true);
        dialog.show();
        String mPrimaryKey;
        DatabaseReference mDataRef = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DEPENDENTS);
        if (_MODE.equals("ADD")) {
            mDependent = new Dependent();
            mPrimaryKey = mDataRef.push().getKey();
            mDependent.setKey(mPrimaryKey);
        } else {
            mPrimaryKey = mDependent.getKey();
        }
        mDependent.setDependent(mDepName);
        mDependent.setDepbirthdate(mDepBirthDate);
        mDependent.setRelation(mRelation);
        mDependent.setAge(mAge);

        mDataRef.child(Global.DONOR_KEY).child(mPrimaryKey).setValue(mDependent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dialog.dismiss();
                if (_MODE.equals("ADD")) {
                    Messages.ShowToast(getApplicationContext(), "Dependent added sucessfully");
                } else {
                    Messages.ShowToast(getApplicationContext(), "Dependent updated sucessfully");
                }
                Intent iMain = new Intent(DependentEntry.this, DependentsList.class);
                iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iMain);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Messages.ShowToast(getApplicationContext(), "Unable to add new donoor");
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent iMain = new Intent(DependentEntry.this, _NAVIGATE_FROM.equals("MAIN") ? MainActivity.class : DependentsList.class);
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
}
