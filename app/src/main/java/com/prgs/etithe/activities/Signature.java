package com.prgs.etithe.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.room.Database;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.prgs.etithe.R;
import com.prgs.etithe.models.OnGetDataListener;
import com.prgs.etithe.models.Receipt;
import com.prgs.etithe.models.ReceiptLine;
import com.prgs.etithe.models.ReceiptSettings;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.ImageCompressor;
import com.prgs.etithe.utilities.InternalStorage;
import com.prgs.etithe.utilities.Messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.widget.Toast.LENGTH_LONG;

public class Signature extends AppCompatActivity {
    SignaturePad signaturePad;

    @BindView(R.id.button_save)
    MaterialButton button_save;

    @BindView(R.id.button_back)
    MaterialButton button_back;

    @BindView(R.id.button_clear)
    MaterialButton button_clear;

    Boolean IsIncremented = false;

    String _FOLDER_PATH = "/eTithe/Pictures";
    ProgressDialog mProgressDialog = null;
    public static final int CAMERA = 0;
    public static final int STORAGE = 125;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt_signature);
        ButterKnife.bind(this);

        signaturePad = (SignaturePad) findViewById(R.id.signaturePad);

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "Signature");
        setSupportActionBar(mToolbarView);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
       // button_save.setEnabled(false);
        //button_clear.setEnabled(false);


        CheckFolderPermission();
        InitializeControl();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void CreateFolders() {

        String MEDIA_MOUNTED = "mounted";
        try {
            File exportDir = new File(getCacheDir(), _FOLDER_PATH);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
        } catch (Exception e) {
            Messages.ShowToast(getApplicationContext(), "There is a problem to create folder");
        }

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

    private void CheckFolderPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            CreateFolders();
                        } else {
                            new MaterialDialog.Builder(Signature.this)
                                    .positiveText("OK")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            onBackPressed();
                                        }
                                    })
                                    .title("Signature Upload")
                                    .content("Permission required to capture sign!!").show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void FolderPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionCheck += ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != 0) {
                ActivityCompat.requestPermissions(Signature.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE);
            }
        }
    }

    private void InitializeControl() {
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
                if (signatureBitmap != null) {
                    SaveImage(Global.SELECTED_RECEIPT.getRegionkey());
                } else {
                    Messages.ShowToast(getApplicationContext(), "Please do signature");
                }
            }
        });
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iMain = new Intent(Signature.this, ReceiptEntry.class);
                iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iMain);
                finish();
            }
        });
        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {
                //button_save.setEnabled(true);
                //button_clear.setEnabled(true);
            }

            @Override
            public void onClear() {
                //button_save.setEnabled(false);
                //button_clear.setEnabled(false);
            }
        });
        BottomNavigationView bottonNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottonNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.btnSubmit:

                    Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
                    if (signatureBitmap != null) {
                        SaveImage(Global.SELECTED_RECEIPT.getRegionkey());
                    } else {
                        Messages.ShowToast(getApplicationContext(), "Please do signature");
                    }
                    break;
                case R.id.btnClear:
                    signaturePad.clear();
                    break;

                case R.id.btnBack:
                    onBackPressed();
                    break;

                case R.id.btnCancel:

                        break;
            }
            return true;
        });
    }
    private void CancelReceipt() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(getResources().getDrawable(R.drawable.logo_spalsh_blue));
        builder.setMessage(getResources().getString(R.string.dialog_cancel_receipt));
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                       Global.SELECTED_RECEIPTS_LIST=null;
                       Global.SELECTED_RECEIPT=null;

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

    public int SaveAndCompress(Bitmap bitmap) throws IOException {
        int retValue = 1;

        File fileSigns = new File(getBaseContext().getExternalCacheDir() + "/" + _FOLDER_PATH, "sign.jpg");
        try {

            FileOutputStream out = new FileOutputStream(fileSigns);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            Messages.ShowToast(getApplicationContext(), e.getMessage());
            e.printStackTrace();
            retValue = -1;
        }


        if (fileSigns.exists()) {
            try {
                ImageCompressor imageCompressor = new ImageCompressor();
                String mPictureOutPath = imageCompressor.compressImage(getBaseContext(), fileSigns.getAbsolutePath(),
                        _FOLDER_PATH,
                        "RECEIPT_" + String.valueOf(System.currentTimeMillis()));
                Global.SELECTED_RECEIPT.setSignurl(mPictureOutPath);
                //Messages.ShowToast(getApplicationContext(),Global.SELECTED_RECEIPT.getSignurl());

            } catch (Exception ex) {
                ex.printStackTrace();
                Messages.ShowToast(getApplicationContext(),"Compress:" + ex.getMessage());
                retValue = -1;
            }
        }
        return retValue;

    }

    @Override
    public void onBackPressed() {
        Intent iMain = new Intent(Signature.this, MainActivity.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }

    private void SaveImage(String ReceiptNo) {
        final ProgressDialog dialog = new ProgressDialog(Signature.this, R.style.MyAlertDialogStyle);
        dialog.setMessage("Creating Receipt..");
        dialog.show();


        Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
        try {
            int iImageSuccess = 0;
            if (signatureBitmap != null) {
                iImageSuccess = SaveAndCompress(signatureBitmap);
                if (iImageSuccess < 0) {
                    Messages.ShowToast(getApplicationContext(), "Unable to get signature");
                    dialog.dismiss();
                    return;
                }
            } else {
                Messages.ShowToast(getApplicationContext(), "Please do the signature");
                dialog.dismiss();
                return;
            }
            if (iImageSuccess > 0) {
                if (!TextUtils.isEmpty(Global.SELECTED_RECEIPT.getSignurl())) {
                    Uri uri = Uri.fromFile(new File(Global.SELECTED_RECEIPT.getSignurl()));
                    if (uri != null) {
                        StorageReference filepath = FirebaseStorage.getInstance().getReference().child(FirebaseTables.TBL_RECEIPTS).child(uri.getLastPathSegment());
                        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                if (taskSnapshot.getMetadata() != null) {

                                    if (taskSnapshot.getMetadata().getReference() != null) {
                                        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String photoStringLink = uri.toString();
                                                dialog.dismiss();
                                                SaveRecord(photoStringLink, ReceiptNo);
                                            }
                                        });
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                Messages.ShowToast(getApplicationContext(), "Unable to save sign so as receipt: Error:" + e.getLocalizedMessage());
                            }
                        });
                    }
                }
            }
        } catch (Exception ex) {
            Messages.ShowToast(getApplicationContext(), ex.getMessage());
            if (dialog != null) {
                dialog.dismiss();
            }

        }
    }

    private void SaveRecord(String urlpath, String receiptNo) {
        if (Global.SELECTED_RECEIPT != null) {

            final ProgressDialog dialog = new ProgressDialog(Signature.this, R.style.MyAlertDialogStyle);
            dialog.setMessage("Creating Receipt..");
            dialog.show();

            Receipt mReceipt = Global.SELECTED_RECEIPT;

            DatabaseReference mDataRef = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_RECEIPTS);
            String mPrimaryKey = mDataRef.push().getKey();
            mReceipt.setKey(mPrimaryKey);
            //mReceipt.setReceiptno(receiptNo);
            mReceipt.setSignurl(urlpath);
            mReceipt.setCreatedon(new Date().getTime());
            mDataRef.child(mPrimaryKey).setValue(mReceipt).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    final int iCount = 0;
                    DatabaseReference mDataReceiptLine = FirebaseDatabase.getInstance().getReference().child(FirebaseTables.TBL_RECEIPTS_LINE);
                    for (ReceiptLine mReceiptLine : Global.SELECTED_RECEIPTS_LIST) {
                        String mLineKey = mDataReceiptLine.push().getKey();
                        mReceiptLine.setKey(mLineKey);
                        mDataReceiptLine.child(mPrimaryKey).child(mLineKey).setValue(mReceiptLine).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialog.dismiss();
                                Intent intent = new Intent(Signature.this, ReceiptFinish.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Messages.ShowToast(getApplicationContext(), "Unable to add new receipt");
                    Intent intent = new Intent(Signature.this, DonorsList.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }


}
