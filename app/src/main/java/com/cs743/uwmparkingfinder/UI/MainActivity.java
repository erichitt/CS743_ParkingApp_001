/*******************************************************************************
 * File Name:  MainActivity.java
 *
 * Description:
 * Handles the UWM Welcome screen activity.
 *
 * Revision  Date        Author             Summary of Changes Made
 * --------  ----------- ------------------ ------------------------------------
 * 1         04-Nov-2015 Eric Hitt          Original
 ******************************************************************************/
package com.cs743.uwmparkingfinder.UI;

/****************************    Include Files    *****************************/
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs743.uwmparkingfinder.Algorithm.Algorithm;
import com.cs743.uwmparkingfinder.HTTPManager.HttpManager;
import com.cs743.uwmparkingfinder.HTTPManager.RequestPackage;
import com.cs743.uwmparkingfinder.Parser.JSONParser;
import com.cs743.uwmparkingfinder.Session.Session;
import com.cs743.uwmparkingfinder.Structures.Building;
import com.cs743.uwmparkingfinder.Structures.LogItem;
import com.cs743.uwmparkingfinder.Structures.Lot;
import com.cs743.uwmparkingfinder.Utility.UTILITY;
import com.cs743.uwmparkingfinder.Structures.ParkingRequest;
import com.cs743.uwmparkingfinder.Structures.SelectedParkingLot;
import com.cs743.uwmparkingfinder.Structures.User;

import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/****************************  Class Definitions  *****************************/

/**
 * UWM Welcome screen activity class
 */
public class MainActivity extends AppCompatActivity
{
    /*************************  Class Static Variables  ***********************/

    /*************************  Class Member Variables  ***********************/

    private TextView welcomeMsg_;                   // Welcome mesage
    private ListView listView_;                     // Main menu
    private int LOGS_MIN_THRESHOLD=3;               // minimum number of logs required to try and guess destination
    private int LOGS_MAX_TIME_DIFF=5;               // Number of months from which to consider log entries for guessing destination

    /*************************  Class Public Interface  ***********************/

    /**
     * Creates the welcome screen.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve screen inputs
        welcomeMsg_ = (TextView)findViewById(R.id.welcomeText);
        listView_ = (ListView)findViewById(R.id.tableOfContents);

        // Create the table of contents list view
        Resources res = getResources();
        String[] tocList = res.getStringArray(R.array.tableOfContents);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_toclistview,
                                                        tocList);
        listView_.setAdapter(adapter);


        // Make web service call to get current lot listing
        if(isOnline())
        {
            //*****FOR EACH CALL TO THE WEBSERVICE, THIS OVERHEAD MUST BE DONE*****
            //1. Create a RequestPackage Object that will hold all of the information
            RequestPackage p = new RequestPackage();
            //2. Set the Request type
            p.setMethod("GET");
            //3. Set the URL
            p.setUri("http://ec2-54-152-4-103.compute-1.amazonaws.com/scripts.php");
            //4. Set all of the parameters
            p.setParam("query", "available");
            //5. Make a call to a private class extending AsyncTask which will run off of the main thread.
            new WebserviceCallOne().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "you are not connected to the internet", Toast.LENGTH_LONG).show();
        }

        // ListView item click listener (based on androidexample.com)
        listView_.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                int itemPosition = position;        // ListView clicked item index
                String itemValue = (String) listView_.getItemAtPosition(position);
                Resources res = getResources();

                if (itemValue.equalsIgnoreCase(res.getString(R.string.tocFindParking)))
                {
                    // Pressed the find parking menu item
                    processFindParkingSelection();
                }
                else if (itemValue.equalsIgnoreCase(res.getString(R.string.tocViewParking)))
                {
                    // Pressed the view parking menu item
                    processViewParkingSelection();
                }
                else if (itemValue.equalsIgnoreCase(res.getString(R.string.tocEditPrefs)))
                {
                    // Pressed the edit preferences menu item
                    processEditPreferencesSelection();
                }
                else if (itemValue.equalsIgnoreCase(res.getString(R.string.tocExit)))
                {
                    // Exit the application
                    System.exit(0);
                }
                else
                {
                    // Unrecognized click
                    System.err.println("ERROR:  Unrecognized command " + itemValue + "detected!");
                }
            }
        });
    }

    /************************  Class Private Interface  ***********************/

