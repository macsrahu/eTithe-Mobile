package com.prgs.etithe.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.prgs.etithe.R;
import com.prgs.etithe.models.AreaPerson;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.models.FieldOfficer;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.ImageUtil;
import com.prgs.etithe.utilities.KeyboardUtil;
import com.prgs.etithe.utilities.Messages;
import com.prgs.etithe.utilities.RoundedCornersTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import io.reactivex.annotations.NonNull;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.widget.Toast.LENGTH_LONG;

public class ProfileEntry extends AppCompatActivity {

    @BindView(R.id.input_rep_name)
    TextInputEditText input_rep_name;


    @BindView(R.id.input_address_line1)
    TextInputEditText input_address_line1;

    @BindView(R.id.input_address_line2)
    TextInputEditText input_address_line2;

    @BindView(R.id.input_city)
    TextInputEditText input_city;

    @BindView(R.id.input_state)
    TextInputEditText input_state;

    @BindView(R.id.input_pincode)
    TextInputEditText input_pincode;

    @BindView(R.id.input_mobile_no)
    TextInputEditText input_mobile_no;

    @BindView(R.id.input_whatsapp_no)
    TextInputEditText input_whatsapp_no;

    @BindView(R.id.input_email)
    TextInputEditText input_email;

    @BindView(R.id.input_detail)
    TextView input_detail;

    @BindView(R.id.input_name)
    TextView input_name;

    @BindView(R.id.imgPicture)
    CircleImageView imgPicture;

    @BindView(R.id.fabCamera)
    FloatingActionButton fabCamera;

    FieldOfficer mFieldOfficer = null;
    AreaPerson mAreaPerson = null;

    String mOutputFilePath;

    DatabaseReference mRootReference;

    boolean ENTRY_MODE_NEW = false;
    Boolean isValid = false;

    String mRepName, mEmail, mMobile, mWhatsAppNo, mAddressLine1, mAddressLine2, mCity, mPincode, mDistrict, mState, mIngUrl;

    String mPrimaryKey = null;
    BottomNavigationView bottonNavigationView;

