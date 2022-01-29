package com.prgs.etithe.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.prgs.etithe.R;
import com.prgs.etithe.activities.ReceiptEntry;
import com.prgs.etithe.models.ReceiptLine;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.InternalStorage;
import com.prgs.etithe.utilities.Messages;

import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.MyViewHolder> {
    private Context mContext;
    private List<ReceiptLine> paymenList;
    private Activity mActivity;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView text_view_fund, text_view_amount, text_view_bank;
        public LinearLayout layReceiptContent;

        public MyViewHolder(View view) {
            super(view);
            text_view_fund = (TextView) view.findViewById(R.id.text_view_fund);
            text_view_amount = (TextView) view.findViewById(R.id.text_view_amount);
            text_view_bank = (TextView) view.findViewById(R.id.text_view_bank);
            layReceiptContent = (LinearLayout) view.findViewById(R.id.layReceiptContent);

        }

    }

    @Override
    public PaymentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.receipt_entry_row, parent, false);

        return new PaymentAdapter.MyViewHolder(itemView);
    }

    public PaymentAdapter(Context mContext, Activity acctivity, List<ReceiptLine> _receiptLineList) {
        this.mContext = mContext;
        this.paymenList = _receiptLineList;
        this.mActivity = acctivity;

    }


    @Override
    public void onBindViewHolder(final PaymentAdapter.MyViewHolder holder, final int position) {
        final ReceiptLine receiptLine = paymenList.get(position);
        if (receiptLine.getFundtype() != null) {
            holder.text_view_fund.setText(receiptLine.getFundtype().trim());
        }
        if (receiptLine.getPaymode() != null) {
            holder.text_view_bank.setText("Pay by :" + receiptLine.getPaymode());
        }

        if (receiptLine.getAmount() != null) {
            holder.text_view_amount.setText(Global.GetFormatedAmountWithCurrency(String.valueOf(receiptLine.getAmount())));
        }
        if (receiptLine.getBankname() != null) {
            if (!receiptLine.getBankname().isEmpty()) {
                holder.text_view_bank.setText("Pay by :" + receiptLine.getPaymode() + " Bank: " + receiptLine.getBankname() + " - Cheque No:" + receiptLine.getChequeno());
            }
        }
        holder.layReceiptContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.app_name);
                builder.setMessage(mContext.getResources().getString(R.string.dialog_delete_fund));
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                paymenList.remove(position);
                                notifyDataSetChanged();
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
                return false;
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return paymenList.size();
    }


}
