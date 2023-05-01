package com.prgs.etithe.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.prgs.etithe.R;
import com.prgs.etithe.adapter.PaymentAdapter;
import com.prgs.etithe.adapter.ReceiptAdapter;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.models.Receipt;
import com.prgs.etithe.models.ReceiptLine;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.GridSpacingItemDecoration;
import com.prgs.etithe.utilities.ImageCompressor;
import com.prgs.etithe.utilities.Messages;
import com.prgs.etithe.utilities.RoundedCornersTransformation;
import com.prgs.etithe.utilities.ScreenshotUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import crl.android.pdfwriter.PDFWriter;
import crl.android.pdfwriter.PaperSize;
import crl.android.pdfwriter.StandardFonts;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.annotations.NonNull;

public class ReceiptView extends AppCompatActivity {

    @BindView(R.id.text_view_donor)
    MaterialTextView text_view_donor;

    @BindView(R.id.text_view_detail)
    MaterialTextView text_view_detail;


    @BindView(R.id.text_view_receipt_date)
    MaterialTextView text_view_receipt_date;

    @BindView(R.id.text_view_receipt_no)
    MaterialTextView text_view_receipt_no;

    @BindView(R.id.text_view_mode)
    MaterialTextView text_view_mode;

    @BindView(R.id.text_view_month)
    MaterialTextView text_view_month;

    @BindView(R.id.text_view_amount)
    MaterialTextView text_view_amount;

    @BindView(R.id.text_view_notes)
    MaterialTextView text_view_notes;

    @BindView(R.id.button_back)
    MaterialButton button_back;

    @BindView(R.id.imgPicture)
    CircleImageView imgPicture;

    @BindView(R.id.text_view_bank)
    MaterialTextView text_view_bank;

    @BindView(R.id.layPicture)
    LinearLayout layPicture;

    @BindView(R.id.layShare)
    LinearLayout layShare;


    @BindView(R.id.layReceipt)
    LinearLayout layReceipt;


    @BindView(R.id.lineView)
    View lineView;


    @BindView(R.id.imgShare)
    ImageView imgShare;

    @BindView(R.id.imgSignature)
    ImageView imgSignature;


    @BindView(R.id.imgWhatsApp)
    ImageView imgWhatsApp;

    @BindView(R.id.imgPdf)
    ImageView imgPdf;

    @BindView(R.id.rvReceipts)
    RecyclerView rvReceipts;

    ReceiptAdapter adapter;

