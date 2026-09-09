package ticketMaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import data.Event;
import data.Event.EventBuilder;

public class TicketMasterClient {
	private static final String HOST = "https://app.ticketmaster.com";
	private static final String ENDPOINT = "/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "event";
	private static final String API_KEY = "mO1Ugoter6j3PO1AhfW2IhuzOo8e5DeT";

	public List<Event> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8"); // special symbol
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String query = String.format("apikey=%s&latlong=%s,%s&keyword=%s&radis=%s", 
				API_KEY, lat, lon, keyword, 50);
		String url = HOST + ENDPOINT + "?" + query;
		StringBuilder responseBody = new StringBuilder();

			// Create a URL Connection instance that represents a connection to the remote
			// object referred to by the URL, The HttpUrlConnection class allows us to
			// perform basic HTTP requests.
			try {
				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
				connection.setRequestMethod("GET");
				
				// Send request and get response at first. Then, get the status code from an HTTP 
				// response messsage. To execute the request we can use the getResponseCode(), 
				// connect(), getInputStream() or getOutputStream() methods.
				int responseCode = connection.getResponseCode();
				System.out.println("Sending requests to url:" + url);
				System.out.println("Response code:" + responseCode);
				
				if (responseCode != 200) {
					return new ArrayList<>();
				}
				
				// connection.getInputStream() can get response body.
				// Create a BufferedReader to help read text from a stream, line by line.
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					responseBody.append(line);
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				//Extract events array only.
				JSONObject obj = new JSONObject(responseBody.toString());
				if (!obj.isNull("_embedded")) {
					JSONObject embedded = obj.getJSONObject("_embedded");
					return getEventList(embedded.getJSONArray("events"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return new ArrayList<>();
	}
	
	private List<Event> getEventList(JSONArray events) throws JSONException {
		List<Event> eventList = new ArrayList<>();
		for (int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			
			EventBuilder builder = new EventBuilder();
			if (!event.isNull("id")) {
				builder.setEventId(event.getString("id"));
			}
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			
			builder.setAddress(getAddress(event));
			builder.setCategories(getCategories(event));
			builder.setImageUrl(getImageUrl(event));
			
			eventList.add(builder.build());
		}
		return eventList;
	}
	
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				for (int i = 0; i < venues.length(); ++i) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder builder = new StringBuilder();
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if (!address.isNull("line1")) {
							builder.append(address.getString("line1"));
						}
						
						if (!address.isNull("line2")) {
							builder.append(",");
							builder.append(address.getString("line2"));
						}
						
						if (!address.isNull("line3")) {
							builder.append(",");
							builder.append(address.getString("line3"));
						}
					}
					
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						builder.append(",");
						builder.append(city.getString("name"));
					}
					
					String result = builder.toString();
					if (!result.isEmpty()) {
						return result;
					}
				}
			}
		}
		return "";	
	}

	
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray array = event.getJSONArray("images");
			for (int i = 0; i < array.length(); i++) {
				JSONObject image = array.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}

	
	private Set<String> getCategories(JSONObject event) throws JSONException {		
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); ++i) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}
	

	
	
	
	public static void main(String[] args) {
		TicketMasterClient client = new TicketMasterClient();
		List<Event> events = client.search(21.40,-130.89,null);
		
		for (Event eventsample : events) {
			System.out.println(eventsample.toJSONObject());
		}
	}
}

