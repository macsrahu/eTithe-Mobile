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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prgs.etithe.R;
import com.prgs.etithe.activities.DependentsList;
import com.prgs.etithe.activities.DonorEntry;
import com.prgs.etithe.activities.ReceiptEntry;
import com.prgs.etithe.activities.ReceiptsList;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.Messages;
import com.prgs.etithe.utilities.RoundedCornersTransformation;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.viethoa.RecyclerViewFastScroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.MyViewHolder>{
    private Context mContext;
    private List<Donor> donorList;
    private ArrayList<Donor> searchList;
    CustomPowerMenu customPowerMenu;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDonor, tvContact, tvAddress, tvCode, tvEmail, tvType;
        public CircleImageView imgPicture;
        public LinearLayout layDefault;
        public CardView cardView;
        public ImageButton menu_edit;


        public MyViewHolder(View view) {
            super(view);
            layDefault = (LinearLayout) view.findViewById(R.id.layDefaultMore);
            tvDonor = (TextView) view.findViewById(R.id.tvDonor);
            tvEmail = (TextView) view.findViewById(R.id.tvEmail);
            tvType = (TextView) view.findViewById(R.id.tvType);
            tvContact = (TextView) view.findViewById(R.id.tvContact);
            tvAddress = (TextView) view.findViewById(R.id.tvAddress);
            cardView = (CardView) view.findViewById(R.id.card_viewMore);
            imgPicture = (CircleImageView) view.findViewById(R.id.imgPicture);
            menu_edit = (ImageButton) view.findViewById(R.id.menu_edit);
        }


    }

    @Override
    public DonorAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.donors_row, parent, false);

        return new DonorAdapter.MyViewHolder(itemView);
    }

    public DonorAdapter(Context mContext, List<Donor> _donorList) {
        this.mContext = mContext;
        this.donorList = _donorList;
//        this.searchList = new ArrayList<Donor>();
//        this.searchList.addAll(_donorList);
    }
    public void filterList(ArrayList<Donor> filterllist) {
        // below line is to add our filtered
        // list in our course array list.
        this.donorList = filterllist;
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged();
    }