    /**
     * Called when the user selects the find parking menu item
     */
    private void processFindParkingSelection()
    {
        System.out.println("Processing find parking selection");

        // TODO:  Determine if should create a new preference or suggest based on past data
        // Need to create a new preference

        if (isOnline()){
            //determine current time using UTC as time zone
            Calendar cal=GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));

            RequestPackage p = new RequestPackage();
            p.setMethod("GET");
            p.setUri(UTILITY.UBUNTU_SERVER_URL);
            p.setParam("query", "log");
            User user = Session.getCurrentUser();
            p.setParam("username", user.getUsername());
            p.setParam("day",String.valueOf(formatDay(cal.get(Calendar.DAY_OF_WEEK))));
            new getLogItemServiceCall(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p);
        } else {
            Toast.makeText(getApplicationContext(), "you are not connected to the internet", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Returns the index of the day of week that matches the MySQL indexes for the days of the week.
     * @param day the index of the day of week in a GregorianCalendar
     * @return an integer representing the day of week using MySQL indexing for days of week.
     */
    public int formatDay(int day){
        switch(day){
            case 2:
                return 0;
            case 3:
                return 1;
            case 4:
                return 2;
            case 5:
                return 3;
            case 6:
                return 4;
            case 7:
                return 5;
            case 1:
                return 6;
            default:
                return -1;
        }
    }

    /**
     * Called when the user selects the view parking menu item
     */
    private void processViewParkingSelection()
    {
        System.out.println("Processing view parking selection");

        // Create a new Intent for the ViewParkingMenuActivity
        Intent intent = new Intent(this, ViewParkingMenuActivity.class);

        // Go to the view parking menu screen
        startActivity(intent);
    }

    /**
     * Called when the user selects the edit preferences menu item
     */
    private void processEditPreferencesSelection()
    {
        System.out.println("Processing edit preferences selection");

        // Go to the edit preferences page
        Intent intent = new Intent(this, EditPreferencesActivity.class);
        startActivity(intent);
    }

    /**
     * Check to see whether there is an internet connection or not.
     * @return whether there is an internet connection
     */
    public boolean isOnline()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Webservice call class, used to get current lots available
     */
    private class WebserviceCallOne extends AsyncTask<RequestPackage, String, List<List<Lot>>>
    {
        @Override
        protected List<List<Lot>> doInBackground(RequestPackage... params)
        {

            String content = HttpManager.getData(params[0]);

            return JSONParser.parseLotFeed(content);
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
        }

        @Override
        protected void onPostExecute(List<List<Lot>> s)
        {
            if(s != null)
            {
                if(s != null){
                    Session.setCurrentLotList(s.get(UTILITY.AVAILABLE));
                    Session.setAllSpacesByLot(s.get(UTILITY.ALL));
                }
            }
            else
            {
                System.out.println("No rows available!!!");
            }
        }

        @Override
        protected void onPreExecute()
        {
            System.out.println("Getting Information...");
        }
    }

    private class getLogItemServiceCall extends AsyncTask<RequestPackage,String,List<LogItem>> {
       Activity activity;

        getLogItemServiceCall(Activity act) {
            activity=act;
        }

        @Override
        protected List<LogItem> doInBackground(RequestPackage... params) {
            String content = HttpManager.getData(params[0]);
            return JSONParser.parseLogFeed(content);
        }
        @Override
        protected void onPostExecute(List<LogItem> logs) {
            if (logs !=null) {
                Session.setCurrentLog(logs);

                //see if there is a destination in these log items
                Building destBuilding=getLikelyDestination(logs);

                //if no destination can be determined, go to SelectParkingOptionsActivity
                if(destBuilding==null) {
                    // Need to create a new preference
                    doNoDestinationFound();
                } else {

                    //use destination to get a recommended parking spot
                    String destination=destBuilding.getName();

                    //get the current user
                    User curUser=Session.getCurrentUser();

                    int disORprice=curUser.getDistORprice();
                    boolean outsideAllowed=curUser.isCovered();
                    boolean disableParkNeeded=curUser.isHandicap();
                    boolean electricParkNeeded=curUser.isElectric();
                    ParkingRequest request = new ParkingRequest(destination, disORprice, outsideAllowed,
                            disableParkNeeded, electricParkNeeded);

                    SelectedParkingLot selectedLot = findParkingLot(request);

                    selectedLot.setReason("You have previously gone to "+destination+" around this time. "+selectedLot.getReason());

                    Intent intent = new Intent(activity, RecommendParkingActivity.class);
                    intent.putExtra(RecommendParkingActivity.PREFERENCES_INTENT_DATA, selectedLot);
                    startActivity(intent);
                }
            } else {
                doNoDestinationFound();
            }
        }

        private void doNoDestinationFound() {
            Intent intent = new Intent(activity, SelectParkingOptionsActivity.class);
            startActivity(intent);
        }

        private SelectedParkingLot findParkingLot(ParkingRequest request)
        {
            // Compute recommended parking lot
            Resources res = getResources();
            Algorithm algorithm = Algorithm.getInstance();
            List<Lot> sortedLots = algorithm.computeRecommendedLots(request);
            if (sortedLots.size() == 0)
            {
                // No lots were found
                return null;
            }
            else
            {
                String reason = res.getString(R.string.LOT_REASON_DIST);
                if (Session.getCurrentUser().getDistORprice() > 50)
                {
                    reason = res.getString(R.string.LOT_REASON_COST);
                }

                return new SelectedParkingLot(sortedLots.get(0).getName(), reason, request.getDestination());
            }
        }

        private Building getLikelyDestination(List<LogItem> logs) {
            Building destBuilding=null;
            if (logs.size()>LOGS_MIN_THRESHOLD) {
                HashMap<String, Integer> buildings = new HashMap<>();
                int maxNum = 0;
                String destination = "";
                Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
                int curMonths = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH);
                //get subset of logs with times within 30 minutes of the current time
                List<LogItem> inRange = UTILITY.getCurrentLogWithinRange(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 30);

                for (LogItem item : inRange) {
                    //get the time this log occurred
                    GregorianCalendar time = item.getTime();
                    int logMonths = time.get(Calendar.YEAR) * 12 + time.get(Calendar.MONTH);

                    //only consider logs that occurred in the last few months
                    if (curMonths - logMonths < LOGS_MAX_TIME_DIFF) {
                        //get destination associated with this log entry
                        String word = item.getKeyword();

                        //if the map alread contains this building, increment number of occurrences
                        if (!word.equalsIgnoreCase("HELLO") && !word.contains(",")) {
                            if (buildings.containsKey(word)) {
                                Integer oldVal = buildings.get(word);
                                Integer newVal = oldVal + 1;
                                buildings.put(word, newVal);

                                //if this building has occurred the most, set it as the best
                                if (newVal > maxNum) {
                                    maxNum = newVal;
                                    destination = word;
                                }
                            } else {
                                //otherwise, add building to the map with count of one
                                buildings.put(word, 1);
                                if (1 > maxNum) {
                                    maxNum = 1;
                                    destination = word;
                                }
                            }
                        }
                    }
                }

                //if at least one building occurrs more than once
                if (maxNum>1) {
                    destBuilding=UTILITY.getBuildingFromString(destination);
                }
            }
            return destBuilding;
        }
    }
}
