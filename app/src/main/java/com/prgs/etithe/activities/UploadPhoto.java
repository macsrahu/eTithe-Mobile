package com.prgs.etithe.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.prgs.etithe.R;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.ImageUtil;
import com.prgs.etithe.utilities.InternalStorage;
import com.prgs.etithe.utilities.Messages;
import com.prgs.etithe.utilities.RealPathUtil;
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
import java.net.URI;
import java.security.Permission;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;
import io.reactivex.annotations.NonNull;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.widget.Toast.LENGTH_LONG;

public class UploadPhoto extends AppCompatActivity {
    View parentLayout;
    FloatingActionButton fabImageSelection;
    @BindView(R.id.imgPicture)
    ImageView imgPicture;

    @BindView(R.id.text_view_donor)
    TextView text_view_donor;

    String mOutputFilePath;

    String _FOLDER_PATH = "eTithe/Pictures";
    public static final int CAMERA = 0;
    public static final int STORAGE = 125;

    boolean PickFromGallary = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_photo);
        setSupportActionBar(Global.PrepareToolBar(this, true, "Upload Picture"));
        ButterKnife.bind(this);
        if (Global.LOGIN_USER_DETAIL==null){
            Global.GET_LOGIN_INFO_FROM_MEMORY(getApplicationContext());
        }
        InitializeControls();
        CheckFolderPermission();
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
                            fabImageSelection.setVisibility(View.GONE);
                            new MaterialDialog.Builder(UploadPhoto.this)
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

    private void InitializeControls() {
        parentLayout = findViewById(android.R.id.content);
        EasyImage.configuration(this)
                .setImagesFolderName(mOutputFilePath)
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(true);

        fabImageSelection = (FloatingActionButton) findViewById(R.id.fabCamera);
        fabImageSelection.setBackgroundColor(getResources().getColor(R.color.primary_color_dark));
        fabImageSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        BottomNavigationView bottonNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottonNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.btnSave:
                    if (!Global.DONOR_KEY.isEmpty()) {
                        if (Global.CheckInternetConnection(parentLayout, getApplicationContext())) {
                            SaveRecord();
                        }
                    } else {
                        Messages.ShowToast(getApplicationContext(), "Donor not yet added");
                    }
                    break;
                case R.id.btnRemove:
                    if (Global.CheckInternetConnection(parentLayout, getApplicationContext())) {
                        if (Global.SELECTED_DONOR_MODEL != null) {
                            if (Global.SELECTED_DONOR_MODEL.getImgurl().isEmpty() && Global.SELECTED_DONOR_MODEL.getImgurl() != "NA") {
                                RemoveImage(Global.SELECTED_DONOR_MODEL.getImgurl());
                            } else {
                                imgPicture.setImageDrawable(getResources().getDrawable(R.drawable.profile));
                            }
                        } else {
                            imgPicture.setImageDrawable(getResources().getDrawable(R.drawable.profile));
                        }
                    }
                    break;
                case R.id.btnCancel:
                    onBackPressed();
                    break;
            }
            return true;
        });
        if (Global.SELECTED_DONOR_MODEL != null) {
            Donor mDonor = Global.SELECTED_DONOR_MODEL;
            text_view_donor.setText(mDonor.getDonor());
            if (mDonor.getImgurl() != null && !mDonor.getImgurl().isEmpty() && !mDonor.getImgurl().equals("NA")) {
                String mImageUrl = "";
                if (!TextUtils.isEmpty(mDonor.getImgurl())) {
                    mImageUrl = mDonor.getImgurl();
                }

                final int radius = 5;
                final int margin = 5;
                final Transformation transformation = new RoundedCornersTransformation(radius, margin);
                Picasso.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.progress_animation).transform(transformation).networkPolicy(NetworkPolicy.OFFLINE).into(imgPicture, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        String sImageUri = mDonor.getImgurl();
                        Picasso.with(getApplicationContext()).load(sImageUri).placeholder(R.drawable.profile).transform(transformation).into(imgPicture);
                    }
                });
            }
        } else {
            //NEW DONOR CASE
            text_view_donor.setText(Global.DONOR_NAME);
        }
    }

    private void CreateFolders() {
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
    private void FolderPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionCheck += ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != 0) {
                ActivityCompat.requestPermissions(UploadPhoto.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE);
            }
        }
    }

    private void RemoveImage(String url) {
        if (url != "NA") {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setIcon(getResources().getDrawable(R.drawable.logo_spalsh_blue));
            builder.setMessage(getResources().getString(R.string.remove_picture));
            builder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                            final int which) {
                            final ProgressDialog dialogProgress = ProgressDialog.show(UploadPhoto.this, null, "Uploading photo..", true);
                            dialogProgress.show();

                            Uri uri = Uri.fromFile(new File(url));
                            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DONORS).child(Global.DONOR_KEY).child("imgurl").setValue("NA");
                                    Messages.ShowToast(getApplicationContext(), "Picture removed successfully");
                                    dialogProgress.dismiss();
                                    Picasso.with(getApplicationContext()).load("NA").placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(imgPicture, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getApplicationContext()).load("NA").placeholder(R.drawable.profile).into(imgPicture);
                                        }
                                    });
                                }
                            });
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    private void SaveRecord() {
        try {
            if (mOutputFilePath != null) {
                //Messages.ShowToast(getApplicationContext(),mOutputFilePath);
                final ProgressDialog dialog = ProgressDialog.show(UploadPhoto.this, null, "Uploading photo..", true);
                dialog.show();

                Uri uri = Uri.fromFile(new File(mOutputFilePath));
                StorageReference filepath = FirebaseStorage.getInstance().getReference(FirebaseTables.TBL_DONORS).child(uri.getLastPathSegment());
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        if (taskSnapshot.getMetadata() != null) {

                            if (taskSnapshot.getMetadata().getReference() != null) {

                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();

                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        if (uri != null) {
                                            String photoStringLink = uri.toString();
                                            FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DONORS).child(Global.DONOR_KEY).child("imgurl").setValue(photoStringLink)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            if (dialog != null) {
                                                                dialog.dismiss();
                                                            }
                                                            if (photoStringLink != null) {
                                                                Global.SELECTED_DONOR_MODEL.setImgurl(photoStringLink);
                                                                Intent intent = new Intent(UploadPhoto.this, DonorEntry.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
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
                        Toast.makeText(getApplicationContext(), "Unable to save picture", LENGTH_LONG).show();

                    }
                });
            }
        }catch (Exception ex){
            Messages.ShowToast(getApplicationContext(), ex.getMessage());
        }
    }

    private void selectImage() {

        final CharSequence[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(UploadPhoto.this);
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
                    EasyImage.openCameraForImage(UploadPhoto.this, 0);


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
                }
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        Intent iDashBoad = new Intent(UploadPhoto.this, DonorEntry.class);
        startActivity(iDashBoad);
        finish();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PickFromGallary) {
            try {
                //Messages.ShowToast(getApplicationContext(),"Req:" + String.valueOf(requestCode) +" Result:" + String.valueOf(resultCode));
                mOutputFilePath = ImageUtil.PrepareImage(getApplicationContext(), requestCode, resultCode, data, RESULT_OK, imgPicture, _FOLDER_PATH, "DONOR");
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
                    //Cancel handling, you might wanna remove taken photo if it was canceled
                    if (source == EasyImage.ImageSource.CAMERA_IMAGE) {
                        File photoFile = EasyImage.lastlyTakenButCanceledPhoto(UploadPhoto.this);
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
                //File mDestination = new File(_FOLDER_PATH +"//temp.jpg");

                File mCompressedFile = new Compressor(this).compressToFile(mFile.getAbsoluteFile());
                mOutputFilePath = mCompressedFile.getAbsolutePath();

             } catch (Exception ex) {
                Messages.ShowToast(getApplication(), "Error:" + ex.getMessage());
            }
         }

    }

    @Override
    protected void onDestroy() {
        // Clear any configuration that was done!
        EasyImage.clearConfiguration(this);
        super.onDestroy();
    }

}
