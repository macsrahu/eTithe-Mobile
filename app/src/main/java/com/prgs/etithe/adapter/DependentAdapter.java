package com.prgs.etithe.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.prgs.etithe.R;
import com.prgs.etithe.activities.DependentEntry;
import com.prgs.etithe.activities.DonorEntry;
import com.prgs.etithe.models.Dependent;
import com.prgs.etithe.models.Donor;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;
import com.prgs.etithe.utilities.InternalStorage;
import com.prgs.etithe.utilities.RoundedCornersTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.viethoa.RecyclerViewFastScroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.annotations.NonNull;


public class DependentAdapter extends RecyclerView.Adapter<DependentAdapter.MyViewHolder> {
    private Context mContext;
    private Activity mActivity;
    private List<Dependent> dependentsList;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvDependent, tvRelation, tvBrithDate, tvAge;
        public LinearLayout layDefault;
        public CardView cardView;
        public ImageButton btnRemove;


        public MyViewHolder(View view) {
            super(view);
            layDefault = (LinearLayout) view.findViewById(R.id.layDefaultMore);
            tvDependent = (TextView) view.findViewById(R.id.tvDependent);
            tvBrithDate = (TextView) view.findViewById(R.id.tvBirthDate);
            tvRelation = (TextView) view.findViewById(R.id.tvRelation);
            tvAge = (TextView) view.findViewById(R.id.tvAge);
            cardView = (CardView) view.findViewById(R.id.card_viewMore);
            btnRemove = (ImageButton) view.findViewById(R.id.btnRemove);
        }

        @Override
        public void onClick(View v) {
            final Dependent dependent = dependentsList.get(getAdapterPosition());
            notifyDataSetChanged();
        }


    }

    @Override
    public DependentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dependent_row, parent, false);

        return new DependentAdapter.MyViewHolder(itemView);
    }

    public DependentAdapter(Context mContext, Activity mActivity, List<Dependent> _dependentsList) {
        this.mContext = mContext;
        this.dependentsList = _dependentsList;
    }


    @Override
    public void onBindViewHolder(final DependentAdapter.MyViewHolder holder, final int position) {
        final Dependent dependent = dependentsList.get(position);
        if (dependent.getDependent() != null) {
            holder.tvDependent.setText(dependent.getDependent().trim());
        }
        holder.tvRelation.setText(Html.fromHtml("Relation to the donor is <b>" + dependent.getRelation().toUpperCase() + "</b>"));
//        String str = "<b>Hi! Innee.</b> <u><i>How are you?</i></u>";
        //need to import android.text.Html class
        //set text style bold, italic and underline from html tag
        //tv4.setText(Html.fromHtml(str));
        holder.tvBrithDate.setText(Html.fromHtml("Date of birth is <b>" + dependent.getDepbirthdate() + "</b> and his age now <b>" + String.valueOf(dependent.getAge()) + "</b>"));
        holder.tvAge.setText(String.valueOf(dependent.getAge()));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.SELECTED_DEPENDENT_MODEL = dependent;
                if (Global.SELECTED_DEPENDENT_MODEL != null) {
                    Intent IDepEntry = new Intent(v.getRootView().getContext(), DependentEntry.class);
                    IDepEntry.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getRootView().getContext().startActivity(IDepEntry);
                }
            }
        });
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
//                builder.setTitle(R.string.app_name);
//                builder.setIcon(v.getRootView().getContext().getResources().getDrawable(R.drawable.logo));
//                builder.setMessage(v.getRootView().getContext().getResources().getString(R.string.dialog_logout_message));
//                builder.setPositiveButton("Yes",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(final DialogInterface dialog,
//                                                final int which) {
//                                FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DEPENDENTS)
//                                        .child(Global.DONOR_KEY).child(dependent.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        Toast.makeText(mContext, "Removed successfully", Toast.LENGTH_LONG).show();
//                                        // mActivity.notifyAll();
//                                    }
//                                });
//                            }
//                        });
//                builder.setNegativeButton("No",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(final DialogInterface dialog,
//                                                final int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                final AlertDialog dialog = builder.create();
//                dialog.show();
                new MaterialDialog.Builder(v.getRootView().getContext())
                        .title(R.string.app_name)
                        .content(R.string.dialog_delete_message)
                        .positiveText(R.string.yes)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DEPENDENTS)
                                        .child(Global.DONOR_KEY).child(dependent.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(v.getRootView().getContext(), "Removed successfully", Toast.LENGTH_LONG).show();
                                        // mActivity.notifyAll();
                                    }
                                });
                            }
                        })
                        .negativeText(R.string.no)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });


    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return dependentsList.size();
    }

}