    ArrayList<Receipt> mReceipts = new ArrayList<Receipt>();
    public static final int STORAGE = 125;
    private ValueEventListener mDonorsValueListener;
    private DatabaseReference mDatabaseReference;
    private ScrollView parentView;
    Donor mDonor = null;
    Receipt mReceipt;
    String _FOLDER_PATH = "eTithe/pdf";
    String _NAVIGATE_FROM;
    File imagePath;
    ArrayList<ReceiptLine> mReceiptLineList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt_view);
        ButterKnife.bind(this);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _NAVIGATE_FROM = extras.getString("FROM");
        }

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, Global.SELECTED_RECEIPT.getCancel() ==0? "Receipt View" :"Cancelled Receipt");
        setSupportActionBar(mToolbarView);
        parentView = findViewById(R.id.content_scroll);
        BindReceiptDetails();
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        FolderPermission();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            CreateFolders();
        } else {
            CreateFolders();
        }

        imgPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Global.SELECTED_RECEIPT != null) {
                  //  ReceiptCreation(1);
                    if (Global.SELECTED_RECEIPT != null) {
                        WebView();
                    }else
                    {
                        Messages.ShowToast(getApplicationContext(),"Receipt detail not found");
                    }
                }
            }
        });
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void CreateFolders() {

        String MEDIA_MOUNTED = "mounted";
        try {
            File exportDir = new File(getBaseContext().getExternalCacheDir(), _FOLDER_PATH);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            String text = "Receipt No: " + mReceipt.getReceiptno() + "\n Receipt Date: " + mReceipt.getReceiptdate() + "\n Amount: " + text_view_amount.getText() + "\n Donated for the month of " + mReceipt.getPaymonth().toUpperCase();
        } catch (Exception e) {
            Messages.ShowToast(getApplicationContext(), "There is a problem to create folder");
        }


    }

    private void FolderPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionCheck += ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != 0) {
                ActivityCompat.requestPermissions(ReceiptView.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE);
            }
        }
    }

    private void LoadReceiptItems() {

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvReceipts.setLayoutManager(mLayoutManager);
        rvReceipts.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(2, getApplicationContext()), true));
        rvReceipts.setItemAnimator(new DefaultItemAnimator());
        rvReceipts.setVisibility(View.VISIBLE);

        //Messages.ShowToast(getApplicationContext(), mReceipt.getKey());
        final ProgressDialog dialog = new ProgressDialog(ReceiptView.this, R.style.MyAlertDialogStyle);
        dialog.setMessage("Loading..");
        dialog.show();
        mReceiptLineList = new ArrayList<ReceiptLine>();
        FirebaseDatabase.getInstance().getReference().child(FirebaseTables.TBL_RECEIPTS_LINE).child(mReceipt.getKey())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot depenSnapshot : dataSnapshot.getChildren()) {
                                ReceiptLine receiptLine = depenSnapshot.getValue(ReceiptLine.class);
                                receiptLine.setKey(depenSnapshot.getKey());
                                mReceiptLineList.add(receiptLine);
                            }
                            Global.SELECTED_RECEIPTS_LIST = mReceiptLineList;
                            if (mReceiptLineList.size() > 0) {

                                PaymentAdapter mAdapter = new PaymentAdapter(getApplicationContext(), ReceiptView.this, mReceiptLineList);
                                rvReceipts.setAdapter(mAdapter);
                            }
                        } else {
                            Messages.ShowToast(getApplicationContext(), "No Receipts");
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Messages.ShowToast(getApplicationContext(), databaseError.getMessage());
                        dialog.dismiss();
                    }
                });
    }

    public Bitmap takeScreenshot() {
        View u = findViewById(R.id.content_scroll);
        u.setDrawingCacheEnabled(true);
        ScrollView z = (ScrollView) findViewById(R.id.content_scroll);
        int totalHeight = z.getChildAt(0).getHeight();
        int totalWidth = z.getChildAt(0).getWidth();
        u.layout(0, 0, totalWidth, totalHeight);
        u.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(u.getDrawingCache());
        u.setDrawingCacheEnabled(false);
        return b;
    }

    public void saveBitmap(Bitmap bitmap) {
        imagePath = new File(getBaseContext().getExternalCacheDir().getAbsolutePath() + "/screenshot.png");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Messages.ShowToast(getApplicationContext(),"Saved:" + imagePath);
            fos.flush();
            fos.close();

        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
            Messages.ShowToast(getApplicationContext(),"File Not found:" + e.getMessage());
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
            Messages.ShowToast(getApplicationContext(),"Exception:" + e.getMessage());
        }
    }
    public void SaveAndCompress(Bitmap bitmap) throws IOException {
        int retValue = 1;

        File fileSigns = new File(getBaseContext().getExternalCacheDir() + "/" + _FOLDER_PATH, "ScreenShot.jpg");

        try {

//            FileOutputStream out = new FileOutputStream(fileSigns);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            out.flush();
//            out.close();
            String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"screen_shot", null);
            //sUri bitmapUri = Uri.parse(bitmapPath);
            shareIt(bitmapPath);

        } catch (Exception e) {
            Messages.ShowToast(getApplicationContext(), e.getMessage());
            e.printStackTrace();
            retValue = -1;
        }
