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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import utilityClasses.Database;
import utilityClasses.utility;

@ Path ( "calendar/event/status" ) public class CalendarEventStatus {
	@ Context protected ServletContext context;
	private String maxParticipant;
	private String participant_type;
	private String db_user;
	private String db_password;
	private String jdbc;
	private static Logger logger = Logger.getLogger(CalendarEventStatus.class);

	/* ========================Status Function=========================== */
	@ GET @ QueryParam ( "{client-id}{application-id}{event-id}" ) @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response function_status( @ QueryParam ( "client-id" ) String client_id , @ QueryParam ( "application-id" ) String application_id , @ QueryParam ( "event-id" ) String event_id ) {
		String result = "";
		try {
			db_user = context.getInitParameter("db_user");
			db_password = context.getInitParameter("db_password");
			jdbc = context.getInitParameter("jdbc");
			if ( utility.parameterCheckNull(client_id) ) {
				return Response.serverError().entity(json_error("client-id is missing.", "error"))
						.build();
			}
			if ( utility.parameterCheckNull(application_id) ) {
				return Response.serverError()
						.entity(json_error("application_id is missing.", "error")).build();
			}
			if ( utility.parameterCheckNull(event_id) ) {
				return Response.serverError().entity(json_error("event_id is missing.", "error"))
						.build();
			}
			String eventDetails = getEventDetails(db_user, db_password, jdbc, client_id,
					application_id, event_id);
			if ( eventDetails.equals("notfound") ) {
				return Response.serverError().entity(json_error("Event Not Found.", "error"))
						.build();
			} else {
				int totalParticipants = utility.getNumberOfParticipants(db_user, db_password, jdbc,
						client_id, application_id, event_id);
				int no_of_confirmed_participant = utility.getNumberOfConfirmedParticipants(db_user,
						db_password, jdbc, client_id, application_id, event_id);
				String event_status = "";
				if ( maxParticipant.equals("notfound") ) {
					return Response.serverError()
							.entity(json_error("no_of_participants Not Found.", "error")).build();
				} else if ( Integer.parseInt(maxParticipant) == 0
						|| totalParticipants >= Integer.parseInt(maxParticipant) ) {
					event_status = "full";
				} else {
					event_status = "available";
				}

				JSONObject response = new JSONObject();
				response.put("event-id", event_id);
				response.put("participant-type", getParticipant_type());
				response.put("no-of-participants", maxParticipant);
				response.put("no-of-participants-confirmed", no_of_confirmed_participant);
				response.put("event-status", event_status);
				result = response.toString();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
		logger.info(result);
		return Response.ok(result).build();
	}

	public String getEventDetails( String db_user , String db_password , String jdbc , String client_id , String application_id , String event_id ) throws ClassNotFoundException , SQLException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("Select client_id,application_id,appointment_id,no_of_participants,participant_type FROM table_appointment WHERE client_id=? AND application_id=? AND appointment_id=?");
		st.setString(1, client_id);
		st.setString(2, application_id);
		st.setInt(3, Integer.parseInt(event_id));

		ResultSet rs = st.executeQuery();
		if ( rs.next() ) {
			setMaxParticipant(rs.getString("no_of_participants"));
			setParticipant_type(rs.getString("participant_type"));
		} else {
			db.close();
			return "notfound";
		}
		return "success";
	}

	public String json_error( String error_message , String type ) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("response-type", type);
		obj.put("response-description", error_message);
		return obj.toString();
	}

	public String getParticipant_type() {
		return participant_type;
	}

	public void setParticipant_type( String participant_type ) {
		this.participant_type = participant_type;
	}

	public void setMaxParticipant( String max ) {
		this.maxParticipant = max;
	}

	public int getMaxParticipant() {
		return Integer.parseInt(this.maxParticipant);
	}
}
