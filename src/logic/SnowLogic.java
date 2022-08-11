package logic;

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

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.json.JSONException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import renderer.GUI;

public class SnowLogic {
	
	private static GUI gui;
	private static long timeBegan;
	
	private static ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(25);
	
	public static void main(String[] args) {
		
		gui = new GUI();
		gui.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				pool.shutdown();
			}
		});
		
		timeBegan = System.currentTimeMillis();
		if (gui.getSearchingMethod() == GUI.NEAR_ZIP_CODE) {
			loadFromZip(gui.getZip(), gui.getMileRadius());
		} else {
			loadMajorCities();
		}
		
		gui.addGoAgainListeners(() -> {
			gui.dispose();
			pool = new ScheduledThreadPoolExecutor(25);
			
			main(null);
		});
	}
	
	/**
	 * Displays the snow depth of an array of cities within the GUI textArea. Often,
	 * the array is large, so it uses a ScheduledThreadPoolExecutor with SwingWorkers
	 * to display the results as they come.
	 * 
	 * @param cities The array of City objects to find the current snow depth in.
	 */
	public static void loadSnowDepth (City[] cities) {
		
		gui.appendText(cities.length + " results");
		
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
						Float[] results = (get() != null) ? get() : null;
						
						if (results == null) {
							gui.setText(gui.getText() + "Connection timed out getting " 
								+ "the snow depth in " + city.getName());
						} else {
							snowDepth = results[0];
							temp = results[1];
							
							// Displays results in GUI
							if (snowDepth > 0) {
								gui.appendText("X - Snow found in " 
									+ city.getName() + ", " + city.getState()
									+ " snow depth: " + snowDepth + "°in "
									+ (city.getDistance() != 0 ? " (" + city.getDistance() 
											+ " miles away" : "") + ", " + temp + "°F)");
							} else {
								gui.appendText("No snow in " 
									+ city.getName() + ", " + city.getState() + " "
									+ (city.getDistance() != 0 ? " (" + city.getDistance() 
											+ " miles away" : "") + ", " + temp + "°F)");
							}
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
	
	public static void loadMajorCities() {
		
		City[] cities = City.loadMajorCities();
		loadSnowDepth(cities);
		
	}

	public static void loadFromZip (Integer zip, float distance) {
		
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
				JOptionPane.showInternalMessageDialog(null, "The request format was not "
					+ "correct.", "Error", JOptionPane.INFORMATION_MESSAGE);
			} else if (hse.getStatusCode() == 401) {
				JOptionPane.showInternalMessageDialog(null, "The API key is invalid.",
						"Error", JOptionPane.INFORMATION_MESSAGE);
				System.exit(0);
			} else if (hse.getStatusCode() == 404) {
				JOptionPane.showInternalMessageDialog(null, "Zip code could not be found.",
						"Error", JOptionPane.INFORMATION_MESSAGE);
			} else if (hse.getStatusCode() == 429) {
				JOptionPane.showInternalMessageDialog(null, "The 10 uses-per-hour limit "
					+ "has been reached.", "Error", JOptionPane.INFORMATION_MESSAGE);
				System.exit(0);
			}
			hse.printStackTrace();
		} catch (IOException ioe) {
			
			JOptionPane.showInternalMessageDialog(null, "Zip code could not be found.",
					"Error", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		} catch (JSONException je) {
			gui.setText(responseJson);
		}
		
		if (cities == null || cities.length == 0) {
			gui.setText("No cities found within " + distance + " miles of zip code " + zip);
		} else {
			loadSnowDepth(cities);
		}
	}
	
	/**
	 * The API only has 10 calls per day, so when not needed, the program can run with
	 * a sample JSON.
	 */
	public static void loadFromSampleZip () {
		City[] cities = City.parseFromJSON(loadSampleJSON());
		loadSnowDepth(cities);
	}
	
	/**
	 * @return A previous JSON string of cities near a zip code from the API.
	 */
	public static String loadSampleJSON() {
		
		InputStream sampleStream = GUI.class.getResourceAsStream("/resources/sampleAPIResponse.txt");
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
}
