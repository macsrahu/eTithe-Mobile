package com.prgs.etithe.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prgs.etithe.R;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.KeyboardUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.widget.Toast.LENGTH_LONG;

public class ChangePassword extends AppCompatActivity  {
    View parentLayout;
    @BindView(R.id.input_old_password)
    TextInputEditText input_old_password;

    @BindView(R.id.input_password)
    TextInputEditText input_password;

    @BindView(R.id.input_retype_password)
    TextInputEditText input_retype_password;

    @BindView(R.id.button_submit_password)
    MaterialButton _Submit_Button;


    private String _oldPassword, _password, _retypepassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "Change Passsword");
        setSupportActionBar(mToolbarView);
        //Global.colorizeToolbar(mToolbarView, Color.WHITE, this);


        ButterKnife.bind(this);

        parentLayout = findViewById(android.R.id.content);
        _Submit_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Global.CheckInternetConnection(parentLayout, getApplicationContext())) {

                    SubmitChanges();
                }
            }
        });

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right);
    }


    private boolean validate() {
        boolean isInvalid = true;

        _oldPassword = input_old_password.getText().toString();
        _password = input_password.getText().toString();
        _retypepassword = input_retype_password.getText().toString();

        if (_oldPassword.isEmpty()) {
            isInvalid = false;
            input_old_password.setError("Enter Old Passsword");
        } else {
            input_old_password.setError(null);
        }


        if (_password.isEmpty()) {
            input_password.setError("Enter passsword");
            isInvalid = false;
        } else {
            input_password.setError(null);
        }
        if (_retypepassword.isEmpty()) {
            input_retype_password.setError("Enter Retype passsword");
            isInvalid = false;
        } else {
            input_retype_password.setError(null);
        }
        if (!_oldPassword.isEmpty() && !_password.isEmpty())
            if (_oldPassword.equalsIgnoreCase(_password)) {
                Toast.makeText(getApplicationContext(), "New password cannot be the same", LENGTH_LONG).show();
                isInvalid = false;
            } else {
                input_password.setError(null);
            }
        if (!_password.isEmpty() && !_retypepassword.isEmpty()) {
            if (!_password.equalsIgnoreCase(_retypepassword)) {
                isInvalid = false;
                Toast.makeText(getApplicationContext(),"New password doesn't match with Retype Password", LENGTH_LONG).show();
            } else {
                input_retype_password.setError(null);
            }
        }

        return isInvalid;

    }

    private void SubmitChanges() {
        KeyboardUtil.hideKeyboard(this);
        try {
            if (!validate()) {
                return;
            } else {

                final ProgressDialog dialog = ProgressDialog.show(ChangePassword.this, null,"Changing password" , true);
                FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
                mFirebaseAuth.signInWithEmailAndPassword(Global.LOGIN_USER_DETAIL.getEmail(), _oldPassword)
                        .addOnCompleteListener(ChangePassword.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    user.updatePassword(_password)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    dialog.dismiss();
                                                    if (task.isSuccessful()) {
                                                        final AlertDialog.Builder builder = new AlertDialog.Builder(ChangePassword.this);

                                                        builder.setTitle(R.string.app_name);
                                                        builder.setIcon(R.mipmap.ic_launcher);
                                                        builder.setMessage("Password has been changed successfuly..Please login with new password");
                                                        builder.setPositiveButton("OK",
                                                                new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(final DialogInterface dialog,
                                                                                        final int which) {
                                                                        ExpireLogin();

                                                                    }
                                                                });
                                                        final AlertDialog alertDialog = builder.create();
                                                        alertDialog.show();
                                                        onBackPressed();
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                } else {
                                    Toast.makeText(getApplicationContext(), "Invalid old Password..Please verify", LENGTH_LONG).show();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                    }
                });
            }

        } catch (Exception ex) {
            Log.e("PASS",ex.getMessage());
            //ExceptionLogger.RegisterLog(ex, "SubmitChange", ex.getMessage(), Log.ERROR);
        }

    }


    private void ExpireLogin() {

        SharedPreferences loginPreferences = getSharedPreferences(Global._ETITHE_REMEMBER_ME, MODE_PRIVATE);
        SharedPreferences.Editor loginPrefsEditor = loginPreferences.edit();
        loginPrefsEditor.remove("saveLogin");
        loginPrefsEditor.remove("loginid");
        loginPrefsEditor.remove("usertype");
        loginPrefsEditor.apply();
        loginPrefsEditor.commit();
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.signOut();

        Intent iCustomer = new Intent(ChangePassword.this, Login.class);
        iCustomer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iCustomer);
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
    public void onBackPressed() {

        Intent iMain = new Intent(ChangePassword.this, MainActivity.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();

    }
}
