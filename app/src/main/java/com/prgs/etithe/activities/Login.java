package com.prgs.etithe.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Measure;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.prgs.etithe.R;
import com.prgs.etithe.models.MobileDeviceInfo;
import com.prgs.etithe.models.UserDetails;
import com.prgs.etithe.utilities.AppStatus;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.KeyboardUtil;
import com.prgs.etithe.utilities.Messages;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;

public class Login extends AppCompatActivity {

    private static final int REQUEST_SIGNUP = 0;
    @BindView(R.id.input_email)
    TextInputEditText _emailText;

    @BindView(R.id.input_password)
    TextInputEditText _passwordText;

    @BindView(R.id.btn_login)
    MaterialButton _loginButton;

    @BindView(R.id.link_reset_password)
    TextView _reset_password;

    @BindView(R.id.link_signup)
    TextView link_signup;


    @BindView(R.id.tvBuild)
    TextView tvBuild;

    private FirebaseAuth mFirebaseAuth;
    View parentLayout;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        parentLayout = findViewById(android.R.id.content);
        ButterKnife.bind(this);

        /*if (!Global.FORCE_SIGNUP) {
            if (!CheckInstallation()) {
                Intent iSignUp = new Intent(LoginActivity.this, SignupInfo.class);
                iSignUp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iSignUp);
            }
        }*/

//        _emailText.setText("bharath_0774@rediffmail.com");
//        _passwordText.setText("test@123");

         _emailText.setText("rahupathi@gmail.com");
        _passwordText.setText("Test@12345");

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Global.CheckInternetConnection(parentLayout, getApplicationContext())) {
                    FireBaseLogin();
                }else{
                    Messages.ShowToast(getApplicationContext(),"No internet connection");
                }
            }
        });
        //  _loginButton.callOnClick();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }


    @SuppressLint("WrongConstant")
    public void FireBaseLogin() {

        KeyboardUtil.hideKeyboard(this);

        if (!AppStatus.getInstance(this).isOnline()) {
            Global.ShowSnackMessage(Login.this, "No internet connection..Please check");
        } else {
            if (!validate()) {
                return;
            } else {

                final ProgressDialog dialog = new ProgressDialog(Login.this, R.style.MyAlertDialogStyle);
                dialog.setMessage("Authenticating....");
                try {
                    mFirebaseAuth = FirebaseAuth.getInstance();
                    // Global.SetBmsProgressBar("Authenticating...", dialog);
                    mFirebaseAuth.signInWithEmailAndPassword(_emailText.getText().toString(), _passwordText.getText().toString())
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @SuppressLint("WrongConstant")
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();
                                        Global.USER_CODE = task.getResult().getUser().getUid();
                                        GetUserDetail(Global.USER_CODE);
                                    } else {
                                        Messages.ShowToast(Login.this,task.getException().getMessage());
                                        Global.ShowSnackMessage(Login.this, "Invalid user id or password");
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        }
    }

    private void RememberMe(String userid, int usertype) {
        try {
            loginPreferences = getSharedPreferences(Global._ETITHE_REMEMBER_ME, MODE_PRIVATE);
            loginPrefsEditor = loginPreferences.edit();
            loginPrefsEditor.putBoolean("remember_me", true);
            loginPrefsEditor.putString("loginid", userid);
            loginPrefsEditor.putInt("usertype", usertype);
            loginPrefsEditor.apply();
            loginPrefsEditor.commit();

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


    public void onLoginFailed() {
        //Snackbar snackbar = Snackbar.make(parentLayout, "Login failed", Snackbar.LENGTH_LONG);
        // snackbar.setDuration(Snackbar.LENGTH_LONG);
        // snackbar.show();
        _loginButton.setEnabled(true);
    }

    private void GetUserDetail(String key) {

        final ProgressDialog dialog = new ProgressDialog(Login.this, R.style.MyAlertDialogStyle);
        dialog.setMessage("Verifying information....");
        dialog.show();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_USER_DETAIL);
        //Toast.makeText(getApplicationContext(), key, Toast.LENGTH_LONG).show();
        mDatabase.orderByChild("userid").equalTo(key).addValueEventListener(new ValueEventListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dialog.dismiss();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        UserDetails userDetail = userSnapshot.getValue(UserDetails.class);
                        if (userDetail != null) {
                            Global.LOGIN_USER_DETAIL = userDetail;
                            Global.WRITE_LOGIN_INFO_TO_MEMORY(getApplicationContext());


                            //User Type : 1 - WEB_USER
                            //USER TYPE : 2 - FIELD_OFFICER
                            //USER TYPE : 3 - AREA  REP
                            //Toast.makeText(getApplicationContext(),"user type:" + String.valueOf(userDetail.getUserkey()),Toast.LENGTH_LONG).show();
                            if (userDetail.getUsertype() == 2 || userDetail.getUsertype() == 3) { // Not equal to web user
                                if (userDetail.getIsactive() == 1) {
                                    Global.USER_TYPE = Global.LOGIN_USER_DETAIL.getUsertype();
                                    Global.USER_CODE = Global.LOGIN_USER_DETAIL.getUserid();
                                    RememberMe(Global.USER_CODE, Global.USER_TYPE);
                                    // RegisterBrodcaster();
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Global.ShowSnackMessage(Login.this, "You are currently deactivated by Admin, Please contact Admin");
                                }
                            } else {
                                Global.ShowSnackMessage(Login.this, "You are not valid user..Please verify");
                            }
                        }
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(parentLayout, "User detail doest not exist!!. Please verify", Snackbar.LENGTH_LONG);
                    snackbar.setDuration(Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
            }
        });

    }

    public boolean validate() {
        boolean valid = true;


        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Global.ShowSnackMessage(Login.this, "Login ID and Password cannot be empty");
            valid = false;
            return valid;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter valid email address");

        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 15) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void RegisterBrodcaster() {

        Global.DEVICE_REGISTRATION_ID = Global.getRegitrationDeviceId(getApplicationContext());
        if (Global.LOGIN_USER_DETAIL != null) {
            if (!Global.DEVICE_REGISTRATION_ID.isEmpty() && !Global.LOGIN_USER_DETAIL.getKey().isEmpty()) {
                //Toast.makeText(getApplicationContext(),Global.DEVICE_REGISTRATION_ID,Toast.LENGTH_LONG).show();
                if (!Global.DEVICE_REGISTRATION_ID.equalsIgnoreCase("NA")) {
                    DatabaseReference mDataReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_MOBILE_DEVICE_REG);
                    MobileDeviceInfo storesDeviceInfo = new MobileDeviceInfo();
                    storesDeviceInfo.setUsertype(Global.LOGIN_USER_DETAIL.getUsertype());
                    storesDeviceInfo.setIsactive(true);
                    storesDeviceInfo.setDeviceid(Global.DEVICE_REGISTRATION_ID);
                    storesDeviceInfo.setDeviceinfo("MANUFACTURER:" + Build.MANUFACTURER + ",DEVICEID" + Build.ID + ",DEVICE:" + Build.DEVICE);
                    mDataReference.child(Global.LOGIN_USER_DETAIL.getKey()).setValue(storesDeviceInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }


}
