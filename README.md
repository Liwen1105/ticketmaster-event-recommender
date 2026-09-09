# Ticketmaster Event Recommender

A full-stack event discovery and recommendation web application built with Java Servlets, MySQL, HTML, CSS, and JavaScript. The application integrates with the Ticketmaster Discovery API to help users search for events, save favorites, and receive personalized recommendations based on their interests and location.

## Features

- Search for nearby events through the Ticketmaster Discovery API
- Filter event results using location information
- Create an account and sign in securely
- Maintain authenticated user sessions
- Save and remove favorite events
- View a personalized favorites history
- Generate content-based event recommendations
- Store users, events, categories, and favorites in MySQL
- Communicate between the frontend and backend using JSON APIs
- Deploy the application with Apache Tomcat on AWS EC2

## Technology Stack

| Area | Technologies |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java 21, Java Servlets, Apache Tomcat 9 |
| Database | MySQL, JDBC |
| External API | Ticketmaster Discovery API |
| API testing | Postman |
| Cloud deployment | AWS EC2 |
| Development environment | Eclipse IDE |

## How It Works

1. A user creates an account or signs in.
2. The frontend sends an event-search request to a Java Servlet.
3. The backend requests matching events from the Ticketmaster API.
4. Relevant event details are transformed into application objects and returned as JSON.
5. Search results and user favorites are stored in MySQL.
6. The recommendation service analyzes the categories in a user's favorite-event history.
7. The application recommends related events near the user's selected location.

## Recommendation Approach

The application uses **content-based filtering**. It identifies categories associated with events a user has saved and searches for new events with similar characteristics. This approach personalizes results from the user's own activity without requiring ratings or behavior from other users.

## Main API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/search` | Search for events near a location |
| `GET` | `/recommendation` | Return personalized event recommendations |
| `GET` | `/history` | Retrieve a user's favorite events |
| `POST` | `/history` | Add an event to favorites |
| `DELETE` | `/history` | Remove an event from favorites |
| `GET` | `/login` | Check the current login session |
| `POST` | `/login` | Authenticate a user |
| `GET` | `/logout` | End the current session |
| `POST` | `/register` | Create a new user account |

Endpoint paths may vary slightly depending on the final servlet mappings in the source code.

## Database Design

The MySQL database contains four main tables:

- `users` stores account and profile information.
- `items` stores event details returned by Ticketmaster.
- `categories` connects events with their categories.
- `history` connects users with their saved events.

Primary and foreign keys maintain relationships between users, events, categories, and favorites.

## Project Structure

```text
ticketmaster-event-recommender/
├── src/
│   └── main/
│       ├── java/
│       │   ├── db/              # Database interfaces and factory
│       │   ├── db/mysql/        # MySQL implementation
│       │   ├── entity/          # Event data models
│       │   ├── external/        # Ticketmaster API client
│       │   ├── recommendation/  # Recommendation logic
│       │   └── rpc/             # Java Servlet endpoints
│       └── webapp/
│           ├── scripts/         # Frontend JavaScript
│           ├── styles/          # CSS styles
│           ├── WEB-INF/         # Web configuration and libraries
│           └── index.html       # Application entry page
└── README.md
```

Adjust this tree if your final folder or package names are different.

## Local Setup

### Prerequisites

Install the following tools before running the project:

- Java 21
- Eclipse IDE for Enterprise Java and Web Developers
- Apache Tomcat 9
- MySQL Server
- MySQL Connector/J
- Postman, optional for API testing
- A Ticketmaster developer API key

### 1. Clone the repository

```bash
git clone https://github.com/Liwen1105/ticketmaster-event-recommender.git
cd ticketmaster-event-recommender
```

### 2. Configure the database

Create a MySQL database and the required `users`, `items`, `categories`, and `history` tables. Then configure the application with your local database URL, username, and password.

Do not commit database passwords to GitHub. Store sensitive settings in environment variables or in a local configuration file excluded by `.gitignore`.

### 3. Configure the Ticketmaster API

Create an API key through the Ticketmaster developer portal and make it available to the backend.

```bash
export TICKETMASTER_API_KEY="your_api_key"
```

Make sure the API client reads this environment variable. Never commit the real key to the repository.

### 4. Configure Tomcat

1. Import the project into Eclipse.
2. Add Apache Tomcat 9 as the server runtime.
3. Add MySQL Connector/J and the required JSON library to the project dependencies.
4. Add the web application to the Tomcat server.
5. Start or restart Tomcat.

### 5. Run the application

Open the application in a browser:

```text
http://localhost:8080/Web/
```

If your deployed context name is different from `Web`, replace it in the URL.

## Testing

Postman can be used to test the servlet endpoints independently. A typical workflow is:

1. Register a new user.
2. Log in and preserve the session cookie.
3. Search for events using latitude and longitude.
4. Add or remove favorite events.
5. Retrieve the user's favorites.
6. Request personalized recommendations.
7. Log out and verify that protected resources are no longer accessible.

## Deployment

The application can be exported as a WAR file from Eclipse and deployed to Apache Tomcat on an AWS EC2 instance. The server requires Java, MySQL, and Tomcat, along with properly configured firewall rules.

For a production deployment:

- Restrict inbound ports instead of exposing Tomcat broadly.
- Use HTTPS through a reverse proxy.
- Store secrets outside the source code.
- Use a dedicated database account with limited privileges.
- Replace development credentials and disable unnecessary services.

## What I Learned

- Building responsive pages with HTML, CSS, and JavaScript
- Designing REST-style endpoints with Java Servlets
- Processing HTTP requests and returning JSON responses
- Integrating a third-party API into a backend service
- Applying object-oriented design and the builder pattern
- Designing relational database tables and using JDBC
- Implementing registration, login, logout, and sessions
- Building a content-based recommendation workflow
- Packaging and deploying a Java web application to AWS EC2

## Future Improvements

- Hash passwords with a modern password-hashing algorithm
- Add stronger input validation and structured error handling
- Move all secrets and environment-specific settings out of the source code
- Add automated unit and integration tests
- Introduce pagination and additional event filters
- Improve recommendations with user feedback and hybrid filtering
- Containerize the application with Docker
- Add a responsive, accessible frontend design

## Author

**Liwen Lydia Chen**

## Acknowledgment

Event information is provided through the Ticketmaster Discovery API. Ticketmaster trademarks and data belong to their respective owners.

## Disclaimer

This project was created for educational and portfolio purposes. Do not upload course lesson documents, API keys, passwords, private keys, or other restricted materials to this public repository.
