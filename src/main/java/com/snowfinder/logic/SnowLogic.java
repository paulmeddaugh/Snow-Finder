package com.snowfinder.logic;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.json.JSONException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

public class SnowLogic {
	
	private static ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(25);
	static City[] majorCities = City.loadMajorCities();
	
	static void cleanup () {
		pool.shutdown();
		pool = new ScheduledThreadPoolExecutor(25);
	}
	
	/**
	 * Displays the snow depth of an array of cities within the GUI textArea. Often,
	 * the array is large, so it uses a ScheduledThreadPoolExecutor with SwingWorkers
	 * to display the results as they come.
	 * 
	 * @param cities The array of City objects to find the current snow depth in.
	 */
	public static void loadSnowDepth (City[] cities, Consumer<String> displayLambda) {
		
		displayLambda.accept(cities.length + " results");
		
		for (City city : cities) {
			
			SwingWorker<Float[], Void> sw = new SwingWorker<Float[], Void>() {

				@Override
				protected Float[] doInBackground() throws Exception {
					return city.getSnowDepthSupplier().get();
				}

				@Override
				protected void done() {
					float snowDepth = -1f;
					float temp = Float.MAX_VALUE;
					try {
						Object result = get();
						Float[] results = (result != null) ? (Float[]) result : null;
						
						if (results == null) {
							displayLambda.accept("Connection timed out getting the snow "
								+ "depth in " + city.getName() + ", " + city.getState());
						} else {
							snowDepth = results[0];
							temp = results[1];
							final boolean zipAtCity = city.getDistance() == 0;
							
							// Displays results in GUI
							displayLambda.accept((snowDepth > 0 ? "X," : "O,") 
								+ city.getName() + "," + city.getState()
								+ "," + snowDepth + " °in,"
								+ temp + " °F"
								+ (!zipAtCity ? "," + city.getDistance() : ""));
						}
						
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
			};
			pool.schedule(sw, 0, TimeUnit.SECONDS);
		}
		pool.shutdown();
	}
	
	public static void loadMajorCities(Consumer<String> displayLambda) {
		
		cleanup();
		loadSnowDepth(majorCities, displayLambda);
		
	}

	public static void loadFromZip (Integer zip, float distance, Consumer<String> displayLambda) {
		
		cleanup();
		
		final String endPoint = "https://www.zipcodeapi.com/rest";
		final String apiKey = "4RIOHHuvRgY7Z7Cbdi6xJ91KXVKqJtkYLGKhqUY3PK5MyKl51y1J3uaM3rpQIyJ6";
		final String radius = "radius.json";
		final String units = "mile";
		
		StringBuilder query = new StringBuilder()
				.append("/").append(apiKey)
				.append("/").append(radius)
				.append("/").append(zip)
				.append("/").append(distance)
				.append("/").append(units);
		
		String responseJson = "";
		City[] cities = null;
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
			
			cities = City.parseFromJSON(responseJson);
			
		} catch (HttpStatusException hse) {
			if (hse.getStatusCode() == 400) {
				throw new ZipCodeAPIException("The request format was not correct.");
			} else if (hse.getStatusCode() == 401) {
				throw new ZipCodeAPIException("The zip code API key is invalid. The "
					+ "application cannot find zip codes. I apologize.");
			} else if (hse.getStatusCode() == 404) {
				throw new ZipCodeAPIException("Zip code could not be found");
			} else if (hse.getStatusCode() == 429) {
				throw new ZipCodeAPIException("The 10 uses-per-hour zip code requests limit "
					+ "has been reached. I was not expecting people quite so curious "
					+ "as you, and I apologize.");
			}
			hse.printStackTrace();
		} catch (IOException ioe) {
			
			throw new ZipCodeAPIException("There appears to be no internet here. "
					+ "I apologize.");
		} catch (JSONException je) {
			throw new ZipCodeAPIException(responseJson);
		}
		
		if (cities == null || cities.length == 0) {
			displayLambda.accept("No cities found within " + distance + " miles of zip code " + zip);
		} else {
			loadSnowDepth(cities, displayLambda);
		}
	}
	
	/**
	 * The API only has 10 calls per day, so when not needed, the program can run with
	 * a sample JSON.
	 */
	static void loadFromSampleZip () {
		City[] cities = City.parseFromJSON(loadSampleJSON());
		loadSnowDepth(cities, (log) -> {});
	}
	
	/**
	 * @return A previous JSON string of cities near a zip code from the API.
	 */
	static String loadSampleJSON() {
		
		InputStream sampleStream = SnowLogic.class.getResourceAsStream("/resources/sampleAPIResponse.txt");
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
}
