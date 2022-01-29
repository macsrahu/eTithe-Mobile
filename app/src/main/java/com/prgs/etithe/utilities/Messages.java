package com.prgs.etithe.utilities;

import android.content.Context;
import android.widget.Toast;

public class Messages {
    public static void ShowToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
