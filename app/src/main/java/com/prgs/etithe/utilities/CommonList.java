package com.prgs.etithe.utilities;

import com.prgs.etithe.models.Salutations;

import java.util.ArrayList;

//import crl.android.pdfwriter.Array;

public class CommonList {

    public static ArrayList<String> GetStateList() {
        ArrayList<String> stateList = new ArrayList<String>();
        stateList.add("Andaman Nicobar");
        stateList.add("Andhra Pradesh");
        stateList.add("Arunachal Pradesh");
        stateList.add("Assam");

        stateList.add("Bihar");
        stateList.add("Chandigarh");
        stateList.add("Dadra Nagar Haveli");
        stateList.add("Daman Diu");
        stateList.add("Delhi");
        stateList.add("Goa");
        stateList.add("Gujarat");
        stateList.add("Himachal Pradesh");
        stateList.add("Jammu Kashmir");
        stateList.add("Jharkhand");
        stateList.add("Karnataka");
        stateList.add("Kerala");
        stateList.add("Jharkhand");
        stateList.add("Ladakh");
        stateList.add("Lakshadweep");
        stateList.add("Madhya Pradesh");
        stateList.add("Maharashtra");
        stateList.add("Manipur");
        stateList.add("Meghalaya");
        stateList.add("Mizoram");
        stateList.add("Nagaland");
        stateList.add("Odisha");
        stateList.add("Puducherry");
        stateList.add("Punjab");
        stateList.add("Rajasthan");
        stateList.add("Sikkim");
        stateList.add("Tamil Nadu");
        stateList.add("Telangana");
        stateList.add("Tripura");
        stateList.add("Uttarakhand");
        stateList.add("West Bengal");
        return stateList;
    }
    public static ArrayList<String> GetSalutationList() {
        ArrayList<String> salutationList = new ArrayList<String>();
//        if (Global.SALUTATIONS!=null && Global.SALUTATIONS.size()>0){
//            for(Salutations sal: Global.SALUTATIONS){
//                salutationList.add(sal.getSalutation());
//            };
//        }else {
//            salutationList.add("Mr.");
//            salutationList.add("Mrs.");
//            salutationList.add("Ms.");
//            salutationList.add("Sir.");
//            salutationList.add("Rev.");
//            salutationList.add("Dr.");
//            salutationList.add("Er.");
//        }
        salutationList.add("Mr.");
        salutationList.add("Mrs.");
        salutationList.add("Ms.");
        salutationList.add("Sir.");
        salutationList.add("Rev.");
        salutationList.add("Dr.");
        salutationList.add("Er.");
        salutationList.add("Ps.");
        return salutationList;
    }

}
