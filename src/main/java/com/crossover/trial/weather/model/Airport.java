package com.crossover.trial.weather.model;

public class Airport {
  private AirportData airportData;
  private AtmosphericInformation atmosphericInformation;

  public AirportData getAirportData() {
    return airportData;
  }

  public void setAirportData(AirportData airportData) {
    this.airportData = airportData;
  }

  public AtmosphericInformation getAtmosphericInformation() {
    return atmosphericInformation;
  }

  public void setAtmosphericInformation(AtmosphericInformation atmosphericInformation) {
    this.atmosphericInformation = atmosphericInformation;
  }
}
