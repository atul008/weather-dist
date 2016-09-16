package com.crossover.trial.weather.controller;

import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.crossover.trial.weather.model.Airport;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.WeatherException;
import com.crossover.trial.weather.service.AirportService;
import com.google.gson.Gson;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport weather collection
 * sites via secure VPN.
 *
 * @author code test administrator
 */

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {
  public final static Logger LOGGER = Logger
      .getLogger(RestWeatherCollectorEndpoint.class.getName());
  private final AirportService airportService = AirportService.getInstance();

  /** shared gson json to object factory */
  public final static Gson gson = new Gson();

  @Override
  public Response ping() {
    return Response.status(Response.Status.OK).entity("ready").build();
  }

  @Override
  public Response updateWeather(String iataCode, String pointType, String datapointJson) {
    try {
      DataPoint dp = gson.fromJson(datapointJson, DataPoint.class);
      airportService.updateAtmosphericInformation(iataCode, pointType, dp);
    } catch (WeatherException e) {
      LOGGER.severe(e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Response.Status.OK).build();
  }

  @Override
  public Response getAirports() {
    Set<String> retval = airportService.getAllAirports();
    return Response.status(Response.Status.OK).entity(retval).build();
  }

  @Override
  public Response getAirport(String iata) {
    Airport airport = airportService.getAirport(iata);
    AirportData ad = null;
    if (airport != null) {
      ad = airport.getAirportData();
    }
    return Response.status(Response.Status.OK).entity(ad).build();
  }

  @Override
  public Response addAirport(String iata, String latString, String longString) {
    try {
      airportService.addAirport(iata, Double.valueOf(latString), Double.valueOf(longString));
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Override
  public Response deleteAirport(String iata) {
    try {
      airportService.removeAirport(iata);
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Override
  public Response exit() {
    System.exit(0);
    return Response.noContent().build();
  }

}
