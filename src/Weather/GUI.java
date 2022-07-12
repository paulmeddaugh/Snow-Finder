package Weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

public class GUI {
	
	static boolean test = true;
	static long timeBegan;

	public static void main(String[] args) {
		
		System.out.println("SnowFinder\n");
		
			System.out.println("1) Get the snow depth in cities near a zip code.");
			System.out.println("2) Get the snow depth in major US cities.");
			System.out.println("3) Exit.");
			System.out.print("What number would you like to do? ");
			
			Scanner sc = new Scanner(System.in);
			int response = sc.nextInt();
			
			switch (response) {
				case 1: 
					String zip = "";
					while (true) {
					System.out.print("Please enter the zip code: ");
					zip = sc.next();
						if (zip.matches("[0-9]{5}")) {
							break;
						} else {
							System.out.println("What do you take me for");
						}
					}
					System.out.print("Please enter the distance in miles to "
							+ "search within: ");
					timeBegan = System.currentTimeMillis();
					loadFromZip(zip, sc.nextFloat());
					break;
				case 2: 
					timeBegan = System.currentTimeMillis();
					loadMajorCities();
					break;
				case 3: break;
				default: System.out.println("Answer must be one of the listed options in "
						+ "parenthesis");
			}
			sc.close();
	}
	
	public static void loadMajorCities() {
		
		City[] cities = City.loadMajorCities();
		City.loadSnowInfo(cities);
		
	}

	public static void loadFromZip (String zip, float distance) {
		
		if (!zip.matches("[0-9]{5}")) {
			throw new IllegalArgumentException("Must enter a zip code");
		} else if (distance <= 0 || distance > 500) {
			throw new IllegalArgumentException("distance must be between a number greater"
					+ "than 0 and 500");
		}
		
		final String endPoint = "https://www.zipcodeapi.com/rest";
		final String apiKey = "cvKqpR0uNCajF6A5PP6WJZCZTYTwX08TVq9pwUVfC0IzTpx9RuWGp3vgGyQJdJnV";
		final String radius = "radius.json";
		final String units = "mile";
		
		StringBuilder query = new StringBuilder()
				.append("/").append(apiKey)
				.append("/").append(radius)
				.append("/").append(zip)
				.append("/").append(distance)
				.append("/").append(units);
		
		String responseJson = "";
		try {
			responseJson = Jsoup.connect(endPoint + query.toString())
				.ignoreContentType(true)
				.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) "
						+ "Gecko/20100101 Firefox/25.0")
				.referrer("http://www.google.com")
				.timeout(60000)
				.followRedirects(true)
				.get()
				.text();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		City[] cities = parseFromJSON(responseJson);
		City.createFinderThreads(cities);
		City.loadSnowInfo(cities);
	}
	
	public static void loadCitiesFromZip (String jsonSample) {
		City[] cities = parseFromJSON(jsonSample);
		City.createFinderThreads(cities);
		City.loadSnowInfo(cities);
	}
	
	public static City[] parseFromJSON(String json) {
		
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
				City city = new City(zipObj.getString("zip_code"),
						zipObj.getFloat("distance"),
						name,
						zipObj.getString("state"));
				
				citiesList.add(city);
			}
		}
		
		cities = citiesList.toArray(cities);
		
		return cities;
	}
	
	public static String loadSampleJSON() {
		
		InputStream sampleStream = GUI.class.getResourceAsStream("/txts/sampleAPIResponse.txt");
		StringBuilder sb = new StringBuilder();
		
		try {
			BufferedReader sampleReader = new BufferedReader(new InputStreamReader(sampleStream));
			String line;
			while ((line = sampleReader.readLine()) != null) {
				sb.append(line + "\n");
			}
			sampleReader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		return sb.toString();
	}

	public static long getTimeBegan() {
		return timeBegan;
	}
	public static void setTimeBegan(long timeBegan) {
		GUI.timeBegan = timeBegan;
	}
}
