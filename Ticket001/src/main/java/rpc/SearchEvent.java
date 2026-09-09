package rpc;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import data.Event;
import db.DBConnection;
import db.DBConnectionFactory;
import ticketMaster.TicketMasterClient;

/**
 * Servlet implementation class SearchEvent
 */
@WebServlet("/search")
public class SearchEvent extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchEvent() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// response.getWriter().append("Served at: ").append(request.getContextPath());
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return ;
		}
		String userId = session.getAttribute("user_id").toString();
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		String keyword = request.getParameter("keyword");
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			List<Event> events = connection.searchEvents(lat, lon, keyword);
			Set<String> favoritedEventIds = connection.getFavoriteEventIds(userId);
			JSONArray array = new JSONArray();
			for (Event event : events) {
				JSONObject obj = event.toJSONObject();
				obj.put("favorite", favoritedEventIds.contains(event.getEventId()));
				array.put(obj);
			}
			RpcHelper.writeJsonArray(response, array);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
