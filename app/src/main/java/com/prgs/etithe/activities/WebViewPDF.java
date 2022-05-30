package com.prgs.etithe.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Region;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.internal.GmsLogger;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.prgs.etithe.R;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.models.ReceiptLine;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.Messages;
import com.prgs.etithe.utilities.NumberToWord;

import android.print.PrintPDF;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class WebViewPDF extends AppCompatActivity {
    WebView printWeb;
    Button savePdfBtn;
    PrintJob printJob;
    boolean printBtnPressed;
    String _FOLDER_PATH = "eTithe/Pictures";
    String mOutputFilePath;
    String sBranchAddress;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "Receipt View");
        setSupportActionBar(mToolbarView);
        CheckFolderPermission();

        final WebView webView = (WebView) findViewById(R.id.webViewMain);
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
        // Initializing the Button
        savePdfBtn = (Button) findViewById(R.id.savePdfBtn);
        savePdfBtn.setVisibility(View.GONE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                printWeb = webView;
            }
        });
        LoadRegion();
        LoadMenu(webView);
        LoadDonor(webView);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void LoadRegion() {
        if (Global.LOGIN_USER_DETAIL != null && Global.REGION_MODEL==null) {
            Global.GetRegionNameByRegionKey(Global.LOGIN_USER_DETAIL.getRegionkey());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void LoadMenu(final WebView webView) {
        BottomNavigationView bottonNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottonNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.btnConvert:
                    if (printWeb != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            PrintTheWebPage(printWeb);
                        } else {
                            Toast.makeText(WebViewPDF.this, "Not available for device below Android LOLLIPOP",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(WebViewPDF.this, "WebPage not fully loaded", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnShare:
                    SharePDFFile(webView);
                    break;
                case R.id.btnBack:
                    onBackPressed();
                    break;
            }
            return true;
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void SharePDFFile(final WebView webView) {

        try {
            printBtnPressed = true;

            PrintAttributes attributes = new PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build();
            String jobName = Global.SELECTED_RECEIPT.getReceiptno();
            //File path = getCacheDir().getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/");

            File pdfDirectory = new File(getCacheDir(), _FOLDER_PATH);
            mOutputFilePath = "//RECEIPT_" + Global.SELECTED_RECEIPT.getReceiptno() + ".pdf";
            PrintPDF pdfPrint = new PrintPDF(attributes);
            pdfPrint.print(webView.createPrintDocumentAdapter(jobName), pdfDirectory, "RECIEPT_" + Global.SELECTED_RECEIPT.getReceiptno() + ".pdf");
            //s.ShowToast(getApplicationContext(), "First:" + mOutputFilePath);
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            if (pdfDirectory.exists()) {
                File mFile = new File(getBaseContext().getExternalCacheDir().getAbsolutePath() + mOutputFilePath);
                //.ShowToast(getApplicationContext(), "Second:" + mOutputFilePath);
                intentShareFile.setType("application/pdf");
                intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mFile));
                intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                        "Sharing File from eTithe");
                intentShareFile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intentShareFile, "Share via"));
            }
        } catch (Exception ex) {
            Messages.ShowToast(getApplicationContext(), ex.getMessage());
        }
    }

    private void LoadDonor(final WebView webView) {

        if (Global.SELECTED_RECEIPT != null) {
            sBranchAddress = Global.GetRegionAddress(Global.REGION_MODEL);

            final ProgressDialog dialog = new ProgressDialog(WebViewPDF.this, R.style.MyAlertDialogStyle);
            dialog.setMessage("Loading receipt..");
            dialog.show();
            FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DONORS).orderByKey()
                    .equalTo(Global.SELECTED_RECEIPT.getDonorkey())
                    .addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                            dialog.dismiss();
                            if (dataSnapshot.exists()) {

                                for (DataSnapshot donorSnapshot : dataSnapshot.getChildren()) {
                                    Donor mDonor = donorSnapshot.getValue(Donor.class);
                                    if (mDonor != null) {
                                        LoadReportView(webView, mDonor);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void LoadReportView(WebView webView, Donor mDonor) {

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        StringBuilder sbReceiptLine = new StringBuilder();


        if (Global.SELECTED_RECEIPTS_LIST != null && Global.SELECTED_RECEIPTS_LIST.size() > 0) {

            String imgUrl = "https://firebasestorage.googleapis.com/v0/b/etithe.appspot.com/o/Logo%2Flogo.png?alt=media&token=3ce66a8e-b023-47cb-9137-e2b4074f77f3";
            String imgSign = Global.SELECTED_RECEIPT.getSignurl();


            for (ReceiptLine receiptLine : Global.SELECTED_RECEIPTS_LIST) {
                sbReceiptLine.append("<tr>\n");
                sbReceiptLine.append("<td>" + receiptLine.getFundtype() + "</td>\n");
                sbReceiptLine.append("<td style='text-align:right;margin-right:5px;'>" + Global.GetFormatedAmount(String.valueOf(receiptLine.getAmount())) + "</td>\n");
                sbReceiptLine.append("</tr>\n");

            }
            sbReceiptLine.append("<td style='text-align:right'><span style='font-weight:bold'>Total&nbsp;&nbsp;</span></td>\n");
            sbReceiptLine.append("<td style='text-align:right;margin-right:5px;'><span style='font-weight:bold'>" + Global.GetFormatedAmount(String.valueOf(Global.SELECTED_RECEIPT.getAmount())) + "</span></td>\n");
            sbReceiptLine.append("</tr>\n");


            //Payment Mode

            sbReceiptLine.append("<tr><td colspan=\"2\"><table style=\"font-family: Cambria;font-size:12px;\">\n");
            if (Global.SELECTED_RECEIPTS_LIST.size() > 0) {
                ReceiptLine mReceiptLine = Global.SELECTED_RECEIPTS_LIST.get(0);
                if (mReceiptLine != null) {
                    //Messages.ShowToast(getApplicationContext(),mReceiptLine.getPaymode());
                    if (mReceiptLine.getPaymode()=="CASH") {
                        sbReceiptLine.append("<tr><td>Received as :" + mReceiptLine.getPaymode() + "</td></tr>");
                    }else if(mReceiptLine.getPaymode()=="NEFT"){
                        sbReceiptLine.append("<tr><td style='width:50%'>Received as:NEFT</td><td style='width:50%'></td><tr><td style='width:50%'>Ref. No: " + mReceiptLine.getChequeno() + "</td></tr>");
                    } else {
                        sbReceiptLine.append("<tr><td style='width:50%'>Received as:Cheque</td><td style='width:50%'>Bank Name:" + mReceiptLine.getBankname() + "</td><tr><td style='width:50%'>Cheque/DD No: " + mReceiptLine.getChequeno() + "</td><td style='width:50%'>Cheque/DD Date: " + mReceiptLine.getChequedate() + "</td></tr>");
                    }
                }
            }
            sbReceiptLine.append("</table></td></tr>\n");

            sbReceiptLine.append("<tr><td colspan=\"2\">\n");
            sbReceiptLine.append("<table  style=\"font-family: Cambria;font-size:12px;\" ><tr><td valign=\"top\"><span style=\"float:left;margin-left:2px;\">Rupees " + NumberToWord.convert((long) (Global.SELECTED_RECEIPT.getAmount())));
            sbReceiptLine.append("</td></tr></table>\n");
            sbReceiptLine.append("</td></tr>\n");


            sbReceiptLine.append("<tr>\n");
            sbReceiptLine.append("<table  style=\"width: 100%;padding: 10px; font-family: Cambria;font-size:12px;\" cellspacing=\"5\" >\n");
            sbReceiptLine.append("<tr>\n");
            sbReceiptLine.append("<td style=\"width:35%\" >&nbsp;&nbsp;</td>\n");
            //sbReceiptLine.append("<table><tr><td valign=\"top\"><span style=\"float:left;\">Rupees " + NumberToWord.convert((long)(Global.SELECTED_RECEIPT.getAmount())));
            //sbReceiptLine.append("</td></tr></table></td>\n");
            sbReceiptLine.append("<td style=\"width:65%;text-align:right;float:right\">");
            sbReceiptLine.append("<table  style=\"font-family: Cambria;font-size:12px;font-weight:bold;\"><tr><td><span >for Scripture Union & CSSM council of India</span></td></tr>\n");
            sbReceiptLine.append("<tr><td style=\"text-align:center;\"><img style=\"text-align:center\" src='" + imgSign + "'  width=\"70\" height=\"50\"></img></td></tr>\n");
            sbReceiptLine.append("<tr><td style=\"text-align:center;\"><span style=\"text-align:center;\" >Authorised Signatory</span></td></tr></table>");
            sbReceiptLine.append("<tr>\n");
            sbReceiptLine.append("<td style=\"width:100%\" colspan=\"2\" >&nbsp;</td>\n");
            sbReceiptLine.append("</tr>\n");
            sbReceiptLine.append("<tr>\n");

            sbReceiptLine.append("<td style=\"width:100%\" colspan=\"2\" >\n");

            sbReceiptLine.append("<table style=\"width:100%;text-align:center;font-family: Cambria;font-size:10px;font-weight:bold;\"><tr><td style\"text-align:center\">");
            sbReceiptLine.append("<span>\"Your word is lamp to my feet and a light for my path. Psalms 119:105\"</span>");
            sbReceiptLine.append("</td></tr></table>\n");


            sbReceiptLine.append("</td>\n");
            sbReceiptLine.append("</tr>\n");

            sbReceiptLine.append("<tr>\n");
            sbReceiptLine.append("<td style=\"width:100%\" colspan=\"2\" >\n");

            sbReceiptLine.append("<table style=\"width:100%;text-align:center;font-family: Cambria;font-size:10px;font-weight:bold;\"><tr><td style\"text-align:center\">");
            sbReceiptLine.append("<tr><td style=\"text-align: center;font-size: 14px;\">\n" +
                    "                                                    <span>\n" +
                    "                                                        Head Office: No. 27 First Main Road, United India Nagar, Ayanavaram, Chennai-600023\n" +
                    "                                                    </span>\n" +
                    "                                                </td></tr>\n" +
                    "                                                <tr><td style=\"text-align: center;font-size: 10px;\">\n" +
                    "                                                    <span>\n" +
                    "                                                        Phone:044-2674 0137 Email:scriptureunionindia@gmail.com\n" +
                    "                                                    </span>\n" +
                    "                                                </td></tr>\n");
            sbReceiptLine.append("</td></tr></table>\n");

            sbReceiptLine.append("</td>\n");
            sbReceiptLine.append("</tr>\n");


            sbReceiptLine.append("</table>\n");
            sbReceiptLine.append("</tr>\n");

            String  sReceiptTitle = Global.SELECTED_RECEIPT.getCancel()==0? "RECEIPT" : "CANCELLED RECEIPT";
            StringBuilder receiptHTML = new StringBuilder();
            receiptHTML.append("<html>\n" +
                    "<tbody>\n" +
                    "    <div style=\"border: 1px solid black;width: 100%;border-collapse:collapse;font-family: Cambria;\">\n" +
                    "        <table style=\"width: 100%; border: 0px solid gray;width: 100%;border-collapse:collapse;\">\n" +
                    "            <tr>\n" +
                    "                <td style=\"text-align:center;\">\n" +
                    "                     <table style=\"width:90%;text-align:center;\">\n" +
                    "                         <tr>\n" +
                    "                             <td style=\"width: 20%;text-align:right;\">\n" +
                    "                                 <img src='" + imgUrl + "'  width=\"90\" height=\"90\"></img> \n" +
                    "                             </td>\n" +
                    "                             <td style=\"text-align: center;width: 80%;\">\n" +
                    "                                 <table>\n" +
                    "                                     <tr>\n" +
                    "                                         <td  style=\"text-align:center;\">\n" +
                    "                                            <table>\n" +
                    "                                                <tr>\n" +
                    "                                                    <td style=\"text-align: center;font-size: 18px;font-weight: bold;\">\n" +
                    "                                                        SCRIPTURE UNION & CSSM COUNCIL OF INDIA\n" +
                    "                                                    </td>\n" +
                    "                                                </tr>\n" +
                    "                                                <tr>\n" +
                    "                                                    <td style=\"text-align: center;font-size: 12px;\">\n" +
                    "                                                        <span>Society Registration Number : 1/1975</span>\n" +
                    "                                                    </td>\n" +
                    "                                                </tr>\n" +
                    "                                               <tr>\n" +
                    "                                                   <td style=\"width:100%;text-align:center;font-weight:bold;font-size:12px;\">\n" +
                    "                                                           <span>" + sBranchAddress +"</span>\n" +
                    "                                                   </td>\n" +
                    "                                              </tr>\n" +

//                    "                                                <tr><td style=\"text-align: center;font-size: 12px;\">\n" +
//                    "                                                    <span>\n" +
//                    "                                                        Head Office: No. 27 First Main Road, United India Nagar, Ayanavaram, Chennai-600023\n" +
//                    "                                                    </span>\n" +
//                    "                                                </td></tr>\n" +
//                    "                                                <tr><td style=\"text-align: center;font-size: 10px;\">\n" +
//                    "                                                    <span>\n" +
//                    "                                                        Phone:044-2674 0137 Email:scriptureunionindia@gmail.com\n" +
//                    "                                                    </span>\n" +
//                    "                                                </td></tr>\n" +

                    "                                            </table> \n" +
                    "                                        </td>\n" +
                    "                                     </tr>\n" +
                    "                                 </table>\n" +
                    "                             </td>\n" +
                    "                         </tr>\n" +
                    "                     </table>\n" +
                    "                </td>\n" +
                    "            </tr>\n" +
                    "           <tr>\n" +
                    "                <td colspan='2'>\n" +
                    "                     <hr/>\n" +
                    "                </td>\n" +
                    "           </tr>\n" +
                    "           <tr>\n" +
                    "           <td colspan='2' style=\"width:100%;text-align:center;\">\n" +
                    "                      <span style='font-size: 15px;text-align:center;font-weight:bold'>" + sReceiptTitle + "</span>\n" +
                    "                </td>\n" +
                    "            </tr>\n" +
                    "            <tr>\n" +
                    "                <td>\n" +
                    "                <hr>\n" +
                    "                </td>\n" +
                    "            </tr>\n" +
                    "            <tr>\n" +
                    "                <td>\n" +
                    "                    <table  style=\"width: 100%;margin-top: 5px;  padding: 10px;font-family: Cambria;font-size:12px\" cellspacing=\"5\" >\n" +
                    "                        <tr>\n" +
                    "                            <td colspan='2'>\n" +
                    "                                <span>Received with thanks from &nbsp;&nbsp;</span><span><b>" + Global.SELECTED_RECEIPT.getDonor().toUpperCase() + "</b></span>\n" +
                    "                            </td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <td>\n" +
                    "                                   &nbsp;" +
                    "                            </td>\n" +
                    "                         <td style=\"width:35%;text-align:left;font-size:14px\"><span>Receipt No:&nbsp;" + Global.SELECTED_RECEIPT.getReceiptno() + "</span></td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <td>\n" +
                    "                             <span>Address</span><br><span style=\"font-weight:bold\" >" + Global.SELECTED_RECEIPT.getAddress() + "</span>\n" +
                    "                            </td>\n" +
                    "                            <td style=\"text-align:left\" ><span>Date:&nbsp;" + Global.SELECTED_RECEIPT.getReceiptdate() + "</span></td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <td>\n" +
                    "                                 <span>Mobile No:&nbsp;</span><span>" + mDonor.getMobile() + "</span>\n" +
                    "                            </td>\n" +
                    "                            <td>\n" +
                    "                            </td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <td>\n" +
                    "                                 <span>Email:&nbsp;" + mDonor.getEmail() + "</span>\n" +
                    "                            </td>\n" +
                    "                            <td>\n" +
                    "                            </td>\n" +
                    "                        </tr>\n" +
                    "                    </table>\n" +
                    "                </td>\n" +
                    "            </tr>\n" +
                    "            <tr>\n" +
                    "                <td style=\"text-align:center\">\n" +
                    "                    <table border=\"1\" style=\"width: 98%;margin:5px; font-family: Cambria;font-size:12px; padding: 10px;border-collapse:collapse;\" cellspacing=\"5\" >\n" +
                    "                        <tr>\n" +
                    "                            <th style=\"width:70%\">\n" +
                    "                                Particulars\n" +
                    "                            </th>\n" +
                    "                            <th>Amount</td>\n" +
                    "                        </tr>\n" + sbReceiptLine.toString() +
                    "                    </table>\n" +
                    "                </td>\n" +
                    "            </tr>\n" +
                    "        </table>\n" +
                    "    </div>\n" +
                    "</tbody>\n" +
                    "</html>");

            webView.loadDataWithBaseURL(null, receiptHTML.toString(), "text/html", "UTF-8", null);
            savePdfBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (printWeb != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            PrintTheWebPage(printWeb);
                        } else {
                            Toast.makeText(WebViewPDF.this, "Not available for device below Android LOLLIPOP",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(WebViewPDF.this, "WebPage not fully loaded", Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void PrintTheWebPage(WebView webView) {
        // set printBtnPressed true

        printBtnPressed = true;
        // Creating  PrintManager instance
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        // setting the name of job
        String jobName = Global.SELECTED_RECEIPT.getReceiptno();  //getString(R.string.app_name) + "webpage" + webView.getUrl();
        // Creating  PrintDocumentAdapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
        // Create a print job with name and adapter instance
        assert printManager != null;
        printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
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

                            new MaterialDialog.Builder(WebViewPDF.this)
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

    @Override
    protected void onResume() {
        super.onResume();
        if (printJob != null && printBtnPressed) {
            if (printJob.isCompleted()) {
                // Showing Toast Message
                Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();
            } else if (printJob.isStarted()) {
                // Showing Toast Message
                Toast.makeText(this, "isStarted", Toast.LENGTH_SHORT).show();

            } else if (printJob.isBlocked()) {
                // Showing Toast Message
                Toast.makeText(this, "isBlocked", Toast.LENGTH_SHORT).show();

            } else if (printJob.isCancelled()) {
                // Showing Toast Message
                Toast.makeText(this, "isCancelled", Toast.LENGTH_SHORT).show();

            } else if (printJob.isFailed()) {
                // Showing Toast Message
                Toast.makeText(this, "isFailed", Toast.LENGTH_SHORT).show();

            } else if (printJob.isQueued()) {
                // Showing Toast Message
                Toast.makeText(this, "isQueued", Toast.LENGTH_SHORT).show();

            }
            // set printBtnPressed false
            printBtnPressed = false;
        }
    }
}
