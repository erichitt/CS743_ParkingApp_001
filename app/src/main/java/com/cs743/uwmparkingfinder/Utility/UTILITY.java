package com.cs743.uwmparkingfinder.Utility;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cs743.uwmparkingfinder.UI.R;

/**
 * Created by fbgrecojr on 11/21/15.
 */
public class UTILITY {

    public static final String UBUNTU_SERVER_URL ="http://ec2-54-152-4-103.compute-1.amazonaws.com/scripts.php";


    /**
     * Check to see whether there is an internet connection or not.
     * @return whether there is an internet connection
     */
    public static boolean isOnline(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Converts the lot name from the database to a user-friendly name
     *
     * Ex:  LOT_59001 --> Garland Hall
     *
     * @param lotNameDb Lot name from database
     *
     * @return Resource ID of user-friendly lot name
     */
    public static int convertDbLotNameToUINameID(String lotNameDb)
    {
        int uiNameID = 0;

        if (lotNameDb.equalsIgnoreCase("LOT_59001"))
        {
            uiNameID = R.string.LOT_59001;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59002"))
        {
            uiNameID = R.string.LOT_59002;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59006"))
        {
            uiNameID = R.string.LOT_59006;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59007"))
        {
            uiNameID = R.string.LOT_59007;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59008"))
        {
            uiNameID = R.string.LOT_59008;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59009"))
        {
            uiNameID = R.string.LOT_59009;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59010"))
        {
            uiNameID = R.string.LOT_59010;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59012"))
        {
            uiNameID = R.string.LOT_59012;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59013"))
        {
            uiNameID = R.string.LOT_59013;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59014"))
        {
            uiNameID = R.string.LOT_59014;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59015"))
        {
            uiNameID = R.string.LOT_59015;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59016"))
        {
            uiNameID = R.string.LOT_59016;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59018"))
        {
            uiNameID = R.string.LOT_59018;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59019"))
        {
            uiNameID = R.string.LOT_59019;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59020"))
        {
            uiNameID = R.string.LOT_59020;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59023_B1"))
        {
            uiNameID = R.string.LOT_59023_B1;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59023_B2"))
        {
            uiNameID = R.string.LOT_59023_B2;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59023_B3"))
        {
            uiNameID = R.string.LOT_59023_B3;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59024"))
        {
            uiNameID = R.string.LOT_59024;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59026"))
        {
            uiNameID = R.string.LOT_59026;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59033_1"))
        {
            uiNameID = R.string.LOT_59033_1;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59033_2"))
        {
            uiNameID = R.string.LOT_59033_2;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59033_3"))
        {
            uiNameID = R.string.LOT_59033_3;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59033_4"))
        {
            uiNameID = R.string.LOT_59033_4;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59034"))
        {
            uiNameID = R.string.LOT_59034;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59035"))
        {
            uiNameID = R.string.LOT_59035;
        }
        else if (lotNameDb.equalsIgnoreCase("LOT_59036"))
        {
            uiNameID = R.string.LOT_59036;
        }
        else
        {
            System.out.println("ERROR:  Unknown parking lot " + lotNameDb);
        }

        return uiNameID;
    }
}
