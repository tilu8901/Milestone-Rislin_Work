package Jersey_Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
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

/**
 * Purpose: Create, Read, and Update methods for table_calendar_schedule 
 *
 * @author Tianhua Lu
 * @version 1.0 23/9/2014
 */
@ Path ( "calendar/schedule" ) public class CalendarSchedule {
	@ Context protected ServletContext context;
	private String db_user;
	private String db_password;
	private String jdbc;
	private static Logger logger = Logger.getLogger(CalendarSchedule.class);

	@ GET @ QueryParam ( "{calendar-id}{client-id}{application-id}{calendar-name}" ) @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response schedule_read( @ QueryParam ( "client-id" ) String client_id , @ QueryParam ( "application-id" ) String application_id , @ QueryParam ( "calendar-id" ) String calendar_id , @ QueryParam ( "calendar-name" ) String calendar_name ) {
		String result = "";
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
			//<-----------------Validate calendar_id and calendar_name----------------->
			if ( utility.parameterCheckNull(calendar_id)
					&& utility.parameterCheckNull(calendar_name) ) {
				return Response.serverError()
						.entity(json_error("Calendar-id/Calendar-name Is Mandatory.", "error"))
						.build();
			}
			if ( utility.parameterCheckNull(calendar_id) ) {
				calendar_id = utility.getCalendarID(db_user, db_password, jdbc, calendar_name,
						client_id, application_id);
			}
			if ( calendar_id.equals("notfound") ) {
				return Response.serverError()
						.entity(json_error("Calendar Could Not Be Found.", "error")).build();
			}
			//<--------------------Validate client-id and application-id-------------------->
			if ( !validClientApplicationID(db_user, db_password, jdbc, client_id, application_id) ) {
				return Response.serverError()
						.entity(getJsonError("Invalid client-id or application-id.", "error"))
						.build();
			}

			//<--------------------Insert Data Into 'table_calendar_schedule'-------------------->
			result = searchSchedule(db_user, db_password, jdbc, client_id, application_id,
					calendar_name, calendar_id);
			if ( result.equals("notfound") ) {
				return Response.serverError()
						.entity(getJsonError("Schedules Could Not Be Found.", "error")).build();
			} else {
				return Response.ok(result).build();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}

	@ POST @ Produces ( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response schedule_create( JSONObject json ) {
		String result = "";
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		try {
			String client_id = json.optString("client-id");
			String application_id = json.optString("application-id");
			String calendar_id = json.optString("calendar-id");
			String calendar_name = json.optString("calendar-name");

			//<--------------If client-id or application-id is not provided-------------->
			if ( isNullClientApplicationID(client_id, application_id) ) {
				return Response
						.serverError()
						.entity(getJsonError("client-id and application-id cannot be null.",
								"error")).build();
			}
			//<-----------------Validate calendar_id and calendar_name----------------->
			if ( utility.parameterCheckNull(calendar_id)
					&& utility.parameterCheckNull(calendar_name) ) {
				return Response.serverError()
						.entity(json_error("Calendar-id/Calendar-name Is Mandatory.", "error"))
						.build();
			}
			if ( utility.parameterCheckNull(calendar_id) ) {
				calendar_id = utility.getCalendarID(db_user, db_password, jdbc, calendar_name,
						client_id, application_id);
			}
			if ( calendar_id.equals("notfound") ) {
				return Response.serverError()
						.entity(json_error("Calendar Could Not Be Found.", "error")).build();
			}
			//<--------------------Validate client-id and application-id-------------------->
			if ( !validClientApplicationID(db_user, db_password, jdbc, client_id, application_id) ) {
				return Response.serverError()
						.entity(getJsonError("Invalid client-id or application-id.", "error"))
						.build();
			}

			//<--------------------Insert Data Into 'table_calendar_schedule'-------------------->
			result = insertSchedule(json, db_user, db_password, jdbc, client_id, application_id,
					calendar_name, calendar_id);
			if ( result.equals("success") ) {
				JSONObject obj = new JSONObject();
				obj.put("response-type", "success");
				obj.put("response-description",
						"New Record Has Been Added To table_calendar_schedule.");
				return Response.ok(obj.toString()).build();
			}
			return Response.serverError().entity(getJsonError(result, "error")).build();
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}

	// ============================= Put Request Handler ================================//
	@ PUT @ Produces ( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response schedule_update( JSONObject json ) {
		String result = "";
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		try {
			String client_id = json.optString("client-id");
			String application_id = json.optString("application-id");
			String calendar_id = json.optString("calendar-id");
			String calendar_name = json.optString("calendar-name");
			//<--------------If client-id or application-id is not provided-------------->
			if ( isNullClientApplicationID(client_id, application_id) ) {
				return Response
						.serverError()
						.entity(getJsonError("client-id and application-id cannot be null.",
								"error")).build();
			}
			//<-----------------Validate calendar_id and calendar_name----------------->
			if ( utility.parameterCheckNull(calendar_id)
					&& utility.parameterCheckNull(calendar_name) ) {
				return Response.serverError()
						.entity(json_error("Calendar-id/Calendar-name Is Mandatory.", "error"))
						.build();
			}
			if ( utility.parameterCheckNull(calendar_id) ) {
				calendar_id = utility.getCalendarID(db_user, db_password, jdbc, calendar_name,
						client_id, application_id);
			}
			if ( calendar_id.equals("notfound") ) {
				return Response.serverError()
						.entity(json_error("Calendar Could Not Be Found.", "error")).build();
			}
			//<--------------------Validate client-id and application-id-------------------->
			if ( !validClientApplicationID(db_user, db_password, jdbc, client_id, application_id) ) {
				return Response.serverError()
						.entity(getJsonError("Invalid client-id or application-id.", "error"))
						.build();
			}
			result = updateSchedule(json, db_user, db_password, jdbc, client_id, application_id,
					calendar_name, calendar_id);
			if ( result.equals("success") ) {
				JSONObject obj = new JSONObject();
				obj.put("response-type", "success");
				obj.put("response-description", "values set");
				return Response.ok(obj.toString()).build();
			} else {
				return Response.serverError().entity(getJsonError(result, "error")).build();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}

	// ============================= Helper Methods =================================//
	/**
	 * Takes database user,password,jdbc and client_id,application_id
	 *
	 * Validate client_id and application_id in table_account
	 *
	 * @return success if record exists in table_account
	 */
	public boolean validClientApplicationID( String db_user , String db_password , String jdbc , String client_id , String application_id ) throws SQLException , ClassNotFoundException {
		Database db = new Database(db_user, db_password, jdbc);
		Statement st = db.getStatement();
		ResultSet rs = st
				.executeQuery("Select client_id,application_id from table_account WHERE client_id = '"
						+ client_id + "' AND application_id = '" + application_id + "'");
		boolean valid = false;
		if ( rs.next() ) {
			valid = true;
		}
		db.close();
		return valid;
	}

	/**
	 * Takes database user,password,jdbc and calendar_name,client_id,application_id,calendar_id
	 *
	 * Finds the schedule records based on inputs and create a JSON response
	 *
	 * @return JSON response if records were retrieved successful
	 */
	public String searchSchedule( String db_user , String db_password , String jdbc , String client_id , String application_id , String calendar_name , String calendar_id ) throws ClassNotFoundException , SQLException , JSONException {
		String result = "";
		boolean empty = true;
		Database db = new Database(db_user, db_password, jdbc);
		PreparedStatement statement;
		Connection con = db.getConnection();
		statement = con
				.prepareStatement("SELECT * FROM table_calendar_schedule WHERE client_id=? AND application_id=? AND calendar_id=? AND calendar_name=?");
		statement.setString(1, client_id);
		statement.setString(2, application_id);
		statement.setInt(3, Integer.parseInt(calendar_id));
		statement.setString(4, calendar_name);

		ResultSet rs = statement.executeQuery();
		//<------------ JSON Objects ------------>
		JSONObject obj = new JSONObject();
		JSONArray schedulesArray = new JSONArray();
		obj.put("client-id", client_id);
		obj.put("application-id", application_id);
		obj.put("calendar-id", calendar_id);
		obj.put("calendar-name", calendar_name);

		//<---------- Grabbing Day Data ---------->
		while ( rs.next() ) {
			empty = false;
			String day_name = rs.getString("day_name");
			String day_start_time = rs.getString("day_start_time");
			String day_finish_time = rs.getString("day_finish_time");

			JSONObject dayObjects = new JSONObject();
			dayObjects.put("day-name", day_name);
			dayObjects.put("day-start-time", day_start_time);
			dayObjects.put("day-finish-time", day_finish_time);
			schedulesArray.put(dayObjects);
			obj.put("day", schedulesArray);
		}
		if ( empty ) {
			result = "notfound";
		} else {
			result = obj.toString();
		}
		con.close();
		return result;
	}

	/**
	 * Takes database user,password,jdbc and calendar_name,client_id,application_id,calendar_id, json request
	 *
	 * Inserts a new schedule record in table_calendar_schedule
	 *
	 * @return success if successfully inserted
	 */
	public String insertSchedule( JSONObject json , String db_user , String db_password , String jdbc , String client_id , String application_id , String calendar_name , String calendar_id ) throws JSONException , ClassNotFoundException , SQLException , ParseException {
		String result = "";
		Database db = new Database(db_user, db_password, jdbc);
		PreparedStatement statement;
		Connection con = db.getConnection();
		JSONArray days = json.getJSONArray("day");
		for ( int i = 0 ; i < days.length() ; i++ ) {
			JSONObject object = (JSONObject) days.get(i);
			String day_name = "";
			String day_start_time = "";
			String day_finish_time = "";

			if ( object.has("day-name") ) {
				day_name = object.getString("day-name");
				if ( !checkDuplicatedDayName(db_user, db_password, jdbc, client_id, application_id,
						calendar_id, calendar_name, day_name) ) {
					if ( object.has("day-start-time") ) {
						day_start_time = object.getString("day-start-time");
					}
					if ( object.has("day-finish-time") ) {
						day_finish_time = object.getString("day-finish-time");
					}
					statement = con
							.prepareStatement("INSERT INTO table_calendar_schedule(client_id,application_id,calendar_id,calendar_name,day_name,day_start_time,day_finish_time) VALUES (?,?,?,?,?,?,?)");
					statement.setString(1, client_id);
					statement.setString(2, application_id);
					statement.setInt(3, Integer.parseInt(calendar_id));
					statement.setString(4, calendar_name);
					statement.setString(5, day_name);
					statement.setTimestamp(6, toTimestamp(day_start_time));
					statement.setTimestamp(7, toTimestamp(day_finish_time));
					statement.execute();
				} else {
					result += day_name + ",";
					logger.error(day_name + " Already Exists For Calendar " + calendar_id + ".");
				}
			} else {
				return "day-name(s) Not Found In The Request.";
			}
		}
		db.close();
		if ( result.equals("") ) {
			return "success";
		} else {
			return result + " Already Exists For Calendar " + calendar_id + ".";
		}
	}

	/**
	 * Takes database user,password,jdbc and calendar_name,client_id,application_id,calendar_id, json request
	 *
	 * Updates table_calendar_schedule based on the json request
	 *
	 * @return success if the record has been found and updated
	 */
	public String updateSchedule( JSONObject json , String db_user , String db_password , String jdbc , String client_id , String application_id , String calendar_name , String calendar_id ) throws JSONException , ClassNotFoundException , SQLException , ParseException {
		Database db = new Database(db_user, db_password, jdbc);
		PreparedStatement statement;
		Connection con = db.getConnection();
		JSONArray days = json.getJSONArray("day");
		String error = "";
		for ( int i = 0 ; i < days.length() ; i++ ) {
			JSONObject object = (JSONObject) days.get(i);
			String day_name = "";
			String day_start_time = "";
			String day_finish_time = "";
			if ( object.has("day-name") ) {
				day_name = object.getString("day-name");
				if ( object.has("day-start-time") ) {
					day_start_time = object.getString("day-start-time");
				}
				if ( object.has("day-finish-time") ) {
					day_finish_time = object.getString("day-finish-time");
				}
				if ( hasSchedule(db_user, db_password, jdbc, client_id, application_id,
						calendar_name, calendar_id, day_name) ) {
					statement = con
							.prepareStatement("UPDATE table_calendar_schedule SET day_start_time=?,day_finish_time=? WHERE client_id=? AND application_id=? AND calendar_id=? AND calendar_name=? AND day_name=?");
					statement.setString(1, day_start_time);
					statement.setString(2, day_finish_time);
					statement.setString(3, client_id);
					statement.setString(4, application_id);
					statement.setInt(5, Integer.parseInt(calendar_id));
					statement.setString(6, calendar_name);
					statement.setString(7, day_name);

					statement.executeUpdate();
				} else {
					error += "'" + day_name + "'" + " For User: " + client_id + " Calendar: "
							+ calendar_name + ", ";
				}
			}
		}
		db.close();
		if ( error.equals("") ) {
			return "success";
		} else {
			return error + " Not Found.";
		}
	}

	/**
	 * Takes database user,password,jdbc and client_id,application_id,calendar_id,calendar_name,day_name
	 *
	 * Check to see if a record in table_calendar_schedule already exists
	 *
	 * @return true if found/false if not found
	 */
	public boolean hasSchedule( String db_user , String db_password , String jdbc , String client_id , String application_id , String calendar_name , String calendar_id , String day_name ) throws ClassNotFoundException , SQLException {
		boolean empty = true;
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement statement = con
				.prepareStatement("SELECT * FROM table_calendar_schedule WHERE client_id=? AND application_id=? AND calendar_id=? AND calendar_name=? AND day_name=?");
		statement.setString(1, client_id);
		statement.setString(2, application_id);
		statement.setInt(3, Integer.parseInt(calendar_id));
		statement.setString(4, calendar_name);
		statement.setString(5, day_name);

		ResultSet rs;
		rs = statement.executeQuery();
		if ( rs.next() ) {
			empty = false;
		}
		if ( empty ) {
			db.close();
			return false;
		} else {
			db.close();
			return true;
		}
	}

	/**
	 * Takes database user,password,jdbc and calendar_name,client_id,application_id
	 *
	 * Finds the calendar_id from table_calendar based on calendar_name
	 *
	 * @return calendar_id if found/return notfound 
	 */
	public String getCalendarID( String db_user , String db_password , String jdbc , String calendar_name , String client_id , String application_id ) throws SQLException , ClassNotFoundException {
		Database db = new Database(db_user, db_password, jdbc);
		logger.info("Searching Through table_calendar....");
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("SELECT calendar_id,name from table_calendar where name = ? AND client_id = ? AND application_id = ?");
		st.setString(1, calendar_name);
		st.setString(2, client_id);
		st.setString(3, application_id);

		ResultSet rs = st.executeQuery();
		String calendar_id;
		if ( rs.next() ) {
			calendar_id = rs.getString("calendar_id");
		} else {
			db.close();
			return "notfound";
		}
		db.close();
		return calendar_id;
	}

	/**
	 * Takes db_user,db_password,jdbc and client_id,application_id,calendar_id,calendar_name,day_name
	 * 
	 * Check whether the day_name exists in table_calendar_schedule for a particular record
	 * 
	 * @return true if found/false if not found
	 */
	public boolean checkDuplicatedDayName( String db_user , String db_password , String jdbc , String client_id , String application_id , String calendar_id , String calendar_name , String day_name ) throws ClassNotFoundException , SQLException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("SELECT client_id,application_id,calendar_id,calendar_name,day_name FROM table_calendar_schedule WHERE client_id = ? AND application_id = ? AND calendar_id =? AND calendar_name = ? AND day_name = ?");
		st.setString(1, client_id);
		st.setString(2, application_id);
		st.setInt(3, Integer.parseInt(calendar_id));
		st.setString(4, calendar_name);
		st.setString(5, day_name);

		ResultSet rs = st.executeQuery();
		boolean valid = false;
		if ( rs.next() ) {
			valid = true;
		}
		db.close();
		return valid;
	}

	/**
	 * Takes an error message and an error type
	 *
	 * @return an error json body
	 */
	public String getJsonError( String error_message , String type ) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("response-type", type);
		obj.put("response-description", error_message);
		return obj.toString();
	}

	/**
	 * Takes a time string
	 *
	 * @return in timestamp format
	 */
	public java.sql.Timestamp toTimestamp( String time ) throws ParseException {
		String format = "hh:mm";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		java.util.Date date = sdf.parse(time);
		java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
		return timestamp;
	}

	/**
	 * Takes client_id and application_id
	 *
	 * @return true if client_id or application_id is empty.
	 */
	public boolean isNullClientApplicationID( String client_id , String application_id ) {
		if ( client_id == null || application_id == null ) {
			return true;
		}
		return false;
	}

	public String json_error( String error_message , String type ) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("response-type", type);
		obj.put("response-description", error_message);
		return obj.toString();
	}
}
