package com.crossover.trial.weather.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.crossover.trial.weather.model.AirportData;

/**
 * A simple airport loader which reads a file from disk and sends entries to the webservice
 *
 * @author code test administrator
 */
public class AirportLoader {

  /** end point for read queries */
  private WebTarget query;

  /** end point to supply updates */
  private WebTarget collect;

  public AirportLoader() {
    Client client = ClientBuilder.newClient();
    query = client.target("http://localhost:9090/query");
    collect = client.target("http://localhost:9090/collect");
  }

  public WebTarget getQuery() {
    return query;
  }

  public WebTarget getCollect() {
    return collect;
  }

  public static List<AirportData> readData(String fileName) throws Exception {
    FileInputStream airportDataStream = null;
    try {
      URL url = AirportLoader.class.getResource("/" + fileName);
      File airportDataFile = new File(url.toURI());
      if (!airportDataFile.exists() || airportDataFile.length() == 0) {
        System.err.println(airportDataFile + " is not a valid input");
        System.exit(1);
      }
      airportDataStream = new FileInputStream(airportDataFile);
      BufferedReader reader = new BufferedReader(new InputStreamReader(airportDataStream));
      String l = null;
      List<AirportData> data = new ArrayList<AirportData>();
      while ((l = reader.readLine()) != null) {
        String[] split = l.split(",");
        String iata = (String) split[4].subSequence(1, split[4].length() - 1);
        String latitude = split[6];
        String longitude = split[7];
        AirportData airport = new AirportData();
        airport.setIata(iata);
        airport.setLatitude(Double.parseDouble(latitude));
        airport.setLongitude(Double.parseDouble(longitude));
        data.add(airport);
      }
      return data;
    } catch (Exception e) {
      throw e;
    } finally {
      if (airportDataStream != null) {
        airportDataStream.close();
      }
    }
  }

  public void upload(String fileName) throws Exception {
    try {
      List<AirportData> data = readData(fileName);
      for (AirportData airportData : data) {
        StringBuilder pathString = new StringBuilder();
        pathString.append("/airport/");
        pathString.append(airportData.getIata());
        pathString.append("/");
        pathString.append(airportData.getLatitude());
        pathString.append("/");
        pathString.append(airportData.getLongitude());
        WebTarget collectTarget = collect.path(pathString.toString());
        collectTarget.request().post(null);
      }
    } catch (Exception e) {
      throw new IllegalStateException("Could not upload airport data to webservice", e);
    }
  }

  public static void main(String args[]) throws Exception {
    AirportLoader al = new AirportLoader();
    al.upload(args[0]);
    System.exit(0);
  }
}
