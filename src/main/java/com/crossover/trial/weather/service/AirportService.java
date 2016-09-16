package com.crossover.trial.weather.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.crossover.trial.weather.model.Airport;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;
import com.crossover.trial.weather.model.WeatherException;

public class AirportService {
  public final double earthRadius = 6372.8;
  private Map<String, Airport> airportMap;

  private static class InstanceHolder {
    public static AirportService INSTANCE = new AirportService();
  }

  private AirportService() {
    airportMap = new ConcurrentHashMap<String, Airport>();
  }

  public static AirportService getInstance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Add a new known airport to our list.
   *
   * @param iataCode 3 letter code
   * @param latitude in degrees
   * @param longitude in degrees
   *
   * @return the added airport
   */
  public void addAirport(String iataCode, double latitude, double longitude) {
    Airport airport = new Airport();
    airport.setAirportData(new AirportData(iataCode, latitude, longitude));
    airport.setAtmosphericInformation(new AtmosphericInformation());
    airportMap.put(iataCode, airport);
  }

  public Airport getAirport(String iata) {
    return airportMap.get(iata);
  }

  public Set<String> getAllAirports() {
    return airportMap.keySet();
  }

  /**
   * Deletes the airport from the airportMap
   * 
   * @param iata
   */
  public void removeAirport(String iata) {
    airportMap.remove(iata);
  }

  public void clearAirports() {
    airportMap.clear();
  }

  /**
   * update atmospheric information with the given data point for the given point type
   *
   * @param iataCode the airport whose atmospheric information object to update
   * @param pointType the data point type as a string
   * @param dp the actual data point
   */
  public void updateAtmosphericInformation(String iataCode, String pointType, DataPoint dp)
      throws WeatherException {
    Airport airport = getAirport(iataCode);
    if (airport != null) {
      try {
        DataPointType dptype = DataPointType.valueOf(pointType.toUpperCase());
        airport.getAtmosphericInformation().setDataPoint(dptype, dp);
      } catch (Exception e) {
        throw new IllegalStateException("couldn't update atmospheric data");
      }
    }
  }

  /**
   * 
   * Generates AtmosphericInformation for the requested IATA code and the airports within
   * the provided radius
   * 
   * @param iata
   * @param radius
   * @return
   */
  public List<AtmosphericInformation> getWeatherWithinRadius(String iata, double radius) {
    List<AtmosphericInformation> retval = new ArrayList<AtmosphericInformation>();
    Airport airport = airportMap.get(iata);
    if (airport == null) {
      return retval;
    }
    if (radius == 0) {
      retval.add(airport.getAtmosphericInformation());
      return retval;
    }
    AirportData airportData = airport.getAirportData();
    for (Entry<String, Airport> entry : airportMap.entrySet()) {
      Airport targetAirport = entry.getValue();
      double distance = calculateDistance(airportData, targetAirport.getAirportData());
      if (distance <= radius) {
        AtmosphericInformation ai = targetAirport.getAtmosphericInformation();
        if (ai.hasAtleastOneNotNullField()) {
          retval.add(ai);
        }
      }
    }
    return retval;
  }


  /**
   * Haversine distance between two airports.
   *
   * @param ad1 airport 1
   * @param ad2 airport 2
   * @return the distance in KM
   */
  private double calculateDistance(AirportData ad1, AirportData ad2) {
    double deltaLat = Math.toRadians(ad2.getLatitude() - ad1.getLatitude());
    double deltaLon = Math.toRadians(ad2.getLongitude() - ad1.getLongitude());
    double a =
        Math.pow(Math.sin(deltaLat / 2), 2) + Math.pow(Math.sin(deltaLon / 2), 2)
            * Math.cos(ad1.getLatitude()) * Math.cos(ad2.getLatitude());
    double c = 2 * Math.asin(Math.sqrt(a));
    return earthRadius * c;
  }

  /**
   * @return Number of valid atmosphericInformation which was updates in last 24 hours
   */
  public Integer getDataSize() {
    int datasize = 0;
    for (Entry<String, Airport> entry : airportMap.entrySet()) {
      AtmosphericInformation atmosphericInformation = entry.getValue().getAtmosphericInformation();
      // we only count recent readings updated in the last day
      if (atmosphericInformation.hasAtleastOneNotNullField()
          && atmosphericInformation.wasUpdatedInLastDay()) {
        datasize++;
      }
    }
    return datasize;
  }

}