//    @Override
//    public String getTextToShowInBubble(int pos) {
//        if (pos < 0 || pos >= donorList.size())
//            return null;
//
//        String name = donorList.get(pos).getDonor().trim();
//        if (name == null || name.length() < 1)
//            return null;
//
//        return donorList.get(pos).getDonor().trim().substring(0, 1);
//    }

    @Override
    public void onBindViewHolder(final DonorAdapter.MyViewHolder holder, final int position) {
        final Donor donor = donorList.get(position);
        if (donor.getDonor() != null) {
            holder.tvDonor.setText(donor.getDonor().trim());
        }
        String sAddress = "";
        if (donor.getAddrline1() != null) {
            sAddress = donor.getAddrline1() + "\n";
        }
        if (donor.getAddrline2() != null) {
            sAddress = sAddress + (!TextUtils.isEmpty(sAddress) ? donor.getAddrline2() + "\n" : "");
        }
        if (donor.getCity() != null) {
            sAddress = sAddress + (!TextUtils.isEmpty(sAddress) ? donor.getCity() : "");
        }
        if (donor.getPincode() != null) {
            sAddress = sAddress + "-" + (!TextUtils.isEmpty(sAddress) ? donor.getPincode() : "");
        }

        holder.tvAddress.setText(sAddress);
        if (donor.getMembertype() != null) {
            holder.tvType.setText(donor.getMembertype().equals("NONMEMBER") ? "Non-Member" : "Member");
            if (donor.getMembertype().equals("NONMEMBER")) {
                holder.tvType.setTextColor(mContext.getResources().getColor(R.color.md_red_400));
            }
        }

        if (donor.getMobile() != null && !donor.getMobile().isEmpty()) {
            holder.tvContact.setText("☎: " + donor.getMobile());
        } else {
            holder.tvContact.setText("☎: NA");
        }
        if (donor.getEmail() != null && !donor.getEmail().isEmpty()) {
            holder.tvEmail.setText("✉ : " + donor.getEmail());
        } else {
            holder.tvEmail.setText("✉ : NA");
        }

//        holder.cardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Global.SELECTED_DONOR_MODEL = donor;
//                if (Global.SELECTED_DONOR_MODEL != null) {
//                    Intent iDeliveryEntry = new Intent(mContext, DonorEntry.class);
//                    iDeliveryEntry.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mContext.startActivity(iDeliveryEntry);
//
//                }
//            }
//        });
        customPowerMenu = new CustomPowerMenu.Builder<>(mContext, new IconMenuAdapter())
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.drawable.save), "Update Profile"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_left), "My Receipts"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_left), "Dependent"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_left), "New Receipt"))
                .setOnMenuItemClickListener(onIconMenuItemClickListener)
                .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .build();
        holder.menu_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.SELECTED_DONOR_MODEL = donor;
                customPowerMenu.showAsAnchorRightBottom(v);
            }
        });

        if (donor.getImgurl() != null && !donor.getImgurl().isEmpty() && !donor.getImgurl().equals("NA")) {
            String mImageUrl = "";
            if (!TextUtils.isEmpty(donor.getImgurl())) {
                mImageUrl = donor.getImgurl();
            }

            final int radius = 5;
            final int margin = 5;
            final Transformation transformation = new RoundedCornersTransformation(radius, margin);
            Picasso.with(mContext).load(mImageUrl).placeholder(R.drawable.profile).transform(transformation).networkPolicy(NetworkPolicy.OFFLINE).into(holder.imgPicture, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    String sImageUri = donor.getImgurl();
                    Picasso.with(mContext).load(sImageUri).placeholder(R.drawable.profile).transform(transformation).into(holder.imgPicture);
                }
            });
        }

    }

    private OnMenuItemClickListener<IconPowerMenuItem> onIconMenuItemClickListener = new OnMenuItemClickListener<IconPowerMenuItem>() {
        @Override
        public void onItemClick(int position, IconPowerMenuItem item) {
            customPowerMenu.dismiss();
            if (item.getTitle() == "Update Profile") {
                if (Global.SELECTED_DONOR_MODEL != null) {
                    Global.DONOR_KEY = Global.SELECTED_DONOR_MODEL.getKey();
                    Intent iDeliveryEntry = new Intent(mContext, DonorEntry.class);
                    iDeliveryEntry.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(iDeliveryEntry);
                }
            } else if (item.getTitle().equals("Dependent")) {
                if (Global.SELECTED_DONOR_MODEL != null) {
                    Global.DONOR_KEY = Global.SELECTED_DONOR_MODEL.getKey();
                    Intent iDep = new Intent(mContext, DependentsList.class);
                    iDep.putExtra("FROM", "DONOR_LIST");
                    iDep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(iDep);
                }
            } else if (item.getTitle().equals("New Receipt")) {
                if (Global.SELECTED_DONOR_MODEL != null) {
                    Global.SELECTED_RECEIPT=null;
                    Global.SELECTED_RECEIPTS_LIST=null;
                    Global.DONOR_KEY = Global.SELECTED_DONOR_MODEL.getKey();
                    Intent iDep = new Intent(mContext, ReceiptEntry.class);
                    iDep.putExtra("FROM", "DONOR_LIST");
                    iDep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(iDep);
                }
            } else if (item.getTitle().equals("My Receipts")) {
                if (Global.SELECTED_DONOR_MODEL != null) {
                    Global.DONOR_KEY = Global.SELECTED_DONOR_MODEL.getKey();
                    Intent iDep = new Intent(mContext, ReceiptsList.class);
                    iDep.putExtra("FROM", "DONOR_LIST");
                    iDep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(iDep);
                }
            }
        }
    };

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return donorList.size();
    }

    public void filter(String charText) {
        if (charText != null) {
            charText = charText.toLowerCase(Locale.getDefault());
            Toast.makeText(mContext, charText, Toast.LENGTH_LONG).show();
            donorList.clear();
            if (charText.length() == 0) {
                donorList.addAll(searchList);
            } else {
                for (Donor s : searchList) {
                    if (!TextUtils.isEmpty(s.getDonor())) {
                        if (s.getDonor().toLowerCase(Locale.getDefault()).contains(charText)) {
                            donorList.add(s);
                        }
                    }
                }
            }
            notifyDataSetChanged();
        }
    }
}