//        if (fileSigns.exists()) {
//            try {
//                ImageCompressor imageCompressor = new ImageCompressor();
//                String imagePath = imageCompressor.compressImage(getBaseContext(), fileSigns.getAbsolutePath(),
//                        _FOLDER_PATH,
//                        "SCREEN_SHOT_" + String.valueOf(System.currentTimeMillis()));
//                Messages.ShowToast(getApplicationContext(),"Path:" + imagePath);
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                Messages.ShowToast(getApplicationContext(),"Compress:" + ex.getMessage());
//                retValue = -1;
//            }
//        }
    }
    private void shareIt(String bitmapPath) {
        Uri bitmapUri = Uri.parse(bitmapPath);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/*");
        String shareBody = "Receipt: " + mReceipt.getReceiptno();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Please find receipt as attachment for the month of " + mReceipt.getPaymonth());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        layPicture.setVisibility(View.VISIBLE);
        layShare.setVisibility(View.VISIBLE);
        button_back.setVisibility(View.VISIBLE);
        if (mReceipt.getNotes() != null && !mReceipt.getNotes().isEmpty()) {
            text_view_notes.setText(mReceipt.getNotes());
        }
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {

        Intent iMain = new Intent(ReceiptView.this, ReceiptsList.class);
        iMain.putExtra("FROM", _NAVIGATE_FROM);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }

    private void BindReceiptDetails() {
        if (Global.SELECTED_RECEIPT != null) {
            mReceipt = Global.SELECTED_RECEIPT;
            text_view_donor.setText(mReceipt.getDonor());
            text_view_detail.setText(mReceipt.getAddress());

            text_view_receipt_no.setText(mReceipt.getReceiptno());
            text_view_receipt_date.setText(Global.GetDateStringByLong(mReceipt.getCreatedon()).toUpperCase());
            text_view_amount.setText(Global.GetFormatedAmount(String.valueOf(mReceipt.getAmount())));
            // + "\n" + NumberToWord.convert(Integer.parseInt(String.valueOf((mReceipt.getAmount()))))
            //text_view_mode.setText(mReceipt.getPaytype());
            text_view_month.setText(mReceipt.getPaymonth().toUpperCase());
            if (mReceipt.getNotes().isEmpty()) {
                text_view_notes.setText("Receipt for the month of " + mReceipt.getPaymonth().toUpperCase());
            } else {
                text_view_notes.setText(mReceipt.getNotes());
            }
            if (mReceipt.getSignurl() != null && !mReceipt.getSignurl().isEmpty() && !mReceipt.getSignurl().equals("NA")) {
                String mImageUrl = "";
                if (!TextUtils.isEmpty(mReceipt.getSignurl())) {
                    mImageUrl = mReceipt.getSignurl();
                }
                final int radius = 5;
                final int margin = 5;
                final Transformation transformation = new RoundedCornersTransformation(radius, margin);
                imgSignature.setVisibility(View.VISIBLE);
                Picasso.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.profile).transform(transformation).networkPolicy(NetworkPolicy.OFFLINE).into(imgSignature, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        String sImageUri = mReceipt.getSignurl();
                        Picasso.with(getApplicationContext()).load(sImageUri).placeholder(R.drawable.profile).transform(transformation).into(imgSignature);
                    }
                });
            } else {
                imgSignature.setVisibility(View.GONE);
            }

            imgShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    text_view_notes.setText("Receipt for the month of " + mReceipt.getPaymonth().toUpperCase() + "\n\n\n Thank you \n Yours eTithe");
                    Bitmap bitmap  = ScreenshotUtil.getInstance().takeScreenshotForView(parentView);
                    //imageViewShowScreenshot.setImageBitmap(bitmap);
                    //takeScreenshot();
                    //saveBitmap(bitmap);
                    try {
                        SaveAndCompress(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            LoadReceiptItems();
            LoadDonor(mReceipt.getDonorkey());
        }
    }

    private void LoadDonor(String donorKey) {
        final ProgressDialog dialog = new ProgressDialog(ReceiptView.this, R.style.MyAlertDialogStyle);
        dialog.setMessage("Loading..");
        dialog.show();
        FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DONORS).child(donorKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Donor donor = dataSnapshot.getValue(Donor.class);
                            if (donor != null) {
                                mDonor = donor;
                                if (mDonor.getImgurl() != null && !mDonor.getImgurl().isEmpty() && !mDonor.getImgurl().equals("NA")) {
                                    String mImageUrl = "";
                                    if (!TextUtils.isEmpty(mDonor.getImgurl())) {
                                        mImageUrl = mDonor.getImgurl();
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
                                            String sImageUri = mDonor.getImgurl();
                                            Picasso.with(getApplicationContext()).load(sImageUri).placeholder(R.drawable.profile).transform(transformation).into(imgPicture);
                                        }
                                    });
                                }
                            }
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();

                    }
                });
    }


    private void CreatePdf(String sometext) {
        // create a new document
        PdfDocument document = new PdfDocument();
        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawCircle(50, 50, 30, paint);
        paint.setColor(Color.BLACK);
        canvas.drawText(sometext, 80, 50, paint);
        //canvas.drawt
        // finish the page
        document.finishPage(page);
// draw text on the graphics object of the page
        // Create Page 2
        pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 2).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        paint = new Paint();
        paint.setColor(Color.BLUE);
        canvas.drawCircle(100, 100, 100, paint);
        document.finishPage(page);
        // write the document content
        String directory_path = getBaseContext().getCacheDir().getPath() + _FOLDER_PATH;
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdir();
        }
        String targetPdf = directory_path + String.valueOf(new Date().getTime()) + ".pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("main", "error " + e.toString());
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }
        // close the document
        document.close();
    }

    ///------------------------------------------------
    ///PDF GENERATION
    //------------------------------------------
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    private void WebView(){
        Intent iMain = new Intent(ReceiptView.this, WebViewPDF.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }
    private void ReceiptCreation(int menu_type) {
        PDFWriter writer = new PDFWriter(PaperSize.FOLIO_WIDTH, PaperSize.FOLIO_HEIGHT);
        int _START_MARGIN = 890;
        int _FOLIO_WIDTH = 612;
        int _LEFT = 20;
        int _NO_OF_LINES_PER_PAGE = 17;
        Bitmap bitmapLogo = drawableToBitmap(getBaseContext().getResources().getDrawable(R.drawable.logo_print));

        writer.setFont(StandardFonts.SUBTYPE, StandardFonts.MAC_ROMAN_ENCODING);
        String sSubTitle = "Donation Receipt";
        //writer.addRawContent("1 0 0 rg\n");
        // writer.addImage(_LEFT, _START_MARGIN, bitmapLogo);

        String sTitle = getResources().getString(R.string.app_name).toUpperCase();

        writer.addText(((_FOLIO_WIDTH / 2) - (sTitle.length() / 2)) - 20, _START_MARGIN, 20, sTitle);

        _START_MARGIN = _START_MARGIN - 30;
        writer.addText(((_FOLIO_WIDTH / 2) - (sSubTitle.length() / 2) - 60), _START_MARGIN, 20, sSubTitle)
        ;
        //writer.addRawContent("0 0 0 rg\n");

        _START_MARGIN = _START_MARGIN - 20;
        writer.addLine(_LEFT, _START_MARGIN, PaperSize.FOLIO_WIDTH - 10, _START_MARGIN);

        _START_MARGIN = _START_MARGIN - 30;

        String sProdductDesc = rightpad("Receipt No:", 8) + rightpad(Global.SELECTED_RECEIPT.getReceiptno(), 60) + rightpad("Date:", 7) + rightpad(Global.SELECTED_RECEIPT.getReceiptdate(), 10);
        writer.addText(_LEFT, _START_MARGIN, 14, sProdductDesc);

        _START_MARGIN = _START_MARGIN - 20;

        writer.addLine(_LEFT, _START_MARGIN, PaperSize.FOLIO_WIDTH - 10, _START_MARGIN);

        _START_MARGIN = _START_MARGIN - 20;

        String sName = rightpad("Name:", 10) + rightpad(Global.SELECTED_RECEIPT.getDonor(), 100);
        writer.addText(_LEFT, _START_MARGIN, 12, sName);

        _START_MARGIN = _START_MARGIN - 20;
        String sAddress = rightpad("Address:", 10) + rightpad(Global.SELECTED_RECEIPT.getAddress(), 150);
        writer.addText(_LEFT, _START_MARGIN, 12, sAddress);

        DecimalFormat decimalQtyFormat = new DecimalFormat("#.00");
        String receiptAmount = decimalQtyFormat.format(Float.parseFloat((String.valueOf(Global.SELECTED_RECEIPT.getAmount()))));

        _START_MARGIN = _START_MARGIN - 20;
        String sAmount = rightpad("Amount:", 10) + rightpad(getStringAtFixedLength(receiptAmount, 8), 10);
        writer.addText(_LEFT, _START_MARGIN, 12, sAmount);
        _START_MARGIN = _START_MARGIN - 20;

        String strProductName = "";
        double dblTotal = 0;
        int Bottom = _START_MARGIN;
        int iSerialNo = 1;
        if (mReceiptLineList.size() == 1) {
            writer.addText(_LEFT, _START_MARGIN, 12, rightpad("Donation distributed to the fund", 100));
        } else {
            writer.addText(_LEFT, _START_MARGIN, 12, rightpad("Donation distributed to the funds are:", 100));
        }
        _START_MARGIN = _START_MARGIN - 20;
        for (ReceiptLine receiptLine : mReceiptLineList) {
            if (receiptLine.getAmount() > 0) {

                if (receiptLine.getFundtype().length() > 30) {
                    strProductName = receiptLine.getFundtype().substring(0, 30).trim().toUpperCase();
                } else {
                    strProductName = receiptLine.getFundtype().trim().toUpperCase();
                }

                String sDesc = rightpad(String.valueOf(iSerialNo) + ".", 7)
                        + strProductName.toUpperCase();
                Bottom = Bottom - 20;
                writer.addText(_LEFT, Bottom, 12, sDesc);
                //----------------------------------------------------------
                //Price
                //----------------------------------------------------------
                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                String amount = decimalFormat.format(receiptLine.getAmount());
                writer.addText(490, Bottom, 12, getStringAtFixedLength(amount, 8));
                iSerialNo = iSerialNo + 1;
            }
        }
//        writer.addRawContent("1 0 0 rg\n");
        Bottom = Bottom - 20;
        writer.addLine(_LEFT, Bottom, PaperSize.FOLIO_WIDTH - 10, Bottom);
        //------------------------------------------------------------------------
        String sThanks = "Thank you for your great generosity.Your support is invaluable to us, thank you again!";

        Bottom = Bottom - 20;
        writer.addText(_LEFT, Bottom, 10, sThanks);

        outputToFile(Global.SELECTED_RECEIPT.getReceiptno() + ".pdf", writer.asString(), "ISO-8859-1", menu_type);
    }

    private String rightpad(String text, int length) {
        return String.format("%-" + length + "." + length + "s", text);
    }

    private void outputToFile(String fileName, String pdfContent, String encoding, int menu_type) {

        File newFile = new File(getBaseContext().getExternalCacheDir().getAbsolutePath() + "/" + fileName);
        try {
            newFile.createNewFile();
            try {
                FileOutputStream pdfFile = new FileOutputStream(newFile);
                pdfFile.write(pdfContent.getBytes(encoding));
                pdfFile.close();
                //Toast.makeText(getApplicationContext(), "PDF created at " + newFile.getPath(), Toast.LENGTH_LONG).show();

                Uri path = Uri.fromFile(newFile);

                /*Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pdfOpenintent.setDataAndType(path, "application/pdf");
                try {
                    startActivity(pdfOpenintent);
                } catch (ActivityNotFoundException e) {

                }
*/
                if (path != null) {
                    if (menu_type == 1) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("pdf/*");
                        intent.putExtra(Intent.EXTRA_STREAM, path);
                        startActivity(Intent.createChooser(intent, "Share"));
                    } else {
                        Global.PDF_FILE = newFile.getPath();
                        Toast.makeText(getApplicationContext(), "PDF created at " + Global.PDF_FILE, Toast.LENGTH_LONG).show();
                        Intent iPDFView = new Intent(ReceiptView.this, PDFViewer.class);
                        iPDFView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(iPDFView);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to create PDF file", Toast.LENGTH_LONG).show();
                }

            } catch (FileNotFoundException e) {
                // ...
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            // ...
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String writeAtFixedLength(String pString, int length) {
        if (pString != null && !pString.isEmpty()) {
            return getStringAtFixedLength(pString, length);
        } else {
            return completeWithWhiteSpaces("", length);
        }
    }

    private String getStringAtFixedLength(String pString, int length) {
        if (length < pString.length()) {
            return pString.substring(0, length);
        } else {
            return completeWithWhiteSpaces(pString, length - pString.length());
        }
    }

    private String completeWithWhiteSpaces(String pString, int lenght) {
        for (int i = 0; i < lenght; i++)
            pString += " ";
        return pString;
    }


}

