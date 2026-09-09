package db.mysql;

import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;

public class MySQLTableCreation {
	// Run this as Java application to reset db schema.
	public static void main(String[] args) {
		try {
			System.out.println("Connecting to " + MySQLUtil.URL);
			Connection conn = DriverManager.getConnection(MySQLUtil.URL);
			
			if (conn == null) {
				System.err.println("DB connection failed");
				return;
			}
			
			Statement statement = conn.createStatement();
			String sql = "DROP TABLE IF EXISTS categories";
			statement.executeUpdate(sql);
			
			sql = "DROP TABLE IF EXISTS history";
			statement.executeUpdate(sql);
			
			sql = "DROP TABLE IF EXISTS events";
			statement.executeUpdate(sql);
			
			sql = "DROP TABLE IF EXISTS users";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE events ("
					+ "event_id VARCHAR(255) NOT NULL,"
					+ "name VARCHAR(255),"
					+ "address VARCHAR(255),"
					+ "image_url VARCHAR(255),"
					+ "url VARCHAR(255),"
					+ "distance FLOAT,"
					+ "PRIMARY KEY (event_id)"
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE users ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "password VARCHAR(255) NOT NULL,"
					+ "first_name VARCHAR(255),"
					+ "last_name VARCHAR(255),"
					+ "PRIMARY KEY (user_id)"
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE categories ("
					+ "event_id VARCHAR(255) NOT NULL,"
					+ "category VARCHAR(255) NOT NULL,"
					+ "PRIMARY KEY (event_id, category),"
					+ "FOREIGN KEY (event_id) REFERENCES events(event_id)"
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE history ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "event_id VARCHAR(255) NOT NULL,"
					+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+ "PRIMARY KEY (user_id, event_id),"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id),"
					+ "FOREIGN KEY (event_id) REFERENCES events(event_id)"
					+ ")";
			statement.executeUpdate(sql);

			sql = "INSERT INTO users VALUES('12943', '52847', 'Mical', 'Su')";
			
			statement.executeUpdate(sql);

			conn.close();
			System.out.println("Import done successfully");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}