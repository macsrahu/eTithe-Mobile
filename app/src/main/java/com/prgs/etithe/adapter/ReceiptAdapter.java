package com.prgs.etithe.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.prgs.etithe.R;
import com.prgs.etithe.activities.ReceiptView;
import com.prgs.etithe.models.Receipt;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.Messages;
import com.skydoves.powermenu.CustomPowerMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.MyViewHolder> {
    private Context mContext;
    public List<Receipt> receiptList;
    private ArrayList<Receipt> searchList;
    CustomPowerMenu customPowerMenu;
    public String _NavigateFrom = "";

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvDonor, tvAmount, tvAddress, tvMode, tvMonth, tvReceiptNo, tvReceiptDate;
        public CircleImageView imgPicture;
        public LinearLayout layDefault,layTitle;
        public CardView cardView;
        public ImageButton menu_edit;
        public View lineView;


        public MyViewHolder(View view) {
            super(view);
            layTitle = (LinearLayout)view.findViewById(R.id.layTitle);
            layDefault = (LinearLayout) view.findViewById(R.id.layDefaultMore);
            tvDonor = (TextView) view.findViewById(R.id.tvDonor);
            tvReceiptDate = (TextView) view.findViewById(R.id.tvReceiptDate);
            lineView =(View)view.findViewById(R.id.lineView);
            tvReceiptNo = (TextView) view.findViewById(R.id.tvReceiptNo);
            tvAmount = (TextView) view.findViewById(R.id.tvAmount);
            tvMonth = (TextView) view.findViewById(R.id.tvMonth);
            tvMode = (TextView) view.findViewById(R.id.tvMode);
            tvMode.setVisibility(View.GONE);
            tvAddress = (TextView) view.findViewById(R.id.tvAddress);
            cardView = (CardView) view.findViewById(R.id.card_viewMore);
            //menu_edit = (ImageButton) view.findViewById(R.id.menu_edit);
        }

        @Override
        public void onClick(View v) {
            Messages.ShowToast(mContext, "Clicked");
            Receipt receipt = receiptList.get(getAdapterPosition());
            if (receipt != null) {
                Global.SELECTED_RECEIPT = receipt;
                Intent iOrderItem = new Intent(mContext, ReceiptView.class);
                iOrderItem.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(iOrderItem);

            }
        }

    }

    @Override
    public ReceiptAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.receipts_row, parent, false);

        return new ReceiptAdapter.MyViewHolder(itemView);
    }

    public ReceiptAdapter(Context mContext, List<Receipt> _receiptList, String _navigateFrom) {
        this.mContext = mContext;
        this.receiptList = _receiptList;
        this.searchList = new ArrayList<Receipt>();
        this.searchList.addAll(_receiptList);
        this._NavigateFrom = _navigateFrom;
    }

    @Override
    public void onBindViewHolder(final ReceiptAdapter.MyViewHolder holder, final int position) {
        final Receipt receipt = receiptList.get(position);
        if (receipt.getDonor() != null) {
            holder.tvDonor.setText(receipt.getDonor().trim().toUpperCase());
        }
        if (receipt.getCancel()==1){
            holder.tvMode.setText("CANCELLED");
            holder.tvMode.setVisibility(View.VISIBLE);
            //holder.tvDonor.setTextColor(mContext.getColor(R.color.colorRed));
            holder.lineView.setBackground(mContext.getResources().getDrawable(R.color.colorRed));
            holder.layTitle.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_text_cancel_view));
        }else{
            holder.tvMode.setText("");

            holder.tvMode.setVisibility(View.GONE);
            holder.layTitle.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_text_view));
        }
        holder.tvReceiptDate.setText(receipt.getReceiptdate());
        holder.tvReceiptNo.setText(receipt.getReceiptno());
        holder.tvAddress.setText(receipt.getAddress());
        holder.tvMonth.setText(receipt.getPaymonth());
        holder.tvAmount.setText(Global.GetFormatedAmountWithCurrency(String.valueOf(receipt.getAmount())));

        holder.tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receipt != null) {
                    Global.SELECTED_RECEIPT = receipt;
                    Intent iReceiptView = new Intent(mContext, ReceiptView.class);
                    iReceiptView.putExtra("FROM", _NavigateFrom);
                    iReceiptView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(iReceiptView);

                }
            }
        });
        holder.tvDonor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receipt != null) {
                    Global.SELECTED_RECEIPT = receipt;
                    Intent iReceiptView = new Intent(mContext, ReceiptView.class);
                    iReceiptView.putExtra("FROM", _NavigateFrom);
                    iReceiptView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(iReceiptView);

                }
            }
        });
        holder.tvReceiptNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receipt != null) {
                    Global.SELECTED_RECEIPT = receipt;
                    Intent iReceiptView = new Intent(mContext, ReceiptView.class);
                    iReceiptView.putExtra("FROM", _NavigateFrom);
                    iReceiptView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(iReceiptView);
                }
            }
        });
        holder.tvReceiptDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receipt != null) {
                    Global.SELECTED_RECEIPT = receipt;
                    Intent iReceiptView = new Intent(mContext, ReceiptView.class);
                    iReceiptView.putExtra("FROM", _NavigateFrom);
                    iReceiptView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(iReceiptView);
                }
            }
        });
//        customPowerMenu = new CustomPowerMenu.Builder<>(mContext, new IconMenuAdapter())
//                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.drawable.save), "Share"))
//                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_left), "Use Receipt"))
//                .setOnMenuItemClickListener(onIconMenuItemClickListener)
//                .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
//                .setMenuRadius(10f)
//                .setMenuShadow(10f)
//                .build();
//        holder.menu_edit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                customPowerMenu.showAsAnchorRightBottom(v);
//            }
//        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return receiptList.size();
    }

    public void filter(String charText) {
        if (charText != null) {
            charText = charText.toLowerCase(Locale.getDefault());
            Toast.makeText(mContext, charText, Toast.LENGTH_LONG).show();
            receiptList.clear();
            if (charText.length() == 0) {
                receiptList.addAll(searchList);
            } else {
                for (Receipt s : searchList) {
                    if (!TextUtils.isEmpty(s.getReceiptno())) {
                        if (s.getReceiptno().toLowerCase(Locale.getDefault()).contains(charText)) {
                            receiptList.add(s);
                        }
                    }
                }
            }
            notifyDataSetChanged();
        }
    }
}
