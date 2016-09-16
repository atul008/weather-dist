package com.crossover.trial.weather;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.service.AirportLoader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * Test for AirportLoader
 * 
 * Requires the server to be running before this test is run
 * 
 * Note : Please comment out @Before and @Test annotations and the contents of init() method of
 * RestWeatherQueryEndpoint.java file before running this test case (because when
 * RestWeatherQueryEndpoint class loads it resets stats and contents of airport map which is
 * necessary inorder to pass previously given test cases).
 * 
 */
public class AirportLoaderTest {
  private AirportLoader airportLoader = new AirportLoader();
  private DataPoint _dp;

  // @Before
  public void setUp() throws Exception {
    airportLoader.upload("airports.dat");
    _dp =
        new DataPoint.Builder().withCount(10).withFirst(10).withMedian(20).withLast(30)
            .withMean(22).build();
    updateWeather();
  }

  // @Test
  public void testQuery() throws Exception {
    JsonObject atmosphericInformation = (JsonObject) queryWeather("JFK", "0");
    JsonObject temp = atmosphericInformation.get("temperature").getAsJsonObject();
    assertEquals(temp.get("count").getAsInt(), _dp.getCount());
    assertEquals(temp.get("first").getAsInt(), _dp.getFirst());
  }

  public void updateWeather() {
    WebTarget collect = airportLoader.getCollect().path("/weather/JFK/temperature");
    collect.request().post(Entity.entity(_dp, "application/json"));
  }

  public JsonElement queryWeather(String iata, String radius) {
    WebTarget collect = airportLoader.getQuery().path("/weather/" + iata + "/" + radius);
    Response response = collect.request().get();
    String res = response.readEntity(String.class);
    JsonElement list = new JsonParser().parse(res);
    return (JsonObject) list.getAsJsonArray().get(0);
  }

}
