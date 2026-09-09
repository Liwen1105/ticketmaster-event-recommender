package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import data.Event;

// Recommendation based on geo distance and similar categories.
public class GeoRecommendation {

  public List<Event> recommendEvents(String userId, double lat, double lon) {
			List<Event> recommendedEvents = new ArrayList<>();

			// Step 1, get all favorited eventids
			DBConnection connection = DBConnectionFactory.getConnection();
			Set<String> favoritedEventIds = connection.getFavoriteEventIds(userId);

			// Step 2, get all categories,  sort by count
			// {"sports": 5, "music": 3, "art": 2}
			Map<String, Integer> allCategories = new HashMap<>();
			for (String eventId : favoritedEventIds) {
				Set<String> categories = connection.getCategories(eventId);
				for (String category : categories) {
					allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
				}
			}
			List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
			Collections.sort(categoryList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
				return Integer.compare(e2.getValue(), e1.getValue());
			});
			
			// Step 3, search based on category, filter out favorite items
			Set<String> visitedEventIds = new HashSet<>();
			for (Entry<String, Integer> category : categoryList) {
				List<Event> events = connection.searchEvents(lat, lon, category.getKey());
				
				for (Event event : events) {
					if (!favoritedEventIds.contains(event.getEventId()) && !visitedEventIds.contains(event.getEventId())) {
						recommendedEvents.add(event);
						visitedEventIds.add(event.getEventId());
					}
				}
			}
			
			connection.close();
			return recommendedEvents;
  }
}
