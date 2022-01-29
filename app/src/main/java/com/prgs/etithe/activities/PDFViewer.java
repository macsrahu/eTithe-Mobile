package com.prgs.etithe.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import com.prgs.etithe.R;
import com.prgs.etithe.utilities.Global;

/*import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;*/

public class PDFViewer extends AppCompatActivity /*implements OnPageChangeListener, OnLoadCompleteListener */{
    private static final String TAG = MainActivity.class.getSimpleName();

    //PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_viewer);

        // pdfView = (PDFView) findViewById(R.id.pdfView);
        Toast.makeText(getApplicationContext(), Global.PDF_FILE, Toast.LENGTH_LONG).show();

        displayFromAsset(Global.PDF_FILE);
    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

//       /* pdfView.fromAsset(Global.PDF_FILE)
//                .defaultPage(pageNumber)
//                .enableSwipe(true)
//
//                .swipeHorizontal(false)
//                .onPageChange(this)
//                .enableAnnotationRendering(true)
//                .onLoad(this)
//                .scrollHandle(new DefaultScrollHandle(this))
//                .load();*/
    }

   /* @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }


    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }
*/
}