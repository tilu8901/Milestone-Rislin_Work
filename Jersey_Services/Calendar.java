package Jersey_Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import utilityClasses.Database;
import utilityClasses.utility;
import Event_Functions.Time_Functions;

@ Path ( "calendar" ) public class Calendar {
	@ Context protected ServletContext context;
	private String db_user;
	private String db_password;
	private String jdbc;
	private final static String APPOINTMENT_TYPE = "appointment-selfservice-session";
	private static Logger logger = Logger.getLogger(Calendar.class);

	//Get Request
	@ GET @ Path ( "/url" ) @ QueryParam ( "{calendar-id}{application-id}{calendar-name}" ) @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response calendarUrl( @ QueryParam ( "client-id" ) String client_id , @ QueryParam ( "application-id" ) String application_id , @ QueryParam ( "calendar-name" ) String calendar_name ) {
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		String url = context.getInitParameter("calendarUrl");
		try {
			//<--------------If client-id or application-id is not provided-------------->
			if ( isNullClientApplicationID(client_id, application_id) ) {
				return Response
						.serverError()
						.entity(getJsonError("client-id and application-id cannot be null.",
								"error")).build();
			}
			//<--------------If client-id or application-id is not provided-------------->
			if ( utility.parameterCheckNull(calendar_name) ) {
				return Response.serverError()
						.entity(getJsonError("calendar-name cannot be null.", "error")).build();
			}
			//<--------------------Validate client-id and application-id-------------------->
			if ( !validClientApplicationID(db_user, db_password, jdbc, client_id, application_id) ) {
				return Response.serverError()
						.entity(getJsonError("Invalid client-id or application-id.", "error"))
						.build();
			}
			if ( !calendarNameExists(db_user, db_password, jdbc, calendar_name, client_id,
					application_id) ) {
				return Response.serverError().entity(getJsonError("Calendar Not Found.", "error"))
						.build();
			}
			String result = getCalendarUrl(db_user, db_password, jdbc, calendar_name, client_id,
					application_id, url);
			if ( result.equals("notfound") ) {
				return Response.serverError().entity(getJsonError("Key Not Found.", "error"))
						.build();
			} else {
				JSONObject obj = new JSONObject();
				obj.put("response-type", "success");
				obj.put("calendar-url", result);
				return Response.ok(obj.toString()).build();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}

	//Post Request 
	@ POST @ Produces ( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response calendarCreate( JSONObject json ) {
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		try {
			String client_id = json.optString("client-id");
			String application_id = json.optString("application-id");
			String calendar_name = json.optString("calendar-name");
			String start_time = json.optString("display-start-time");
			String end_time = json.optString("display-end-time");

			//<--------------If client-id or application-id is not provided-------------->
			if ( isNullClientApplicationID(client_id, application_id) ) {
				return Response
						.serverError()
						.entity(getJsonError("client-id and application-id cannot be null.",
								"error")).build();
			}
			//<--------------------Validate client-id and application-id-------------------->
			if ( !validClientApplicationID(db_user, db_password, jdbc, client_id, application_id) ) {
				return Response.serverError()
						.entity(getJsonError("Invalid client-id or application-id.", "error"))
						.build();
			}
			//<--------------If calendar-name is not provided use 'default'-------------->
			if ( calendar_name.equals("") ) {
				calendar_name = "default";
			}
			if ( start_time.equals("") ) {
				start_time = "";
			}
			if ( end_time.equals("") ) {
				end_time = "";
			}
			if ( calendarNameExists(db_user, db_password, jdbc, calendar_name, client_id,
					application_id) ) {
				return Response.serverError()
						.entity(getJsonError("Calendar-name Already Exists.", "error")).build();
			}

			Database db = new Database(db_user, db_password, jdbc);
			PreparedStatement statement;
			//<--------------Insert New Calendar-------------->
			statement = db
					.getConnection()
					.prepareStatement(
							"INSERT INTO table_calendar(client_id,application_id,name,start_time,end_time) VALUES (?,?,?,?,?)");
			statement.setString(1, client_id);
			statement.setString(2, application_id);
			statement.setString(3, calendar_name);
			statement.setString(4, start_time);
			statement.setString(5, end_time);
			statement.execute();

			//<--------------Getting New calendar_id-------------->
			statement = db
					.getConnection()
					.prepareStatement(
							"SELECT client_id,calendar_id,application_id,name FROM table_calendar WHERE client_id = ? AND application_id=? AND name=?");
			statement.setString(1, client_id);
			statement.setString(2, application_id);
			statement.setString(3, calendar_name);

			ResultSet rs = statement.executeQuery();

			String calendar_id;
			if ( rs.next() ) {
				calendar_id = rs.getString("calendar_id");
			} else {
				db.close();
				logger.error("calendar_id Not Found.");
				return Response.serverError()
						.entity(getJsonError("calendar_id Not Found.", "error")).build();
			}
			JSONObject obj = new JSONObject();
			obj.put("response-type", "success");
			obj.put("response-description", "values set");
			obj.put("calendar-id", calendar_id);
			return Response.ok(obj.toString()).build();
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}

	//Get Request
	@ GET @ QueryParam ( "{calendar-id}{application-id}" ) @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response calendar_list( @ QueryParam ( "client-id" ) String client_id , @ QueryParam ( "application-id" ) String application_id ) {
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		try {
			//<--------------If client-id or application-id is not provided-------------->
			if ( isNullClientApplicationID(client_id, application_id) ) {
				return Response
						.serverError()
						.entity(getJsonError("client-id and application-id cannot be null.",
								"error")).build();
			}
			//<--------------------Validate client-id and application-id-------------------->
			if ( !validClientApplicationID(db_user, db_password, jdbc, client_id, application_id) ) {
				return Response.serverError()
						.entity(getJsonError("Invalid client-id or application-id.", "error"))
						.build();
			}
			String listCalendarsQuery = "Select name,calendar_id,client_id,application_id from table_calendar WHERE client_id=? AND application_id=?";
			Database db = new Database(db_user, db_password, jdbc);
			PreparedStatement st = db.getConnection().prepareStatement(listCalendarsQuery);
			st.setString(1, client_id);
			st.setString(2, application_id);
			ResultSet rs = st.executeQuery();

			//<------------ JSON Objects ------------>
			JSONObject obj = new JSONObject();
			JSONArray calendarsArray = new JSONArray();
			obj.put("client-id", client_id);
			obj.put("application-id", application_id);
			boolean empty = true;
			while ( rs.next() ) {
				empty = false;
				String calendar_id = rs.getString("calendar_id");
				String calendar_name = rs.getString("name");

				JSONObject dayObjects = new JSONObject();
				dayObjects.put("calendar-id", calendar_id);
				dayObjects.put("calendar-name", calendar_name);
				calendarsArray.put(dayObjects);
				obj.put("calendar", calendarsArray);
			}
			if ( empty ) {
				db.close();
				return Response.serverError()
						.entity(getJsonError("No Calendars Found For This User.", "error")).build();
			} else {
				db.close();
				return Response.ok(obj.toString()).build();
			}

		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}

	}

	//Delete Request
	@ DELETE @ Path ( "/{id}" ) @ QueryParam ( "{client-id}{application-id}" ) @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response calendar_delete( @ PathParam ( "id" ) String id , @ QueryParam ( "client-id" ) String client_id , @ QueryParam ( "application-id" ) String application_id ) {
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		String result = "";

		try {
			//<--------------If client-id or application-id is not provided-------------->
			if ( isNullClientApplicationID(client_id, application_id) ) {
				return Response
						.serverError()
						.entity(getJsonError("client-id and application-id cannot be null.",
								"error")).build();
			}
			//<-----------------Validate calendar_id and calendar_name----------------->
			if ( id != null ) {
				if ( !Time_Functions.isInteger(id) ) {
					return Response.serverError()
							.entity(getJsonError("calendar-id Must Be Integer.", "error")).build();
				}
			} else {
				return Response.serverError()
						.entity(getJsonError("Must Provide calendar_id.", "error")).build();
			}

			Database db = new Database(db_user, db_password, jdbc);
			PreparedStatement st;
			Connection con = db.getConnection();
			String searchCalendarQuery = "SELECT client_id,application_id,calendar_id,name from table_calendar WHERE client_id=? AND application_id=? AND calendar_id =?";
			st = con.prepareStatement(searchCalendarQuery);
			st.setString(1, client_id);
			st.setString(2, application_id);
			st.setInt(3, Integer.parseInt(id));

			ResultSet rs = st.executeQuery();
			String calendar_name;

			if ( rs.next() ) {
				calendar_name = rs.getString("name");
			} else {
				con.close();
				return Response.serverError().entity(getJsonError("Calendar Not Found.", "error"))
						.build();
			}
			rs.close();

			if ( calendar_name.equalsIgnoreCase("default") ) {
				db.close();
				return Response
						.serverError()
						.entity(getJsonError("calendar " + id
								+ " is default calendar and cannot be deleted.", "error")).build();
			} else {
				String deleteCalendarQuery = "DELETE FROM table_calendar WHERE client_id=? AND application_id=? AND calendar_id =?";
				st = con.prepareStatement(deleteCalendarQuery);
				st.setString(1, client_id);
				st.setString(2, application_id);
				st.setInt(3, Integer.parseInt(id));

				if ( st.executeUpdate() > 0 ) {
					JSONObject obj = new JSONObject();
					obj.put("response-type", "success");
					obj.put("response-description", "calendar " + id + " " + calendar_name
							+ " deleted.");
					result = obj.toString();
				} else {
					con.close();
					return Response.serverError()
							.entity(getJsonError("Error Deleting Calendar.", "error")).build();
				}
				con.close();
				return Response.ok(result).build();
			}

		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}

	}

	/**
	 * Takes client_id and application_id
	 *
	 * @return true if client_id or application_id is empty.
	 */
	public boolean isNullClientApplicationID( String client_id , String application_id ) {
		if ( client_id == null || application_id == null || client_id.equals("")
				|| application_id.equals("") ) {
			return true;
		}
		return false;
	}

	/**
	 * Takes an error message and an error type
	 *
	 * @return a json object
	 */
	public String getJsonError( String error_message , String type ) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("response-type", type);
		obj.put("response-description", error_message);
		return obj.toString();
	}

	/**
	 * Takes database user,password,jdbc and client_id,application_id
	 *
	 * Validate client_id and application_id in table_account
	 *
	 * @return success if record exists in table_account
	 */
	public boolean validClientApplicationID( String db_user , String db_password , String jdbc , String client_id , String application_id ) throws SQLException , ClassNotFoundException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("Select client_id,application_id from table_account WHERE client_id =? AND application_id = ?");
		st.setString(1, client_id);
		st.setString(2, application_id);

		ResultSet rs = st.executeQuery();
		boolean valid = false;
		if ( rs.next() ) {
			valid = true;
		}
		db.close();
		return valid;
	}

	/**
	 * Check if calendar_name already exists in table_calendar
	 */
	public boolean calendarNameExists( String db_user , String db_password , String jdbc , String calendar_name , String client_id , String application_id ) throws ClassNotFoundException , SQLException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("Select * From table_calendar WHERE name=? AND client_id=? AND application_id=?");
		st.setString(1, calendar_name);
		st.setString(2, client_id);
		st.setString(3, application_id);

		ResultSet rs = st.executeQuery();
		boolean result = false;
		if ( rs.next() ) {
			result = true;
		}
		db.close();
		return result;
	}

	public String getCalendarUrl( String db_user , String db_password , String jdbc , String calendar_name , String client_id , String application_id , String url ) throws SQLException , ClassNotFoundException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("select client_id,application_id,key from table_account where client_id=? AND application_id=?");
		st.setString(1, client_id);
		st.setString(2, application_id);
		String key = "";
		ResultSet rs = st.executeQuery();
		if ( rs.next() ) {
			key = rs.getString("key");
		}
		if ( key == null || key.equals("") ) {
			con.close();
			return "notfound";
		} else {
			String newUrl = url + "?key=" + key + "&calendar-name=" + calendar_name
					+ "&appointment-type=" + APPOINTMENT_TYPE;
			con.close();
			return newUrl;
		}
	}
}
