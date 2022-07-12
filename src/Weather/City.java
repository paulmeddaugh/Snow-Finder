package Weather;

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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class City {
	String name;
	String state;
	String country;
	boolean hasState;
	
	String zip;
	float distance;
	
	/** URL to scrape snowdepth from. */
	String url;
	Thread loadSnowDepth;
	float snowDepth;
	
	/** Assigned to the current value of numberOfCities. */
	int id;
	static int numberOfCities;
	static Map<Integer, City> cities = new HashMap<>();
	
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
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public static boolean hasName(String searchName) {
		for (int i = 0; i < cities.size(); i++) {
			if ( ((City) cities.get(i)).name.equals(searchName)) {
				return true;
			}
		}
		return false;
	}
	
	public static void loadSnowInfo (City[] cities) {
		System.out.println("\nLoading...");
		
		ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(25);
		for (int i = 0; i < cities.length; i++) {
			pool.schedule(cities[i].loadSnowDepth, 0, TimeUnit.SECONDS);
		}
		pool.shutdown();
	}
	
	public static Thread createFinderThread (City city) {
		
			Thread t = new Thread(() -> {
				
				RUNNING: {
					Elements ACrepositories = null;
					try {
						ACrepositories = Jsoup.connect(city.url)
								.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) "
										+ "Gecko/20100101 Firefox/25.0")
								.referrer("http://www.google.com")
								.timeout(60000)
								.followRedirects(true)
								.execute()
								.parse()
								.getElementsByClass("data-module additional-conditions");
					} catch (SocketTimeoutException ste) {
						System.out.println("Connection timed out for "
								+ city.name);
						break RUNNING;
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					for (Element repository : ACrepositories) {
						// Gets "_ °in"
						String depthString = repository.getElementsByClass("test-false wu-unit"
								+ " wu-unit-snow ng-star-inserted").text();
						
						float snowDepth = Float.parseFloat(depthString.replaceAll(" °in", ""));
						
						if (snowDepth > 0) { // Found snow
							System.out.println("X - " + city.name + ", " + city.state 
									+ " snow depth: " + depthString + " "
									+ (city.distance != 0 ? " (" + city.distance 
											+ " miles away) - " : "- ")
									+ (int) (System.currentTimeMillis()
											- GUI.getTimeBegan()));
						} else {
							System.out.println("No snow in " 
									+ city.name + ", " + city.state + " "
									+ (city.distance != 0 ? " (" + city.distance 
											+ " miles away) - " : "- ")
									+ (int) (System.currentTimeMillis()
											- GUI.getTimeBegan()));
						}
					}
				}
			});
			return t;

			// 2 HTMLUnit Method (library in project file, not in build path)
			
			/*WebClient webClient = new WebClient();

		    // Get the first page
		    final HtmlPage page1 = webClient.getPage("http://some_url");

		    // Get the form that we are dealing with and within that form, 
		    // find the submit button and the field that we want to change.
		    final HtmlForm form = page1.getFormByName("myform");

		    final HtmlSubmitInput button = form.getInputByName("submitbutton");
		    final HtmlTextInput textField = form.getInputByName("userid");

		    // Change the value of the text field
		    textField.setValueAttribute("root");

		    // Now submit the form by clicking the button and get back the second page.
		    final HtmlPage page2 = button.click();

		    webClient.closeAllWindows();*/
			
			// 3 URL Connection Method
			
			/*URL urlLit = new URL(url);
			URLConnection con = urlLit.openConnection();
			InputStream is = con.getInputStream();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

	        String line = null;

	        // read each line and write to System.out
	        while ((line = br.readLine()) != null) {
	        	if (line.contains("Snow Depth")) {
	        		if (line.contains("<span _ngcontent-app-root-c121=\"\" class=\"wu-value wu-value-to\">"))
	        			System.out.print("true");
	        	}
	        }*/
			
			
			// 4 API Integration
		
	}
	
	public static City[] loadMajorCities () {
		
		InputStream cL = City.class.getResourceAsStream("/txts/List of Cities.txt");
		InputStream saL = City.class.getResourceAsStream("/txts/state_Abbreviations.txt");
		
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
		cities[200] = new City(names.get(200), "jp");
		
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
			cities[i].loadSnowDepth = createFinderThread(cities[i]);
		}
	}
}
