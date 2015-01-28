package Jersey_Services;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import javax.servlet.ServletContext;

import javax.ws.rs.GET;

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

@ Path ( "calendar/event/recurrence" ) public class CalendarEventRecurrence {
	@ Context protected ServletContext context;
	private String db_user;
	private String db_password;
	private String jdbc;
	private final static int CLIENT_ID = 0;
	private final static int APPLICATION_ID = 1;
	private final static int INTERVAL = 0;
	private final static int MONTH = 2;
	private final static int DAY = 1;
	private final static int DATE = 1;
	private final static int ARRAY_SIZE = 2;
	
	private static Logger logger = Logger.getLogger(CalendarEventRecurrence.class);

	//------------------------------------------ Get Participant Function --------------------------------------------//
	@ GET @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response getParticipant( @ QueryParam ( "client-id" ) String client_id , @ QueryParam ( "application-id" ) String application_id ,  @ QueryParam ( "calendar-id" ) String calendar_id ,@ QueryParam ( "calendar-name" ) String calendar_name ,@ QueryParam ( "key" ) String key,@ QueryParam ( "appointment-type" ) String appointment_type  ) {
		String result = "";
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		String[] ClientApplicationID = new String[ARRAY_SIZE];
		
		try {
			if ( utility.parameterCheckNull(key) && utility.parameterCheckNull(client_id) &&  utility.parameterCheckNull(application_id) ) {
				return Response.serverError().entity(utility.json_error("Must Provide key Or client-id/application-id.", "error"))
						.build();
			}
			//check key
			if(!utility.parameterCheckNull(key)){
				ClientApplicationID = utility.getClientApplicationID(db_user, db_password,
						jdbc, key);
				
				if ( ClientApplicationID == null ) {
					return Response.serverError().entity(utility.json_error("Account Not Found.", "error"))
							.build();
				}
			}
			else{
				//<--------------If client-id or application-id is not provided-------------->
				if ( utility.parameterCheckNull(client_id) ) {
					return Response.serverError()
							.entity(utility.json_error("client-id cannot be null.", "error")).build();
				}
				if ( utility.parameterCheckNull(application_id) ) {
					return Response.serverError()
							.entity(utility.json_error("application-id cannot be null.", "error")).build();
				}
				
				ClientApplicationID[CLIENT_ID] = client_id;
				ClientApplicationID[APPLICATION_ID] = application_id;
			}
			
			//check appointment-type
			if ( utility.parameterCheckNull(appointment_type) ) {
				return Response.serverError()
						.entity(utility.json_error("appointment-type cannot be null.", "error")).build();
			}
			//check calendar-name/calendar-id
			if ( utility.parameterCheckNull(calendar_id) && utility.parameterCheckNull(calendar_name)) {
				return Response.serverError()
						.entity(utility.json_error("Must Provide calendar-id Or calendar-name.", "error")).build();
			}
			else{
				if ( utility.parameterCheckNull(calendar_id) ) {
					calendar_id = utility.getCalendarID(db_user, db_password, jdbc,
							calendar_name, ClientApplicationID[CLIENT_ID], ClientApplicationID[APPLICATION_ID]);
				}
				if ( calendar_id.equals("notfound") ) {
					return Response.serverError()
							.entity(utility.json_error("Calendar Could Not Be Found.", "error")).build();
				}
			}
			//<--------------------Validate client-id and application-id-------------------->
			if ( !utility.clientApplicationIDValidate(db_user, db_password, jdbc,
					ClientApplicationID[CLIENT_ID], ClientApplicationID[APPLICATION_ID]) ) {
				return Response.serverError()
						.entity(utility.json_error("Invalid client-id or application-id.", "error"))
						.build();
			}
			
			result = eventRecurrence_Function(db_user, db_password, jdbc, ClientApplicationID, calendar_id,appointment_type);
			
			if ( result.equals("notfound") ) {
				return Response.serverError().entity(utility.json_error("No Recurrences Found.", "error"))
						.build();
			}else {
				return Response.ok(result).build();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}



	//------------------------------------------------------------------------ API Methods (SQL Transactions) -------------------------------------------------------------------------------//
	public String eventRecurrence_Function(String db_user,String db_password,String jdbc,String[] ClientApplicationID,String calendar_id,String appointment_type) throws ClassNotFoundException, SQLException, JSONException{
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		String recurrenceAppointmentJoin = "select * from table_recurrence as r INNER JOIN (select MIN(title)title,MIN(transaction_id)transaction_id,MIN(\"location\")\"location\", MIN(duration)duration,MIN(notes)notes,recurrence_id from table_appointment WHERE client_id=? AND application_id = ? AND calendar_id=? AND appointment_type=? GROUP BY recurrence_id) as a ON a.recurrence_id  = r.id  WHERE r.client_id=? AND r.application_id = ?";
		
		String result = "";
		boolean found = false;
		PreparedStatement st = con.prepareStatement(recurrenceAppointmentJoin);
		st.setString(1, ClientApplicationID[CLIENT_ID]);
		st.setString(2, ClientApplicationID[APPLICATION_ID]);
		st.setInt(3, Integer.parseInt(calendar_id));
		st.setString(4,appointment_type);
		st.setString(5, ClientApplicationID[CLIENT_ID]);
		st.setString(6, ClientApplicationID[APPLICATION_ID]);
		
		JSONObject obj = new JSONObject();
		obj.put("calendar-id",calendar_id);
		JSONArray recurrences = new JSONArray();
		String recurrence_info[];
		
		ResultSet rs = st.executeQuery();
		
		//Compose JSON body
		while(rs.next()){
			found = true;
			JSONObject recurrence = new JSONObject();
			recurrence.put("recurrence-id",rs.getString("recurrence_id"));
			recurrence.put("title",rs.getString("title"));
			recurrence.put("transaction-id",rs.getString("transaction_id"));
			recurrence.put("location",rs.getString("location"));
			recurrence.put("recurrence-start-time",rs.getString("recurrence_start"));
			recurrence.put("recurrence-end-time",rs.getString("recurrence_end"));
			recurrence.put("recurrence-time",rs.getString("recurrence_time"));
			recurrence.put("duration",rs.getString("duration"));
			
			if(rs.getString("recurrence")!=null && rs.getString("recurrence").equals("daily")){
				recurrence.put("repeat-type","day");
				recurrence.put("interval", rs.getString("recurrence_info"));
				recurrence.put("description","Every "+rs.getString("recurrence_info")+" day(s) at "+rs.getString("recurrence_time"));
			}
			else if(rs.getString("recurrence")!=null && rs.getString("recurrence").equals("weekly")){
				recurrence.put("repeat-type","week");
				recurrence_info = rs.getString("recurrence_info").split(":");
				recurrence.put("interval", recurrence_info[INTERVAL]);
				recurrence.put("day", recurrence_info[DAY].toLowerCase());
				recurrence.put("description",recurrence_info[DAY]+" of every "+recurrence_info[INTERVAL]+" week(s) at "+rs.getString("recurrence_time"));
			}
			else if(rs.getString("recurrence")!=null && rs.getString("recurrence").equals("monthly")){
				recurrence.put("repeat-type","month");
				recurrence_info = rs.getString("recurrence_info").split(":");
				recurrence.put("interval", recurrence_info[INTERVAL]);
				recurrence.put("date", recurrence_info[DATE].toLowerCase());
				recurrence.put("description",getDateString(recurrence_info[DATE])+" of every "+recurrence_info[INTERVAL]+" month(s) at "+rs.getString("recurrence_time"));
			}
			else if(rs.getString("recurrence")!=null && rs.getString("recurrence").equals("yearly")){
				recurrence.put("repeat-type","year");
				recurrence_info = rs.getString("recurrence_info").split(":");
				recurrence.put("interval", recurrence_info[INTERVAL]);
				recurrence.put("date", recurrence_info[DATE]);
				recurrence.put("month", recurrence_info[MONTH].toLowerCase());
				recurrence.put("description",getDateString(recurrence_info[DATE])+" of "+recurrence_info[2].toLowerCase()+" of every "+recurrence_info[INTERVAL]+" year(s) at "+rs.getString("recurrence_time"));
			}
			recurrences.put(recurrence);
		}
		if(found){
			obj.put("recurrences", recurrences);
			result = obj.toString();
		}
		else{
			result = "notfound";
		}
		con.close();
		return result;
	}
	
	public String getDateString(String date){
		if(Integer.parseInt(date) == 1){
			return date+"st";
		}
		if(Integer.parseInt(date) == 2){
			return date+"nd";
		}
		if(Integer.parseInt(date) == 3){
			return date+"rd";
		}
		else{
			return date+"th";
		}
	}

}
