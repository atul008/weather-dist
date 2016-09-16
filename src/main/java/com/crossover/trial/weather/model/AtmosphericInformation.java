package com.crossover.trial.weather.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * encapsulates sensor information for a particular location
 */
public class AtmosphericInformation {

  private final int milisecsInDay = 86400000;

  /** temperature in degrees celsius */
  private DataPoint temperature;

  /** wind speed in km/h */
  private DataPoint wind;

  /** humidity in percent */
  private DataPoint humidity;

  /** precipitation in cm */
  private DataPoint precipitation;

  /** pressure in mmHg */
  private DataPoint pressure;

  /** cloud cover percent from 0 - 100 (integer) */
  private DataPoint cloudCover;

  /** the last time this data was updated, in milliseconds since UTC epoch */
  private long lastUpdateTime;

  private static Map<DataPointType, BiConsumer<AtmosphericInformation, DataPoint>> dataPointSetterMap;

  static {
    dataPointSetterMap =
        new HashMap<DataPointType, BiConsumer<AtmosphericInformation, DataPoint>>();
    dataPointSetterMap.put(DataPointType.TEMPERATURE, (ai, dp) -> ai.setTemperature(dp));
    dataPointSetterMap.put(DataPointType.WIND, (ai, dp) -> ai.setWind(dp));
    dataPointSetterMap.put(DataPointType.HUMIDTY, (ai, dp) -> ai.setHumidity(dp));
    dataPointSetterMap.put(DataPointType.PRECIPITATION, (ai, dp) -> ai.setPrecipitation(dp));
    dataPointSetterMap.put(DataPointType.PRESSURE, (ai, dp) -> ai.setPressure(dp));
    dataPointSetterMap.put(DataPointType.CLOUDCOVER, (ai, dp) -> ai.setCloudCover(dp));
  }

  public AtmosphericInformation() {}

  protected AtmosphericInformation(DataPoint temperature, DataPoint wind, DataPoint humidity,
      DataPoint percipitation, DataPoint pressure, DataPoint cloudCover) {
    this.temperature = temperature;
    this.wind = wind;
    this.humidity = humidity;
    this.precipitation = percipitation;
    this.pressure = pressure;
    this.cloudCover = cloudCover;
    this.lastUpdateTime = System.currentTimeMillis();
  }

  public DataPoint getTemperature() {
    return temperature;
  }

  public void setTemperature(DataPoint temperature) {
    if (temperature.getMean() >= -50 && temperature.getMean() < 100) {
      this.temperature = temperature;
    }
  }

  public DataPoint getWind() {
    return wind;
  }

  public void setWind(DataPoint wind) {
    if (wind.getMean() >= 0) {
      this.wind = wind;
    }
  }

  public DataPoint getHumidity() {
    return humidity;
  }

  public void setHumidity(DataPoint humidity) {
    if (humidity.getMean() >= 0 && humidity.getMean() < 100) {
      this.humidity = humidity;
    }
  }

  public DataPoint getPrecipitation() {
    return precipitation;
  }

  public void setPrecipitation(DataPoint precipitation) {
    if (precipitation.getMean() >= 0 && precipitation.getMean() < 100) {
      this.precipitation = precipitation;
    }
  }

  public DataPoint getPressure() {
    return pressure;
  }

  public void setPressure(DataPoint pressure) {
    if (pressure.getMean() >= 650 && pressure.getMean() < 800) {
      this.pressure = pressure;
    }
  }

  public DataPoint getCloudCover() {
    return cloudCover;
  }

  public void setCloudCover(DataPoint cloudCover) {
    if (cloudCover.getMean() >= 0 && cloudCover.getMean() < 100) {
      this.cloudCover = cloudCover;
    }
  }

  public long getLastUpdateTime() {
    return this.lastUpdateTime;
  }

  protected void setLastUpdateTime(long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public boolean hasAtleastOneNotNullField() {
    return this.getCloudCover() != null || this.getHumidity() != null
        || this.getPrecipitation() != null || this.getPressure() != null
        || this.getTemperature() != null || this.getWind() != null;
  }

  public boolean wasUpdatedInLastDay() {
    return this.getLastUpdateTime() > System.currentTimeMillis() - milisecsInDay;
  }

  public void setDataPoint(DataPointType dataPointType, DataPoint dataPoint) {
    dataPointSetterMap.get(dataPointType).accept(this, dataPoint);
    this.setLastUpdateTime(System.currentTimeMillis());
  }

}
