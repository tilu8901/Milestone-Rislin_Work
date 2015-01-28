package Jersey_Services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import utilityClasses.Database;
import utilityClasses.utility;

@ Path ( "calendar/event/participant" ) public class CalendarEventParticipant {
	@ Context protected ServletContext context;
	private String db_user;
	private String db_password;
	private String jdbc;
	private String contact_firstname;
	private String mobile_number;
	private String email_address;
	private String contact_id;
	private String participant_id;
	private JSONArray participantArray;
	private String participant_type;
	private String no_of_participants;
	private String confirmation_status;
	private final static int ARRAY_SIZE = 2;
	private final static int CLIENT_ID = 0;
	private final static int APPLICATION_ID = 1;

	private static Logger logger = Logger.getLogger(CalendarEventParticipant.class);

	public CalendarEventParticipant () {
		contact_firstname = "";
		mobile_number = "";
		email_address = "";
		participantArray = new JSONArray();
	}

	//------------------------------------------ Delete Participant Function --------------------------------------------//
	@ DELETE @ QueryParam ( "{client-id}{application-id}{event-id}{participant-id}{user-id}{key}" ) @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response deleteParticipant( @ QueryParam ( "client-id" ) String client_id , @ QueryParam ( "application-id" ) String application_id , @ QueryParam ( "event-id" ) String event_id , @ QueryParam ( "participant-id" ) String participant_id , @ QueryParam ( "user-id" ) String user_id , @ QueryParam ( "key" ) String key ) {
		String result = "";
		String ClientApplicationID[] = new String[ARRAY_SIZE];
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		try {
			if ( utility.parameterCheckNull(key) ) {
				//<--------------If client-id or application-id is not provided-------------->
				if ( utility.parameterCheckNull(client_id) ) {
					return Response.serverError()
							.entity(json_error("client-id cannot be null.", "error")).build();
				}
				if ( utility.parameterCheckNull(application_id) ) {
					return Response.serverError()
							.entity(json_error("application-id cannot be null.", "error")).build();
				}
				//<--------------------Validate client-id and application-id-------------------->
				if ( !utility.clientApplicationIDValidate(db_user, db_password, jdbc, client_id,
						application_id) ) {
					return Response.serverError()
							.entity(json_error("Invalid client-id or application-id.", "error"))
							.build();
				}
			} else {
				ClientApplicationID = utility.getClientApplicationID(db_user, db_password, jdbc,
						key);
				if ( ClientApplicationID == null ) {
					return Response.serverError().entity(json_error("Account Not Found.", "error"))
							.build();
				}
				client_id = ClientApplicationID[CLIENT_ID];
				application_id = ClientApplicationID[APPLICATION_ID];
			}

			if ( utility.parameterCheckNull(event_id) ) {
				return Response.serverError()
						.entity(json_error("event-id cannot be null.", "error")).build();
			}

			if ( utility.parameterCheckNull(participant_id) && utility.parameterCheckNull(user_id) ) {
				return Response.serverError()
						.entity(json_error("Participant-id/User-id is mandatory.", "error"))
						.build();
			}

			result = deleteParticipant(db_user, db_password, jdbc, client_id, application_id,
					event_id, participant_id, user_id);

			if ( result.equals("notfound") ) {
				return Response
						.serverError()
						.entity(json_error("Participant Record Not Found, no record was deleted.",
								"error")).build();
			} else if ( result.equals("sql_error") ) {
				return Response.serverError()
						.entity(json_error("Invalid SQL String, no record was deleted.", "error"))
						.build();
			} else {
				return Response.ok(result).build();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}

	//------------------------------------------ Get Participant Function --------------------------------------------//
	@ GET @ QueryParam ( "{client-id}{application-id}{event-id}{calendar-id}" ) @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response getParticipant( @ QueryParam ( "client-id" ) String client_id , @ QueryParam ( "application-id" ) String application_id , @ QueryParam ( "event-id" ) String event_id , @ QueryParam ( "calendar-id" ) String calendar_id ) {
		String result = "";
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		try {
			//<--------------If client-id or application-id is not provided-------------->
			if ( utility.parameterCheckNull(client_id) ) {
				return Response.serverError()
						.entity(json_error("client-id cannot be null.", "error")).build();
			}
			if ( utility.parameterCheckNull(application_id) ) {
				return Response.serverError()
						.entity(json_error("application-id cannot be null.", "error")).build();
			}
			if ( utility.parameterCheckNull(event_id) ) {
				return Response.serverError()
						.entity(json_error("event-id cannot be null.", "error")).build();
			}
			if ( utility.parameterCheckNull(calendar_id) ) {
				return Response.serverError()
						.entity(json_error("calendar-id cannot be null.", "error")).build();
			}

			//<--------------------Validate client-id and application-id-------------------->
			if ( !utility.clientApplicationIDValidate(db_user, db_password, jdbc, client_id,
					application_id) ) {
				return Response.serverError()
						.entity(json_error("Invalid client-id or application-id.", "error"))
						.build();
			}
			result = setParticipantData(db_user, db_password, jdbc, client_id, application_id,
					event_id, calendar_id);
			if ( result.equals("not_found") ) {
				return Response.serverError().entity(json_error("Event Not Found.", "error"))
						.build();
			}

			result = getParticipant(db_user, db_password, jdbc, client_id, application_id,
					event_id, calendar_id);

			if ( result.equals("not_found") ) {
				return Response.serverError()
						.entity(json_error("Participant Record Not Found.", "error")).build();
			} else {
				return Response.ok(result).build();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}

	//------------------------------------------ Get Participant Details Function --------------------------------------------//
	@ GET @ Path ( "/{id}" ) @ QueryParam ( "{client-id}{application-id}{event-id}" ) @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response getParticipantDetails( @ PathParam ( "id" ) String participant_id , @ QueryParam ( "client-id" ) String client_id , @ QueryParam ( "application-id" ) String application_id , @ QueryParam ( "event-id" ) String event_id ) {
		String result = "";
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		String contactAPIUrl = context.getInitParameter("contact_api_url");
		try {
			//<--------------If client-id or application-id is not provided-------------->
			if ( utility.parameterCheckNull(client_id) ) {
				return Response.serverError()
						.entity(json_error("client-id cannot be null.", "error")).build();
			}
			if ( utility.parameterCheckNull(application_id) ) {
				return Response.serverError()
						.entity(json_error("application-id cannot be null.", "error")).build();
			}
			if ( utility.parameterCheckNull(event_id) ) {
				return Response.serverError()
						.entity(json_error("event-id cannot be null.", "error")).build();
			}
			if ( utility.parameterCheckNull(participant_id) ) {
				return Response.serverError()
						.entity(json_error("participant-id cannot be null.", "error")).build();
			}

			//<--------------------Validate client-id and application-id-------------------->
			if ( !utility.clientApplicationIDValidate(db_user, db_password, jdbc, client_id,
					application_id) ) {
				return Response.serverError()
						.entity(json_error("Invalid client-id or application-id.", "error"))
						.build();
			}

			result = getParticipantDetails(db_user, db_password, jdbc, contactAPIUrl, client_id,
					application_id, event_id, participant_id);

			if ( result.equals("not_found") ) {
				return Response.serverError()
						.entity(json_error("Participant Record Not Found.", "error")).build();
			}
			if ( result.equals("contactAPIError") ) {
				return Response.serverError().entity(json_error("Contact API Error.", "error"))
						.build();
			}
			return Response.ok(result).build();
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}

	//------------------------------------------ Create Participant Function --------------------------------------------//
	@ POST @ Produces ( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response create_participant( JSONObject json ) {
		String result = "";
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc");
		String contactAPIUrl = context.getInitParameter("contact_api_url");

		String calendar_id = json.optString("calendar-id");
		String calendar_name = json.optString("calendar-name");
		String client_id = json.optString("client-id");
		String application_id = json.optString("application-id");
		String event_id = json.optString("event-id");
		String user_id = json.optString("user-id");
		contact_id = json.optString("contact-id");

		try {
			if ( utility.parameterCheckNull(client_id)
					|| utility.parameterCheckNull(application_id) ) {
				JSONObject obj = new JSONObject();
				obj.put("response-type", "error");
				obj.put("response-description", "Must provide client-id and application-id.");
				return Response.serverError().entity(obj.toString()).build();
			}

			if ( utility.parameterCheckNull(event_id) ) {
				JSONObject obj = new JSONObject();
				obj.put("response-type", "error");
				obj.put("response-description", "Must provide event-id.");
				return Response.serverError().entity(obj.toString()).build();
			}

			if ( !utility.clientApplicationIDValidate(db_user, db_password, jdbc, client_id,
					application_id) ) {
				logger.error("invalid client-id/application-id.");
				return Response.serverError()
						.entity(json_error("invalid client-id/application-id.", "error")).build();
			}

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

			String getContactsResult = getContactsArray(json);

			if ( getContactsResult.equals("missing_contacts") && user_id == null
					&& contact_id == null ) {
				return Response.serverError()
						.entity(json_error("Must provide contacts/user-id/contact-id", "error"))
						.build();
			} else if ( getContactsResult.equals("missing_data") ) {
				return Response.serverError()
						.entity(json_error("Data missing in contacts.", "error")).build();
			}

			String firstnameArr[] = contact_firstname.split(",");
			String mobileArr[] = mobile_number.split(",");
			String emailArr[] = email_address.split(",");
			//Total participants from this request
			int numberOfParticipant = firstnameArr.length;

			//------------------ Checking for current number of participants ----------------//
			String participant_type = utility.getParticipantType(db_user, db_password, jdbc,
					event_id);
			if ( participant_type.equals("notfound") ) {
				return Response.serverError()
						.entity(json_error("Event-id Not Found In table_appointment.", "error"))
						.build();
			}
			if ( !participant_type.equals("open") ) {
				//Total participants in table_participant
				int totalParticipants = utility.getNumberOfParticipants(db_user, db_password, jdbc,
						client_id, application_id, event_id);
				//no_of_participants for the event (appointment)
				String maxParticipant = utility.getMaxParticipant(db_user, db_password, jdbc,
						event_id);

				if ( maxParticipant.equals("notfound") ) {
					return Response
							.serverError()
							.entity(json_error("Event-id Not Found In table_appointment.", "error"))
							.build();
				} else if ( Integer.parseInt(maxParticipant) == 0 ) {
					return Response
							.serverError()
							.entity(json_error("Max number of participant is: " + maxParticipant
									+ ", current participants: " + totalParticipants + ".", "error"))
							.build();
				} else if ( numberOfParticipant > Integer.parseInt(maxParticipant) ) {
					return Response
							.serverError()
							.entity(json_error("Max number of participant is: " + maxParticipant
									+ ", current participants: " + totalParticipants + ".", "error"))
							.build();
				} else if ( totalParticipants + numberOfParticipant > Integer
						.parseInt(maxParticipant) ) {
					return Response
							.serverError()
							.entity(json_error("Max number of participant is: " + maxParticipant
									+ ", current participants: " + totalParticipants + ".", "error"))
							.build();
				}
			}
			//------------------------------------ Create Function ---------------------------------------//
			result = createParticipant(db_user, db_password, jdbc, contactAPIUrl, client_id,
					application_id, event_id, calendar_id, calendar_name, firstnameArr, mobileArr,
					emailArr, user_id);

			if ( !result.equals("success") ) {
				return Response.serverError().entity(json_error(result, "error")).build();
			} else {
				JSONObject obj = new JSONObject();
				obj.put("response-type", "success");
				obj.put("participant", participantArray);
				obj.put("response-description", numberOfParticipant
						+ " participants added to the booking.");
				result = obj.toString();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
		logger.info(result);
		return Response.ok(result).build();
	}

	//------------------------------------------------------------------------ API Methods (SQL Transactions) -------------------------------------------------------------------------------//
	public String deleteParticipant( String db_user , String db_password , String jdbc , String client_id , String application_id , String event_id , String participant_id , String user_id ) throws ClassNotFoundException , SQLException , IOException , org.json.simple.parser.ParseException , JSONException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		String deleteParticipantSQL = "DELETE FROM table_participant WHERE appointment_id=?";
		PreparedStatement st = null;

		//-------------------------------- Preparing SQL -----------------------------------//
		if ( client_id != null && participant_id != null ) {
			deleteParticipantSQL += " AND client_id=? AND application_id=? AND participant_id=?";
			st = con.prepareStatement(deleteParticipantSQL);
			st.setInt(1, Integer.parseInt(event_id));
			st.setString(2, client_id);
			st.setString(3, application_id);
			st.setInt(4, Integer.parseInt(participant_id));
		} else if ( client_id != null && user_id != null ) {
			deleteParticipantSQL += " AND client_id=? AND application_id=? AND user_id=?";
			st = con.prepareStatement(deleteParticipantSQL);
			st.setInt(1, Integer.parseInt(event_id));
			st.setString(2, client_id);
			st.setString(3, application_id);
			st.setInt(4, Integer.parseInt(user_id));
		} else {
			db.close();
			return "sql_error";
		}
		int rowsAffected = st.executeUpdate();
		if ( rowsAffected > 0 ) {
			db.close();
			JSONObject response = new JSONObject();
			response.put("response-type", "success");
			response.put("response-description", "participant deleted");
			return response.toString();
		} else {
			db.close();
			return "notfound";
		}
	}

	/*
	 * Methods for create participant/get participant/get participant details
	 */
	public String getParticipantDetails( String db_user , String db_password , String jdbc , String contact_api_url , String client_id , String application_id , String event_id , String participant_id ) throws Exception {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("SELECT * FROM table_participant WHERE client_id=? AND application_id=? AND appointment_id=? AND participant_id=?");
		st.setString(1, client_id);
		st.setString(2, application_id);
		st.setInt(3, Integer.parseInt(event_id));
		st.setInt(4, Integer.parseInt(participant_id));

		ResultSet rs = st.executeQuery();
		JSONObject response = new JSONObject();
		String contactAPIResult;
		boolean success;
		if ( rs.next() ) {
			response.put("calendar-id", rs.getString("calendar_id"));
			response.put("contact-id", rs.getString("contact_id"));
			response.put("confirmation-status", rs.getString("confirmation_status"));

			contactAPIResult = invokeContactAPI_GET(contact_api_url, client_id, application_id,
					rs.getString("contact_id"));

			if ( contactAPIResult.equals("success") ) {
				response.put("contact-firstname", getContact_firstname());
				response.put("mobile-number", getMobile_number());
				response.put("email-address", getEmail_address());
				success = new Boolean("true");
			} else {
				success = new Boolean("false");
			}
		} else {
			db.close();
			return "not_found";
		}
		if ( success ) {
			db.close();
			return response.toString();
		} else {
			return "contactAPIError";
		}
	}

	public String createParticipant( String db_user , String db_password , String jdbc , String contactAPIUrl , String client_id , String application_id , String event_id , String calendar_id , String calendar_name , String[] firstnameArr , String[] mobileArr , String[] emailArr , String user_id ) throws ClassNotFoundException , SQLException , IOException , org.json.simple.parser.ParseException , JSONException {
		//---- Invoker Contact API To Get Contact-id ----//
		int size = firstnameArr.length;
		Database db = new Database(db_user, db_password, jdbc);
		String insertParticipantSQL = "INSERT INTO table_participant (client_id,application_id,calendar_id,calendar_name,appointment_id,contact_id,user_id) VALUES(?,?,?,?,?,?,?)";
		PreparedStatement st;
		for ( int i = 0 ; i < size ; i++ ) {
			String result = invokeContactAPI_POST(contactAPIUrl, client_id, application_id,
					firstnameArr[i], mobileArr[i], emailArr[i]);
			if ( !result.equals("success") ) {
				db.close();
				return result;
			}
			//---- Insert Into table_participant ----//
			st = db.getConnection().prepareStatement(insertParticipantSQL,
					Statement.RETURN_GENERATED_KEYS);
			st.setString(1, client_id);
			st.setString(2, application_id);
			st.setInt(3, Integer.parseInt(calendar_id));
			st.setString(4, calendar_name);
			st.setInt(5, Integer.parseInt(event_id));
			st.setInt(6, Integer.parseInt(getContact_id()));
			if ( user_id == null ) {
				st.setInt(7, 0);
			} else {
				st.setInt(7, Integer.parseInt(user_id));
			}
			st.executeUpdate();

			ResultSet generatedKeys = st.getGeneratedKeys();
			if ( generatedKeys.next() ) {
				setParticipant_id(generatedKeys.getString(1));
			} else {
				db.close();
				return "participant_id_error";
			}
			setParticipantDetailsArray(getParticipant_id(), getContact_id());
		}
		db.close();
		return "success";
	}

	private String getParticipant( String db_user , String db_password , String jdbc , String client_id , String application_id , String event_id , String calendar_id ) throws SQLException , ClassNotFoundException , JSONException {
		Database db = new Database(db_user, db_password, jdbc);
		String getEventQuery = "SELECT * FROM table_participant WHERE client_id=? AND application_id=? AND appointment_id=? AND calendar_id=?";
		Connection con = db.getConnection();
		PreparedStatement st = con.prepareStatement(getEventQuery);
		st.setString(1, client_id);
		st.setString(2, application_id);
		st.setInt(3, Integer.parseInt(event_id));
		st.setInt(4, Integer.parseInt(calendar_id));

		ResultSet rs = st.executeQuery();
		boolean found = false;
		JSONObject response = new JSONObject();
		response.put("calendar-id", calendar_id);
		response.put("participant-type", getParticipant_type());
		response.put("no-of-participants", getNo_of_participants());
		while ( rs.next() ) {
			found = true;
			setParticipantArray(rs.getString("participant_id"), rs.getString("contact_id"),
					rs.getString("confirmation_status"));
		}
		if ( !found ) {
			db.close();
			return "not_found";
		}
		response.put("participants", participantArray);

		db.close();
		return response.toString();

	}

	//------------------------------------------------------------------------ Methods To Invoke Other APIs -------------------------------------------------------------------------------//
	/*
	 * Methods to invoke contact and status APIs
	 */

	// HTTP GET request
	private String invokeContactAPI_GET( String contact_api_url , String client_id , String application_id , String contact_id ) throws Exception {
		String getURL = contact_api_url += "/" + contact_id + "?client-id=" + client_id
				+ "&application-id=" + application_id;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(getURL);
		// Getting Response From Notification API
		HttpResponse response = httpClient.execute(getRequest);
		// -------------------------- Responses --------------------------//
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		int status_code = response.getStatusLine().getStatusCode();
		if ( status_code == 401 ) {
			// unauthorized
			return "client-id/application-id unauthorized.";
		} else if ( status_code == 400 ) {
			// error
			JSONObject json = new JSONObject(responseString);
			String error = (String) json.get("response-description");
			return error;
		} else if ( status_code == 200 ) {
			// 200 ok
			JSONObject json = new JSONObject(responseString);
			contact_firstname = json.getString("contact-firstname");
			mobile_number = json.getString("mobile-number");
			email_address = json.getString("email-address");
			return "success";
		}
		return "unsuccessful";
	}

	// To invoke contact api 
	public String invokeContactAPI_POST( String contact_api_url , String client_id , String application_id , String contact_firstname , String mobile_number , String email_address ) throws IOException , org.json.simple.parser.ParseException , JSONException {
		//----------- Generating Request For Contact API -----------//
		JSONObject requestJSON = new JSONObject();
		requestJSON.put("client-id", client_id);
		requestJSON.put("application-id", application_id);
		requestJSON.put("contact-firstname", contact_firstname);
		requestJSON.put("mobile-number", mobile_number);
		requestJSON.put("email-address", email_address);

		JSONArray contactGroupArr = new JSONArray();
		JSONObject groupObject = new JSONObject();
		groupObject.put("group-name", "default");
		contactGroupArr.put(groupObject);
		requestJSON.put("contact-group", contactGroupArr);

		logger.info("Request Message: " + requestJSON.toString());
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(contact_api_url);
		StringEntity input = new StringEntity(requestJSON.toString());
		input.setContentType("application/json;charset=UTF-8");
		postRequest.setEntity(input);
		// Getting Response From Notification API
		HttpResponse response = httpClient.execute(postRequest);
		// -------------------------- Responses --------------------------//
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		int status_code = response.getStatusLine().getStatusCode();
		if ( status_code == 401 ) {
			// unauthorized
			return "client-id/application-id unauthorized.";
		} else if ( status_code == 400 ) {
			// error
			JSONObject json = new JSONObject(responseString);
			String error = (String) json.get("response-description");
			return error;
		} else if ( status_code == 200 ) {
			// 200 ok
			JSONObject json = new JSONObject(responseString);
			String contact_id = json.getString("contact-id");
			if ( contact_id.equals("") ) {
				return "contact-id Not Found.";
			} else {
				setContact_id(contact_id);
				return "success";
			}
		}
		return "unsuccessful";
	}

	//------------------------------------------------------------------------ Helper Methods -------------------------------------------------------------------------------//
	public String getContactsArray( JSONObject json ) throws JSONException {
		org.codehaus.jettison.json.JSONArray contacts;
		if ( !json.has("contacts") ) {
			return "missing_contacts";
		} else {
			contacts = json.getJSONArray("contacts");
		}
		for ( int i = 0 ; i < contacts.length() ; i++ ) {
			JSONObject p = (JSONObject) contacts.get(i);
			if ( p.has("contact-firstname") && p.has("mobile-number") && p.has("email-address") ) {
				String firstname = p.getString("contact-firstname");
				contact_firstname += firstname + ",";

				String mobile = p.getString("mobile-number");
				mobile_number += mobile + ",";

				String email = p.getString("email-address");
				email_address += email + ",";
			} else {
				return "missing_data";
			}
		}
		return "success";
	}

	/*
	 * Searches table_appointment and set participant-type and no-of-participants
	 */
	public String setParticipantData( String db_user , String db_password , String jdbc , String client_id , String application_id , String event_id , String calendar_id ) throws ClassNotFoundException , SQLException {
		Database db = new Database(db_user, db_password, jdbc);
		String getEventQuery = "SELECT * FROM table_appointment WHERE client_id=? AND application_id=? AND appointment_id=? AND calendar_id=?";
		Connection con = db.getConnection();
		PreparedStatement st = con.prepareStatement(getEventQuery);
		st.setString(1, client_id);
		st.setString(2, application_id);
		st.setInt(3, Integer.parseInt(event_id));
		st.setInt(4, Integer.parseInt(calendar_id));

		ResultSet rs = st.executeQuery();
		if ( rs.next() ) {
			setParticipant_type(rs.getString("participant_type"));
			setNo_of_participants(rs.getString("no_of_participants"));
		} else {
			db.close();
			return "not_found";
		}
		db.close();
		return "success";
	}

	public void setParticipantDetailsArray( String participant_id , String contact_id ) throws JSONException {
		JSONObject participant = new JSONObject();
		participant.put("participant-id", participant_id);
		participant.put("contact-id", contact_id);
		participantArray.put(participant);
	}

	public void setParticipantArray( String participant_id , String contact_id , String confirmation_status ) throws JSONException {
		JSONObject participant = new JSONObject();
		participant.put("participant-id", participant_id);
		participant.put("contact-id", contact_id);
		participant.put("confirmation-status", confirmation_status);
		participantArray.put(participant);
	}

	public String json_error( String error_message , String type ) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("response-type", type);
		obj.put("response-description", error_message);
		return obj.toString();
	}

	public String getContact_firstname() throws JSONException {
		return contact_firstname;
	}

	public void setContact_firstname( String contact_firstname ) {
		this.contact_firstname = contact_firstname;
	}

	public String getMobile_number() {
		return mobile_number;
	}

	public void setMobile_number( String mobile_number ) {
		this.mobile_number = mobile_number;
	}

	public String getEmail_address() {
		return email_address;
	}

	public void setEmail_address( String email_address ) {
		this.email_address = email_address;
	}

	public void setContact_id( String id ) {
		this.contact_id = id;
	}

	public String getContact_id() {
		return this.contact_id;
	}

	public String getParticipant_id() {
		return participant_id;
	}

	public void setParticipant_id( String participant_id ) {
		this.participant_id = participant_id;
	}

	public String getParticipant_type() {
		return participant_type;
	}

	public void setParticipant_type( String participant_type ) {
		this.participant_type = participant_type;
	}

	public String getNo_of_participants() {
		return no_of_participants;
	}

	public void setNo_of_participants( String no_of_participants ) {
		this.no_of_participants = no_of_participants;
	}

	public String getConfirmation_status() {
		return confirmation_status;
	}

	public void setConfirmation_status( String confirmation_status ) {
		this.confirmation_status = confirmation_status;
	}

}
