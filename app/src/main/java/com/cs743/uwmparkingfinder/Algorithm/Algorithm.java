package com.cs743.uwmparkingfinder.Algorithm;

import android.location.Location;

import com.cs743.uwmparkingfinder.Session.Session;
import com.cs743.uwmparkingfinder.Structures.Building;
import com.cs743.uwmparkingfinder.Structures.Lot;
import com.cs743.uwmparkingfinder.Structures.ParkingPreferences;
import com.cs743.uwmparkingfinder.Structures.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Masaki Ikuta on 11/20/2015.
 * Updated by masaki Ikuta on 11/22/2015 5:40PM.
 */
public class Algorithm {

    // The average parking expense per hour in the campus.
    // At UWM, the parking fee varies from $1.00 to $2.00.
    // This should be pre-determined value for Recommender system
    private static double AVERAGE_PARKING_FEE_PER_HOUR = 1.50;

    // The average walking distance from a parking lot to a destination.
    // This should be pre-determined value for Recommender system
    // Note that it is about 500 meters from North Quadrant to EMS building.
    private static double AVERAGE_DISTANCE_LOT_TO_DEST = 500.00;

    private List<Lot> sortedLotList_;                   ///< Sorted lots from algorithm

    private static Algorithm sInstance_ = null;         ///< Class instance

    /**
     * Default (private) constructor
     */
    private Algorithm()
    {
        sortedLotList_ = new ArrayList<Lot>();
    }

    /**
     * Retrieve the instance of the class.
     *
     * Creates a new instance if one has not yet been created.
     *
     * @return Singleton instance of the class
     */
    public static Algorithm getInstance()
    {
        if (sInstance_ == null)
        {
            sInstance_ = new Algorithm();
        }

        return sInstance_;
    }

    /**
     * Returns the list of computed lots
     *
     * @return list of computed parking lots
     */
    public List<Lot> getLotList()
    {
        return sortedLotList_;
    }

    /**
     * Determines a set of recommended parking lots.
     *
     * @param preferences user preferences
     *
     * @return List of recommended parking lots
     */
    public List<Lot> computeRecommendedLots(ParkingPreferences preferences)
    {
        System.out.println("Algorithm: Entry");

        // Declare the resulting list
        List<Lot> sortedLotList = sortedLotList_;

        // Get the current lot list
        List<Lot> currentLotList = Session.getCurrentLotList();
        System.out.println("Algorithm:    currentLotList = " + currentLotList.size());

        // Get the building list
        List<Building> currentBuildings = Session.getCurrentBuildings();
        System.out.println("Algorithm:    currentBuildings = " + currentBuildings.size());

        // Get the building user wants to go
        Building building = currentBuildings.get(0);
        building.setName(""); // Setting the initial value to null string so that it can be used for error checking
        for(Building each_building : currentBuildings)
        {
            if(preferences.getDestination().equals(each_building.getName()))
            {
                building = each_building;
                break;
            }
        }
        System.out.println("Algorithm:    destination = " + building.getName());

        // Get distORprice
        //TODO: distORprice preference is in both parkingPreferences class and in the User class, so which should be used?
        int distORprice = Session.getCurrentUser().getDistORprice();
        System.out.println("Algorithm:    distORprice = " + distORprice);

        // Just return the empty list if some variables are not valid.
        if(currentLotList.size() == 0 || currentBuildings.size() == 0 || building.getName().equals(""))
        {
            return sortedLotList;
        }

        // Declare a tree map for the sorting
        TreeMap<Double, Lot> tmap = new TreeMap<Double, Lot>();

        //
        // Compute x_prime for each parking lot
        //
        for (Lot lot : currentLotList)
        {
            double x = computeNormalizedDistance(building, lot);
            double y = getNormalizedParkingCost(lot);
            double theta = (double)((distORprice - 1) * 10); // TODO Make sure if distORprice ranges from 1 to 10
            double x_prime = x * Math.cos(Math.toRadians(theta)) + y * Math.sin(Math.toRadians(theta));

            System.out.println("Algorithm:    lot_name = " + lot.getName() + ", distance = " + computeDistanceInMeter(building, lot) + ", fee = " + lot.getRate()
                    + ", x = " + x + ", y = " + y + ", theta = " + theta + ", x_prime = " + x_prime);
            tmap.put(x_prime, lot);
        }

        // Generate a sorted list
        Set set = tmap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry) iterator.next();
            sortedLotList.add((Lot) mentry.getValue());
        }

        System.out.println("Algorithm: Exit");

        return sortedLotList;
    }

    private double getNormalizedParkingCost(Lot lot)
    {
        return lot.getRate() / AVERAGE_PARKING_FEE_PER_HOUR;
    }

    private double computeNormalizedDistance(Building building, Lot lot)
    {

        return computeDistanceInMeter(building, lot) / AVERAGE_DISTANCE_LOT_TO_DEST;
    }

    private double computeDistanceInMeter(Building building, Lot lot)
    {
        Location building_location = building.getLocation();
        Location lot_location = lot.getLocation();
        float[] result = new float[1];

        //System.out.println("DEBUG: Algorithm: getActualDistance: " + building_location.getLatitude() + ", " + building_location.getLongitude() + ", " + lot_location.getLatitude() + ", " + lot_location.getLongitude());
        Location.distanceBetween(building_location.getLatitude(), building_location.getLongitude(), lot_location.getLatitude(), lot_location.getLongitude(), result);

        return result[0];
    }
}