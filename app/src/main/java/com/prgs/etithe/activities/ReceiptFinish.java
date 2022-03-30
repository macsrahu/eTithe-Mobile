package com.prgs.etithe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.prgs.etithe.R;
import com.prgs.etithe.utilities.Global;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReceiptFinish extends AppCompatActivity {

    @BindView(R.id.button_done)
    MaterialButton button_done;

    @BindView(R.id.button_print)
    MaterialButton button_print;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt_finished);
        ButterKnife.bind(this);

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "Receipt Created");
        setSupportActionBar(mToolbarView);

        button_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReceiptFinish.this, DonorsList.class);
                startActivity(intent);
                finish();
            }
        });
        button_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReceiptFinish.this, WebViewPDF.class);
                startActivity(intent);
                finish();
            }
        });
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