    String _LOCAL_FOLDER = "eTithe/Pictures";
    public static final int CAMERA = 0;
    public static final int STORAGE = 125;
    boolean PickFromGallary=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        ButterKnife.bind(this);

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "Update Profile");
        setSupportActionBar(mToolbarView);
        input_state.setEnabled(false);

        input_email.setEnabled(false);
        input_mobile_no.setEnabled(false);
        if (Global.LOGIN_USER_DETAIL == null) {
            Global.GET_LOGIN_INFO_FROM_MEMORY(getApplicationContext());
        }

        Global.GET_OBJECT_FROM_MEMORY(getApplicationContext(), Global.USER_TYPE);

        CheckFolderPermission();
        if (Global.USER_TYPE == 3) {
            mAreaPerson = Global.LOGIN_BY_AREA_PERSON;
            if (mAreaPerson != null) {
                if (Global.REGION_MODEL == null) {
                    if (mAreaPerson.getRegionkey()!=null) {
                        Global.GetRegionNameByRegionKey(mAreaPerson.getRegionkey());
                    }
                }
                BindAreaPerson();
            }
        } else {
            mFieldOfficer = Global.LOGIN_BY_FIELD_OFFICER;
            if (mFieldOfficer != null) {
                if (Global.REGION_MODEL == null) {
                    if (mFieldOfficer.getRegionkey()!=null) {
                        Global.GetRegionNameByRegionKey(mFieldOfficer.getRegionkey());
                    }
                }
                BindFieldOfficer();
            }
        }
        InitializeControl();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void CheckFolderPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            CreateFolders();
                        } else {
                            fabCamera.setVisibility(View.GONE);
                            new MaterialDialog.Builder(ProfileEntry.this)
                                    .positiveText("OK")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            onBackPressed();
                                        }
                                    })
                                    .title("Picture Upload")
                                    .content("Permission required to upload picture!!!").show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void InitializeControl() {

        if (Global.USER_TYPE == 3) {
            if (mAreaPerson!=null) {
                input_name.setText(mAreaPerson.getPerson());
                input_detail.setText("✉: " + mAreaPerson.getEmail() + "\n ☎: " + mAreaPerson.getMobile() + "\n " + Global.REGION_MODEL != null ? Global.REGION_MODEL.getRegion() : "");
            }else{
                input_name.setText("Admin");
            }
        } else {
            if (mFieldOfficer!=null) {
                input_name.setText(mFieldOfficer.getOfficer());
                String sFormatted = "✉: " + mFieldOfficer.getEmail() + "\n ☎: " + mFieldOfficer.getMobile() + "\n " + Global.REGION_MODEL != null ? Global.REGION_MODEL.getRegion() : "";
                input_detail.setText(sFormatted);
            }else{
                input_name.setText("Admin");
            }
        }

        EasyImage.configuration(this)
                .setImagesFolderName(mOutputFilePath)
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(true);

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ProfileEntry.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE);
                } else {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ProfileEntry.this, new String[]{Manifest.permission.CAMERA}, CAMERA);
                    } else {
                        selectImage();
                    }
                }
            }
        });
        bottonNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottonNavigationView.getMenu().findItem(R.id.btnSave).setTitle("Update");
        bottonNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.btnSave:

                    if (Validate()) {
                        KeyboardUtil.hideKeyboard(this);
                        if (Global.USER_TYPE == 3) {
                            UpdateAreaPersonRecord();
                        } else {
                            UpdateFieldOfficerRecord();
                        }
                    }
                    break;

                case R.id.btnCancel:
                    onBackPressed();
                    break;
            }
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    private void UploadAreaPersonPicture() {
        if (mOutputFilePath != null && !mOutputFilePath.isEmpty()) {
            final ProgressDialog dialog = ProgressDialog.show(ProfileEntry.this, null, "Uploading photo..", true);
            dialog.show();

            Uri uri = Uri.fromFile(new File(mOutputFilePath));
            if (uri.getLastPathSegment() != null) {
                StorageReference filepath = FirebaseStorage.getInstance().getReference().child(FirebaseTables.TBL_AREA_PERSONS).child(uri.getLastPathSegment());
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String photoStringLink = uri.toString();
                                FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_AREA_PERSONS).child(Global.LOGIN_BY_AREA_PERSON.getKey()).child("imgurl").setValue(photoStringLink)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                if (dialog != null) {
                                                    dialog.dismiss();
                                                }
                                                Messages.ShowToast(getApplicationContext(), "Picture updated successfully");
                                                Intent intent = new Intent(ProfileEntry.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), "Unable to save picture", LENGTH_LONG).show();

                    }
                });
            }
        }
    }

    private void CreateFolders() {

//        String MEDIA_MOUNTED = "mounted";
//        if (getBaseContext().getExternalCacheDir().equals(MEDIA_MOUNTED)) {

        try {
            File exportDir = new File(getCacheDir(), _LOCAL_FOLDER);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
        } catch (Exception e) {
            Messages.ShowToast(getApplicationContext(), "There is a problem to create folder");
        }
//        } else {
//            Messages.ShowToast(getApplicationContext(), "The external disk not mounted");
//        }

    }

    public File getCacheDir() {
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (state == null || state.startsWith(Environment.MEDIA_MOUNTED)) {
            try {
                File file = getApplicationContext().getExternalCacheDir();
                if (file != null) {
                    return file;
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error:" + e.getMessage(), LENGTH_LONG).show();
            }
        }
        try {
            File file = getApplicationContext().getCacheDir();
            if (file != null) {
                return file;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error:" + e.getMessage(), LENGTH_LONG).show();
        }
        return new File("");
    }

    private void UploadingProfilePicture() {
        if (mOutputFilePath != null && !mOutputFilePath.isEmpty()) {
            final ProgressDialog dialog = ProgressDialog.show(ProfileEntry.this, null, "Uploading photo..", true);
            dialog.show();

            Uri uri = Uri.fromFile(new File(mOutputFilePath));
            if (uri.getLastPathSegment() != null) {
                StorageReference filepath = FirebaseStorage.getInstance().getReference().child(FirebaseTables.TBL_FIELD_OFFICERS).child(uri.getLastPathSegment());
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String photoStringLink = uri.toString();
                                FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_FIELD_OFFICERS).child(Global.LOGIN_BY_FIELD_OFFICER.getKey()).child("imgurl").setValue(photoStringLink)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                if (dialog != null) {
                                                    dialog.dismiss();
                                                }
                                                Messages.ShowToast(getApplicationContext(), "Picture updated successfully");
                                                Intent intent = new Intent(ProfileEntry.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), "Unable to save picture", LENGTH_LONG).show();

                    }
                });
            }
        }
    }

    private void BindFieldOfficer() {
        if (mFieldOfficer != null) {
            input_rep_name.setText(mFieldOfficer.getOfficer());
            input_address_line1.setText(mFieldOfficer.getAddressline1());
            input_address_line2.setText(mFieldOfficer.getAddressline2());
            input_city.setText(mFieldOfficer.getCity());
            input_pincode.setText(mFieldOfficer.getPincode());
            input_state.setText(mFieldOfficer.getState());
            input_whatsapp_no.setText(mFieldOfficer.getWhatsappno());
            input_mobile_no.setText(mFieldOfficer.getMobile());
            input_email.setText(mFieldOfficer.getEmail());
            if (mFieldOfficer.getImgurl() != null && !mFieldOfficer.getImgurl().isEmpty() && !mFieldOfficer.getImgurl().equals("NA")) {
                String mImageUrl = "";
                if (!TextUtils.isEmpty(mFieldOfficer.getImgurl())) {
                    mImageUrl = mFieldOfficer.getImgurl();
                }

                final int radius = 5;
                final int margin = 5;
                final Transformation transformation = new RoundedCornersTransformation(radius, margin);
                Picasso.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.profile).transform(transformation).networkPolicy(NetworkPolicy.OFFLINE).into(imgPicture, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        String sImageUri = mFieldOfficer.getImgurl();
                        Picasso.with(getApplicationContext()).load(sImageUri).placeholder(R.drawable.profile).transform(transformation).into(imgPicture);
                    }
                });
            }

        }
    }

    private void BindAreaPerson() {
        if (mAreaPerson != null) {
            input_rep_name.setText(mAreaPerson.getPerson());
            input_address_line1.setText(mAreaPerson.getAddressline1());
            input_address_line2.setText(mAreaPerson.getAddressline2());
            input_city.setText(mAreaPerson.getCity());
            input_pincode.setText(mAreaPerson.getPincode());
            input_state.setText(mAreaPerson.getState());
            input_whatsapp_no.setText(mAreaPerson.getWhatsappno());
            input_mobile_no.setText(mAreaPerson.getMobile());
            input_email.setText(mAreaPerson.getEmail());
            if (mAreaPerson.getImgurl() != null && !mAreaPerson.getImgurl().isEmpty() && !mAreaPerson.getImgurl().equals("NA")) {
                String mImageUrl = "";
                if (!TextUtils.isEmpty(mAreaPerson.getImgurl())) {
                    mImageUrl = mAreaPerson.getImgurl();
                }

                final int radius = 5;
                final int margin = 5;
                final Transformation transformation = new RoundedCornersTransformation(radius, margin);
                Picasso.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.profile).transform(transformation).networkPolicy(NetworkPolicy.OFFLINE).into(imgPicture, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        String sImageUri = mAreaPerson.getImgurl();
                        Picasso.with(getApplicationContext()).load(sImageUri).placeholder(R.drawable.profile).transform(transformation).into(imgPicture);
                    }
                });
            }
        }
    }

    private void UpdateFieldOfficerRecord() {

        final ProgressDialog dialog = ProgressDialog.show(ProfileEntry.this, null, "Updating Record...", true);
        dialog.show();

        DatabaseReference mDataRef = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_FIELD_OFFICERS);
        mPrimaryKey = mFieldOfficer.getKey();

        mFieldOfficer.setOfficer(input_rep_name.getText().toString());
        mFieldOfficer.setAddressline1(mAddressLine1);
        mFieldOfficer.setAddressline2(mAddressLine2);
        mFieldOfficer.setCity(mCity);
        mFieldOfficer.setPincode(mPincode);
        mFieldOfficer.setEmail(mEmail);
        mFieldOfficer.setState(mState);
        mFieldOfficer.setMobile(mMobile);
        mFieldOfficer.setWhatsappno(mWhatsAppNo);
        mFieldOfficer.setIsactive(true);
        mFieldOfficer.setKey(mPrimaryKey);

        mDataRef.child(mPrimaryKey).setValue(mFieldOfficer).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dialog.dismiss();
                Global.LOGIN_BY_FIELD_OFFICER = mFieldOfficer;

                Messages.ShowToast(getApplicationContext(), "Profile updated successfully");
                Intent iMain = new Intent(ProfileEntry.this, MainActivity.class);
                iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iMain);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Global.ShowSnackMessage(ProfileEntry.this, "Unable to update");
            }
        });
    }

    private void UpdateAreaPersonRecord() {

        final ProgressDialog dialog = ProgressDialog.show(ProfileEntry.this, null, "Updating Record", true);
        dialog.show();

        DatabaseReference mDataRef = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_AREA_PERSONS);
        mPrimaryKey = mAreaPerson.getKey();

        mAreaPerson.setPerson(input_rep_name.getText().toString());
        mAreaPerson.setAddressline1(mAddressLine1);
        mAreaPerson.setAddressline2(mAddressLine2);
        mAreaPerson.setCity(mCity);
        mAreaPerson.setPincode(mPincode);
        mAreaPerson.setEmail(mEmail);
        mAreaPerson.setState(mState);
        mAreaPerson.setMobile(mMobile);
        mAreaPerson.setWhatsappno(mWhatsAppNo);
        mAreaPerson.setIsactive(true);
        mAreaPerson.setKey(mPrimaryKey);

        mDataRef.child(mPrimaryKey).setValue(mAreaPerson).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dialog.dismiss();
                Global.LOGIN_BY_AREA_PERSON = mAreaPerson;
