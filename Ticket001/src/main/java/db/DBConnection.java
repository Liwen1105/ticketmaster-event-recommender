package db;

import java.util.List;
import java.util.Set;

import data.Event;

public interface DBConnection {
	public void close();
	public void setFavoriteEvents(String userId, List<String> eventIds);
	public void unsetFavoriteEvents(String userId, List<String> eventIds);
	public Set<String> getFavoriteEventIds(String userId);
	public Set<Event> getFavoriteEvents(String userId);
	public Set<String> getCategories(String eventId);
	public List<Event> searchEvents (double lat, double lon, String keyword);
	public void saveEvent(Event event);
	public String getFullname(String userId);
	public boolean verifyLogin(String userId, String password);
	public boolean registerUser(String userId, String password, String firstname, String lastname);
}
