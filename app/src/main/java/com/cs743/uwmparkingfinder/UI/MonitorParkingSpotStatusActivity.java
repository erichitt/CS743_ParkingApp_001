/*******************************************************************************
 * File Name:  MonitorParkingSpotStatusActivity.java
 *
 * Description:
 * Handles the monitor parking spot status screen activity.
 *
 * Revision  Date        Author             Summary of Changes Made
 * --------  ----------- ------------------ ------------------------------------
 * 1         13-Nov-2015 Eric Hitt          Original
 ******************************************************************************/
package com.cs743.uwmparkingfinder.UI;

/****************************    Include Files    *****************************/
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cs743.uwmparkingfinder.GPSLocation.LocationTracker;
import com.cs743.uwmparkingfinder.GPSLocation.ProviderLocationTracker;
import com.cs743.uwmparkingfinder.HTTPManager.HttpManager;
import com.cs743.uwmparkingfinder.HTTPManager.RequestPackage;
import com.cs743.uwmparkingfinder.Parser.JSONParser;
import com.cs743.uwmparkingfinder.Session.Session;
import com.cs743.uwmparkingfinder.Structures.Lot;
import com.cs743.uwmparkingfinder.Structures.SelectedParkingLot;
import com.cs743.uwmparkingfinder.Utility.UTILITY;
import com.cs743.uwmparkingfinder.Structures.Space;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/****************************  Class Definitions  *****************************/

/**
 * Create a new monitor parking spot status screen activity class
 */
public class MonitorParkingSpotStatusActivity extends AppCompatActivity
{
    /*************************  Class Static Variables  ***********************/

    /// Polling timer interval (milliseconds)
    public static final int POLL_TIMER_INTERVAL_MSEC = 1000;

    /*************************  Class Member Variables  ***********************/
    private ProviderLocationTracker tracker;
    private TextView selectedLotNameLabel_;         ///< Parking lot name label
    private ListView parkingSpotStatusList_;        ///< List of parking spots
    private SelectedParkingLot selectedLot_;        ///< Selected parking lot
    private Timer pollTimer_;                   ///< Poll timer
    private SpacesAdapter adapter;

    /*************************  Class Public Interface  ***********************/

    /**
     * Creates the monitor parking spot status page.
     *
     * @param savedInstanceState Saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_parking_spot_status);

        //initialize tracker
        tracker = new ProviderLocationTracker(MonitorParkingSpotStatusActivity.this, ProviderLocationTracker.ProviderType.GPS);

        // Retrieve screen inputs
        selectedLotNameLabel_ = (TextView)findViewById(R.id.selectedLotNameLabel);
        parkingSpotStatusList_ = (ListView)findViewById(R.id.parkingSpotStatusList);

        // Retrieve intent
        Intent intent = getIntent();
        selectedLot_ =
                (SelectedParkingLot)intent.getSerializableExtra(RecommendParkingActivity.PREFERENCES_INTENT_DATA);

        // Set parking lot name
        String uiLotName = getResources().getString(UTILITY.convertDbLotNameToUINameID(selectedLot_.getParkingLotName()));
        selectedLotNameLabel_.setText("Lot:  " + uiLotName);

        //get list of spaces in the selected lot
        Space[] spaces=getSpacesList();

        if (spaces!=null) {

            //create and set custom array adapter
            adapter = new SpacesAdapter(this, R.layout.activity_spotlistview, spaces);
            parkingSpotStatusList_.setAdapter(adapter);

        } else {
            System.out.println("ERROR: No parking spots found for " + selectedLot_.getParkingLotName());
        }


        tracker.start(new LocationTracker.LocationUpdateListener() {
            @Override
            public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime) {
                NumberFormat formatter = new DecimalFormat("#0.00000");
                //LOG LOCATION UPDATES TO THE CONSOLE FOR DEBUGGING/REFERENCE
                Log.i("LOCATION UPDATED", tracker.hasLocation() ? ("old: [" + oldLoc.getLatitude() + ", " + oldLoc.getLongitude() + "]") : "no previous location");
                Log.i("LOCATION UPDATED", "new: [" + newLoc.getLatitude() + ", " + newLoc.getLongitude() + "]\n");
                Space[] spaces=null;
                //get updated information from backend - STORED IN Session.getCurrentLotList();
                if(UTILITY.isOnline(getApplicationContext())){
                    //get list of spaces in the selected lot
                    //this should be a call to the backend, so it will need the if online logic...eventually
                    spaces=getSpacesList();
                }else{
                    Toast.makeText(getApplicationContext(), "you are not connected to the internet", Toast.LENGTH_LONG).show();
                }
                //clear the current spots from the list
                adapter.clear();
                if(spaces!=null) {
                    //add new spaces to adapter
                    adapter.addAll(spaces);
                    //refresh the view
                    adapter.notifyDataSetChanged();
                } else {
                    //I feel like we shoul put a warning...maybe if we have time!
                }
            }
        });
    }

    private Space[] getSpacesList() {
        List<Lot> l=Session.getCurrentLotList();
        List<Space> tmpSpaces=new ArrayList<>();
        int totalSpaces=0;
        for (Lot lot: l) {
            if (selectedLot_.getParkingLotName().equalsIgnoreCase(lot.getName())){
                //TODO:is there a way to get all the spaces for a lot because these are only available spaces
                tmpSpaces=lot.getSpaces();
                totalSpaces=lot.getNumSpaces();
                break;
            }
        }


        if(!tmpSpaces.isEmpty()) {
            Space[] spaces=new Space[totalSpaces];
            //TODO:Fix--This is not real data
            //since the list in Lot is only available spaces, fill in the unavailable spaces
            boolean handicap=tmpSpaces.get(0).isHandicap();
            boolean electric=tmpSpaces.get(0).isElectric();

            for(Space s:tmpSpaces) {
                spaces[s.getNumber()]=s;
            }
            for(int i=0;i<spaces.length;i++) {
                if(spaces[i]==null) {
                    spaces[i]=new Space(i,selectedLot_.getParkingLotName(),false,"False",handicap,electric);
                }
            }
            return spaces;
        } else {
            return null;
        }
    }

    /* used https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView to help
    get the custom arrayadapter working*/
    private class SpacesAdapter extends ArrayAdapter<Space> {

        public SpacesAdapter (Context context, int layoutResourceId, Space[] spaces) {
            super(context,layoutResourceId,spaces);
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent) {
            //if the current view is null, create a new one.
            if(convertView==null){
                convertView= LayoutInflater.from(getContext()).inflate(R.layout.activity_spotlistview,parent,false);
            }

            //fill spot's number and availability
            if (getItem(position)!=null) {
                //get the TextView objects
                TextView number = (TextView) convertView.findViewById(R.id.spotNumber);
                TextView available = (TextView) convertView.findViewById(R.id.spotAvailable);

                //set the name and availability of the space
                number.setText("Space: " + Integer.toString(getItem(position).getNumber()));
                available.setText(getItem(position).isAvailable() ? "Available" : "Unavailable");

                //set row background color to green=available and red=unavailable
                if (getItem(position).isAvailable()) {
                    convertView.setBackgroundColor(Color.GREEN);
                } else {
                    convertView.setBackgroundColor(Color.RED);
                }
            }
            return convertView;
        }
    }

}
