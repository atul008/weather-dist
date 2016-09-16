package com.crossover.trial.weather.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StatsService {
  /**
   * Internal performance counter to better understand most requested information, this map can be
   * improved but for now provides the basis for future performance optimizations. Due to the
   * stateless deployment architecture we don't want to write this to disk, but will pull it off
   * using a REST request and aggregate with other performance metrics {@link #ping()}
   */
  public static Map<String, Integer> requestFrequency;
  public static Map<Double, Integer> radiusFreq;
  public static AtomicInteger totalFrequency;
  AirportService airportServ;

  private static class InstanceHolder {
    public static StatsService INSTANCE = new StatsService();
  }

  public static StatsService getInstance() {
    return InstanceHolder.INSTANCE;
  }

  private StatsService() {
    requestFrequency = new ConcurrentHashMap<String, Integer>();
    radiusFreq = new ConcurrentHashMap<Double, Integer>();
    airportServ = AirportService.getInstance();
    totalFrequency = new AtomicInteger(0);
  }

  public void reset() {
    requestFrequency.clear();
    radiusFreq.clear();
    totalFrequency.set(0);
  }

  /**
   * Records information about how often requests are made
   *
   * @param iata an iata code
   * @param radius query radius
   */
  public void updateRequestFrequency(String iata, Double radius) {
    requestFrequency.put(iata, requestFrequency.getOrDefault(iata, 0) + 1);
    radiusFreq.put(radius, radiusFreq.getOrDefault(radius, 0));
    totalFrequency.incrementAndGet();
  }

  /**
   * Return map of all stats elements
   *
   */
  public Map<String, Object> getStats() {
    Map<String, Object> retval = new HashMap<String, Object>();
    retval.put("datasize", airportServ.getDataSize());
    retval.put("iata_freq", getRequestFrequency());
    retval.put("radius_freq", getRadiusFreqHist());
    return retval;
  }

  
  /**
   * @return Returns a map of ratios of each iata request to the total requests 
   */
  public Map<String, Double> getRequestFrequency() {
    Map<String, Double> frequencyMap = new HashMap<String, Double>();
    for (String iata : airportServ.getAllAirports()) {
      double frac = requestFrequency.getOrDefault(iata, 0) / totalFrequency.get();
      frequencyMap.put(iata, frac);
    }
    return frequencyMap;
  }

  /**
   * @return returns a histogram of radius searches with a bucket size of 10
   */
  public int[] getRadiusFreqHist() {
    int m = radiusFreq.keySet().stream().max(Double::compare).orElse(1000.0).intValue();
    m = m / 10 + 1;
    int[] hist = new int[m];
    Arrays.fill(hist, 0);
    for (Map.Entry<Double, Integer> e : radiusFreq.entrySet()) {
      int i = e.getKey().intValue() / 10;
      hist[i] += e.getValue();
    }
    return hist;
  }

}