//                if (mOutputFilePath != null) {
//                    if (!mOutputFilePath.isEmpty()) {
//                        UploadAreaPersonPicture();
//                    }
//                } else {
                Messages.ShowToast(getApplicationContext(), "Profile updated successfully");
                Intent iMain = new Intent(ProfileEntry.this, MainActivity.class);
                iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iMain);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Messages.ShowToast(ProfileEntry.this, "Unable to update");
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent iMain = new Intent(ProfileEntry.this, MainActivity.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }

    private boolean Validate() {

        boolean isvalid = true;
        mRepName = input_rep_name.getText().toString();
        mAddressLine1 = input_address_line1.getText().toString();
        mAddressLine2 = input_address_line2.getText().toString();
        mCity = input_city.getText().toString();
        mState = input_state.getText().toString();
        mEmail = input_email.getText().toString();
        mMobile = input_mobile_no.getText().toString();
        mWhatsAppNo = input_whatsapp_no.getText().toString();
        mPincode = input_pincode.getText().toString();

        if (mRepName.isEmpty()) {
            input_rep_name.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_rep_name.setError(null);
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
        if (mState.isEmpty()) {
            input_state.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_state.setError(null);
        }

        if (mWhatsAppNo.isEmpty() || mWhatsAppNo.length() != 10) {
            input_whatsapp_no.setError("Enter valid Whats App No");
            isvalid = false;
        } else {
            input_whatsapp_no.setError(null);
        }
        return isvalid;
    }

    private void selectImage() {

        final CharSequence[] options = {"Camera", "Gallery", "Remove"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEntry.this);
        builder.setTitle("Select Picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Camera")) {
//                    try {
//                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        File f = new File(getBaseContext().getExternalCacheDir(), "temp.jpg");
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
//                        startActivityForResult(intent, 1);
//                    } catch (Exception ex) {
//                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
//                    }
                    PickFromGallary = false;
                    EasyImage.openCameraForImage(ProfileEntry.this, 0);

                } else if (options[item].equals("Gallery")) {
                    PickFromGallary = true;
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                } else if (options[item].equals("Pictures")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), 3);
                } else if (options[item].equals("Remove")) {

                }
            }
        });
        builder.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PickFromGallary) {
            try {
                mOutputFilePath = ImageUtil.PrepareImage(getApplicationContext(), requestCode, resultCode, data, RESULT_OK, imgPicture, _LOCAL_FOLDER, "PROFILE");
                if (mOutputFilePath!=""){
                    UploadingProfilePicture();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Messages.ShowToast(getApplicationContext(), e.getMessage());
            }
        }else {
            EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
                @Override
                public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                    //Some error handling
                    e.printStackTrace();
                    Messages.ShowToast(getApplicationContext(), e.getMessage());
                }

                @Override
                public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                    onPhotosReturned(imageFiles);
                }

                @Override
                public void onCanceled(EasyImage.ImageSource source, int type) {
                    if (source == EasyImage.ImageSource.CAMERA_IMAGE) {
                        File photoFile = EasyImage.lastlyTakenButCanceledPhoto(ProfileEntry.this);
                        if (photoFile != null) photoFile.delete();
                    }
                }
            });
        }
    }
    private void onPhotosReturned(List<File> returnedPhotos) {
        for (File mFile: returnedPhotos) {
            try {
                Bitmap myBitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath());

                Bitmap imgRotated = Global.rotateImageIfRequired(getApplicationContext(), myBitmap, Uri.fromFile(mFile));
                imgPicture.setImageBitmap(imgRotated);

                File mCompressedFile = new Compressor(this).compressToFile(mFile.getAbsoluteFile());
                mOutputFilePath = mCompressedFile.getAbsolutePath();
                if (mOutputFilePath!=""){
                    UploadingProfilePicture();
                }
            } catch (Exception ex) {
                Messages.ShowToast(getApplication(), "Error:" + ex.getMessage());
            }
        }

    }

    private void FolderPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionCheck += ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != 0) {
                ActivityCompat.requestPermissions(ProfileEntry.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE);
            }
        }
    }

}
