package com.prgs.etithe.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;



import com.prgs.etithe.R;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.Messages;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ShowUPIPaymentCode extends AppCompatActivity {
    Integer QRcodeWidth = 500;
    Integer TOTAL_AMOUNT = 10;

    @BindView(R.id.imgQRCode)
    ImageView imgQRCode;

    @BindView(R.id.text_title)
    TextView text_title;

    @BindView(R.id.txtAmount)
    TextView txtAmount;

    @BindView(R.id.btnNext)
    AppCompatButton btnNext;

    @BindView(R.id.input_ref)
    TextInputEditText input_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_upi_code);

        ButterKnife.bind(this);

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "UPI Detail");
        setSupportActionBar(mToolbarView);
        BindCode();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent iMain = new Intent(ShowUPIPaymentCode.this, ReceiptEntry.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }
    private void BindCode() {

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String referenceNo = input_ref.getText().toString();
                Messages.ShowToast(getApplicationContext(),referenceNo);
                if (!referenceNo.isEmpty()){
                    if (referenceNo.length()!=13) {
                        Intent intent = new Intent(ShowUPIPaymentCode.this, Signature.class);
                        startActivity(intent);
                        finish();
                    }else{
                        input_ref.setError("Reference no should be 13 digit");
                    }
                }else {
                    input_ref.setError("Enter reference no");
                }
            }
        });

        String url = "upi://pay?pa=" +   // payment method.
                "rahupathi-1@okhdfcbank" +         // VPA number.
                "&am="+ String.valueOf(Global.SELECTED_RECEIPT.getAmount()) +       // this param is for fixed amount (non editable).
                "&pn=Ragupathi%P"+      // to showing your name in app.
                "&cu=INR" +                  // Currency code.
                "&mode=02" +                 // mode O2 for Secure QR Code.
                "&orgid=189999" +            //If the transaction is initiated by any PSP app then the respective orgID needs to be passed.
                "&sign=MEYCIQC8bLDdRbDhpsPAt9wR1a0pcEssDaV" +   // Base 64 encoded Digital signature needs to be passed in this tag
                "Q7lugo8mfJhDk6wIhANZkbXOWWR2lhJOH2Qs/OQRaRFD2oBuPCGtrMaVFR23t";

        try {

            Bitmap bitmap = textToImageEncode(url);
            imgQRCode.setImageBitmap(bitmap);
            text_title.setText(Global.REGION_MODEL.getUPI());
            if (Global.SELECTED_RECEIPT.getAmount()>0) {
                txtAmount.setText("Receipt Amount:" + Global.GetFormatedAmountWithCurrency(String.valueOf(Global.SELECTED_RECEIPT.getAmount())));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private Bitmap textToImageEncode(String Value){
         BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(Value,
                    BarcodeFormat.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );
        } catch (IllegalArgumentException | WriterException ex) {
            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];
        for (int y=0; y< bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x =0; x<bitMatrixWidth; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[offset + x] = Color.parseColor("#000000");
                }
                else {
                    pixels[offset + x] = Color.parseColor("#ffffff");
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

}
