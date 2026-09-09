package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import data.Event;
import data.Event.EventBuilder;
import db.DBConnection;
import ticketMaster.TicketMasterClient;

public class MySQLConnection implements DBConnection {
	
	private Connection conn;
	
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void setFavoriteEvents(String userId, List<String> eventIds) {
		if (conn == null) {
			System.err.println("DB connection failled");
			return ;
		}
		
		try {
			String sql = "INSERT IGNORE INTO history(user_id, event_id) VALUES (?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for (String eventId : eventIds) {
				ps.setString(2, eventId);
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsetFavoriteEvents(String userId, List<String> eventIds) {
		if (conn == null) {
			System.err.println("DB connection failled");
			return ;

	}
	
	try {
		String sql = "DELETE FROM history WHERE user_id = ? AND event_Id=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, userId);
		for (String eventId : eventIds) {
			ps.setString(2, eventId);
			ps.execute();
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	}

	@Override
	public Set<String> getFavoriteEventIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<String> favoriteEventIds = new HashSet<>();
		try {
			String sql = "SELECT event_id FROM history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String eventId = rs.getString("event_id");
				favoriteEventIds.add(eventId);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteEventIds;
	}

	@Override
	public Set<Event> getFavoriteEvents(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		
		Set<Event> favoriteEvents = new HashSet<>();
		Set<String> eventIds = getFavoriteEventIds(userId);
		
		try {
			String sql = "SELECT * FROM events WHERE event_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			for (String eventId : eventIds) {
				statement.setString(1, eventId);
				ResultSet rs = statement.executeQuery();
				EventBuilder builder = new EventBuilder();
				
				while (rs.next()) {
					builder.setEventId(rs.getString("event_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories("event_id"));
					builder.setDistance(rs.getDouble("distance"));
					
					favoriteEvents.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteEvents;
	}

	@Override
	public Set<String> getCategories(String eventId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category from categories WHERE event_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, eventId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String category = rs.getString("category");
				categories.add(category);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return categories;
	}

	@Override
	public List<Event> searchEvents(double lat, double lon, String keyword) {
		TicketMasterClient client = new TicketMasterClient();
		List <Event> events = client.search(lat, lon, keyword);
		for (Event event : events) {
			saveEvent(event);
		}
		
		return events;
	}
	

	@Override
	public void saveEvent(Event event) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		
		try {
			String sql = "INSERT IGNORE INTO events VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, event.getEventId());
			ps.setString(2, event.getName());
			ps.setString(3, event.getAddress());
			ps.setString(4, event.getImageUrl());
			ps.setString(5, event.getUrl());
			ps.setDouble(6, event.getDistance());
			ps.execute();
			
			sql = "INSERT IGNORE INTO categories VALUES (?, ?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, event.getEventId());
			for (String category : event.getCategories()) {
				ps.setString(2, category);
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		
		String name = "";
		try {
			String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				name = rs.getString("first_name") + " " + rs.getString("last_name");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return name;
		}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			return false;
		}
		
		try {
			String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			stmt.setString(2, password);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	@Override
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		try {
			String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, password);
			ps.setString(3, firstname);
			ps.setString(4, lastname);
			
			return ps.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
