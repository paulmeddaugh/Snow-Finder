package com.snowfinder.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class City {
	private String name;
	private String state;
	private String country;
	private boolean hasState;
	
	private String zip;
	private float distance;
	
	/** URL to scrape snowdepth from. */
	private String url;
	private Supplier<Float[]> getSnowDepth;
	private float snowDepth;
	
	/** Assigned to the current value of numberOfCities. */
	private int id;
	static private int numberOfCities;
	static private Map<Integer, City> cities = new HashMap<>();
	
	public City (String name, String country) {
		this.name = name;
		this.country = country;
		hasState = false;
		
		cities.put(numberOfCities, this);
		id = numberOfCities++;
	}
	
	public City (String name, String state, String country) {
		this(name, country);
		
		this.state = state;
		hasState = true;
	}
	
	public City (String zip, float distance, String name, String state) {
		this(name, state, "US");
		this.zip = zip;
		this.distance = distance;
	}
	
	public String getState() {
		return state;
	}
	
	public float getDistance() {
		return distance;
	}

	public String getName() {
		return name;
	}
	
	public Supplier<Float[]> getSnowDepthSupplier() {
		return this.getSnowDepth;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public static boolean hasName(String searchName) {
		for (int i = 0; i < cities.size(); i++) {
			if ( ((City) cities.get(i)).name.equals(searchName)) {
				return true;
			}
		}
		return false;
	}
	
	public static Supplier<Float[]> createFinderThread (City city) {
		
		return () -> {
			Elements ACrepositories = null;
			float snowDepth = -1;
			try {
				ACrepositories = Jsoup.connect(city.url)
						.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) "
								+ "Gecko/20100101 Firefox/25.0")
						.referrer("http://www.google.com")
						.timeout(60000)
						.followRedirects(true)
						.execute()
						.parse()
						.getElementsByClass("region-content-main");
						
			} catch (SocketTimeoutException ste) {
				return null;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Gets "_ �in"
			String depthString = ACrepositories.get(0)
					.getElementsByClass("test-false wu-unit wu-unit-snow ng-star-inserted")
					.get(0)
					.text();
			
			String tempString = ACrepositories.get(0)
					.getElementsByClass("test-true wu-unit wu-unit-temperature is-degree-visible ng-star-inserted")
					.get(0)
					.text();
			try {// �
				snowDepth = Float.parseFloat(depthString.replaceAll(" °in", ""));
			} catch (NumberFormatException nfe) {
				System.err.print("Could not parse " + city.name + ", " + city.state + " "
						+ city.country + ": " + depthString + "\n");
				snowDepth = 0f;
			}
			
			float temp = Float.parseFloat(tempString.replaceAll(" °F", ""));
			
			return new Float[] { snowDepth, temp };
		};
	}
	
	public static City[] loadMajorCities () {
		
		InputStream cL = City.class.getResourceAsStream("/com/snowfinder/resources/List of Cities.txt");
		InputStream saL = City.class.getResourceAsStream("/com/snowfinder/resources/state_Abbreviations.txt");
		
		String abbList = new String();
		
		ArrayList<String> names = new ArrayList<>();
		ArrayList<String> states = new ArrayList<>();
		ArrayList<String> countries = new ArrayList<>();
		
		try {
			BufferedReader cityReader = new BufferedReader(new InputStreamReader(cL));
			String line;
			int numberOfLines = 0;
			while ((line = cityReader.readLine()) != null) {
				int lastTabIndex = 0;
				String cityNames = line.substring(
				//		starting index from \t + 1,
						line.indexOf('\t') + 1, 
				//		ending at next instance of indexOf(\t) - 1 (2nd param takes the starting index)
						lastTabIndex = line.indexOf('\t', line.indexOf("\t") + 1));
				String state = line.substring(
						line.indexOf('\t', line.indexOf(cityNames)) + 1,
						lastTabIndex = line.indexOf('\t', lastTabIndex + 1));
				names.add(cityNames);
				states.add(state);
				numberOfLines++;
				if (numberOfLines <= 200) {
					countries.add("us");
				} else {
					String country = line.substring(
							line.indexOf('\t', line.indexOf(cityNames)) + 1,
							line.indexOf('\t', lastTabIndex + 1));
				}
			}
			cityReader.close();
			
			BufferedReader abbReader = new BufferedReader(new InputStreamReader(saL));
			String abbLine = " ";
			while ((abbLine = abbReader.readLine()) != null) {
				abbList = abbList + abbLine + "\n";
			}
			abbReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//changes all states to their abbreviations
		for (int i = 0; i < states.size(); i++) {
			String state = states.get(i);
			if (abbList.contains(state)) {
				states.set(i, abbList.substring(
						abbList.indexOf(state) + state.length() + 3,
						abbList.indexOf(state) + state.length() + 5));
			}
		}
		
		City[] cities = new City[names.size()];
		
		//automatically add US as country to first 200 cities listed
		for (int i = 0; i < 200; i++) {
			cities[i] = new City(names.get(i), states.get(i), countries.get(i));
		}
		cities[200] = new City(names.get(200), "Japan", "Japan");
		
		createFinderThreads(cities);
		
		return cities;
	}
	
	public static void createFinderThreads (City[] cities) {
		for (int i = 0; i < cities.length; i++) {
			if (cities[i].state != null) {
				cities[i].url = "https://www.wunderground.com/weather/"
					+ cities[i].country.toLowerCase()
					+ "/" + cities[i].state.toLowerCase() 
					+ "/" + cities[i].name.toLowerCase();
			} else {
				cities[i].url = "https://www.wunderground.com/weather/"
					+ cities[i].country.toLowerCase()
					+ "/" + cities[i].name.toLowerCase();
			}
			cities[i].getSnowDepth = createFinderThread(cities[i]);
		}
	}
	
	/**
	 * Parses City objects from a JSON response, including loading SwingWorkers to find
	 * their snow depth in real time.
	 * 
	 * @param json The JSON object to parse as a String.
	 * @return An array of City objects.
	 */
	public static City[] parseFromJSON(String json) throws JSONException {
		
		City[] cities = new City[0];
		
		if (json == null) {
			throw new IllegalArgumentException("json must not be null");
		}
		
		JSONObject obj = new JSONObject(json);
		JSONArray zipArr = obj.getJSONArray("zip_codes");
		
		ArrayList<City> citiesList = new ArrayList<>();
		
		for (Object jObj : zipArr) {
			JSONObject zipObj = (JSONObject) jObj;
			
			String name = zipObj.getString("city");
			if (!City.hasName(name)) {
				City city = new City(
					zipObj.getString("zip_code"),
					zipObj.getFloat("distance"),
					name,
					zipObj.getString("state")
				);
				
				citiesList.add(city);
			}
		}
		
		cities = citiesList.toArray(cities);
		City.createFinderThreads(cities);
		
		return cities;
	}
}
