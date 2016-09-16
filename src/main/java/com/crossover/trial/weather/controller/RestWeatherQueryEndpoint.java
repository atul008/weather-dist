package com.crossover.trial.weather.controller;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.service.AirportService;
import com.crossover.trial.weather.service.StatsService;
import com.google.gson.Gson;

/**
 * The Weather App REST endpoint allows clients to query, update and check health stats. Currently,
 * all data is held in memory. The end point deploys to a single container
 *
 * @author code test administrator
 */
@Path("/query")
public class RestWeatherQueryEndpoint implements WeatherQueryEndpoint {

  public final static Logger LOGGER = Logger.getLogger("WeatherQuery");
  private final static AirportService airportService = AirportService.getInstance();
  private final static StatsService statsService = StatsService.getInstance();
  /** shared gson json to object factory */
  public static final Gson gson = new Gson();


  static {
    init();
  }

  /**
   * Retrieve service health including total size of valid data points and request frequency
   * information.
   *
   * @return health stats for the service as a string
   */
  @Override
  public String ping() {
    Map<String, Object> retval = statsService.getStats();
    return gson.toJson(retval);
  }

  /**
   * Given a query in json format {'iata': CODE, 'radius': km} extracts the requested airport
   * information and return a list of matching atmosphere information.
   *
   * @param iata the iataCode
   * @param radiusString the radius in km
   *
   * @return a list of atmospheric information
   */
  @Override
  public Response weather(String iata, String radiusString) {
    double radius =
        radiusString == null || radiusString.trim().isEmpty() ? 0 : Double.valueOf(radiusString);
    List<AtmosphericInformation> retval = airportService.getWeatherWithinRadius(iata, radius);
    statsService.updateRequestFrequency(iata, radius);
    return Response.status(Response.Status.OK).entity(retval).build();
  }

  /**
   * A dummy init method that loads hard coded data
   */
  public static void init() {
    airportService.clearAirports();
    statsService.reset();
    airportService.addAirport("BOS", 42.364347, -71.005181);
    airportService.addAirport("EWR", 40.6925, -74.168667);
    airportService.addAirport("JFK", 40.639751, -73.778925);
    airportService.addAirport("LGA", 40.777245, -73.872608);
    airportService.addAirport("MMU", 40.79935, -74.4148747);
  }

}
