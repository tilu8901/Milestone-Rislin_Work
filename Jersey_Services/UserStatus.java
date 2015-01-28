package Jersey_Services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.parser.ParseException;

import utilityClasses.Database;
import utilityClasses.utility;

@ Path ( "user/status" ) public class UserStatus {

	@ Context protected ServletContext context;
	private String db_user;
	private String db_password;
	private String jdbc , jdbc_users;
	private String notification_api_url;
	private String APIResult = "success";
	private final static int CLIENT_ID = 0;
	private final static int APPLICATION_ID = 1;
	private final static int ARRAY_SIZE = 2;
	private Map<String, String> verification_state_map;
	private static Logger logger = Logger.getLogger(UserStatus.class);
	public UserStatus () {
		verification_state_map = new HashMap<String, String>();
		verification_state_map.put("email-sent", "Verification-email-sent");
		verification_state_map.put("sms-sent", "Verification-sms-sent");
		verification_state_map.put("validated", "Verification-completed");
	}

	//------------------------------------------ User Status Function --------------------------------------------//
	@ GET @ QueryParam ( "{email-address}{mobile-number}{verification}{key}{rule-id}{rule-name}" ) @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response userStatus( @ QueryParam ( "email-address" ) String email_address , @ QueryParam ( "mobile-number" ) String mobile_number , @ QueryParam ( "verification" ) String verification , @ QueryParam ( "key" ) String key , @ QueryParam ( "rule-id" ) String rule_id , @ QueryParam ( "rule-name" ) String rule_name ) {
		String result = "";
		String[] ClientApplicationID = new String[ARRAY_SIZE];
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc_users = context.getInitParameter("jdbc_users");
		jdbc = context.getInitParameter("jdbc");

		setNotification_api_url(context.getInitParameter("notification_api_url"));

		try {

			if ( utility.parameterCheckNull(email_address)
					&& utility.parameterCheckNull(mobile_number) ) {
				return Response
						.serverError()
						.entity(json_error("Must Provide email-address or mobile-number.", "error"))
						.build();
			}
			if ( utility.parameterCheckNull(key) ) {
				return Response.serverError().entity(json_error("Must Provide key.", "error"))
						.build();
			}
			ClientApplicationID = utility.getClientApplicationID(db_user, db_password, jdbc, key);
			if ( ClientApplicationID == null ) {
				return Response.serverError().entity(json_error("Account Not Found.", "error"))
						.build();
			}

			if ( !utility.parameterCheckNull(verification) ) {
				if ( utility.parameterCheckNull(rule_id) & utility.parameterCheckNull(rule_name) ) {
					return Response.serverError()
							.entity(json_error("Must Provide rule-id or rule-name.", "error"))
							.build();
				}
			}
			result = getStatus(db_user, db_password, jdbc_users, email_address, mobile_number,
					verification, ClientApplicationID, rule_id, rule_name);

			if ( !getAPIResult().equals("success") ) {
				return Response.serverError().entity(json_error(getAPIResult(), "error")).build();
			}
			if ( result.equals("not_found") ) {
				return Response.serverError().entity(json_error("User Not Found.", "error"))
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

	public String getStatus( String db_user , String db_password , String jdbc , String email_address , String mobile_number , String verification , String[] ClientApplicationID , String rule_id , String rule_name ) throws ClassNotFoundException , SQLException , JSONException , IOException , ParseException {
		Database db = new Database(db_user, db_password, jdbc);
		String getUserQuery = "";
		Connection con = db.getConnection();
		PreparedStatement st = null;

		//---------------------------- Preparing SQL -------------------------------//
		if ( mobile_number != null ) {
			getUserQuery = "SELECT validation_code,mobile_number,  validation_status,user_status,user_id FROM users WHERE mobile_number=?";
			if ( ClientApplicationID[CLIENT_ID] != null
					&& ClientApplicationID[APPLICATION_ID] != null ) {
				getUserQuery += "AND client_id=? AND application_id=?";
				st = con.prepareStatement(getUserQuery);
				st.setString(1, mobile_number);
				st.setString(2, ClientApplicationID[CLIENT_ID]);
				st.setString(3, ClientApplicationID[APPLICATION_ID]);
			} else {
				st = con.prepareStatement(getUserQuery);
				st.setString(1, mobile_number);
			}
		} else if ( email_address != null ) {
			getUserQuery = "SELECT validation_code,email_address, validation_status,user_status,user_id FROM users WHERE email_address=?";
			if ( ClientApplicationID[CLIENT_ID] != null
					&& ClientApplicationID[APPLICATION_ID] != null ) {
				getUserQuery += "AND client_id=? AND application_id=?";
				st = con.prepareStatement(getUserQuery);
				st.setString(1, email_address);
				st.setString(2, ClientApplicationID[CLIENT_ID]);
				st.setString(3, ClientApplicationID[APPLICATION_ID]);
			} else {
				st = con.prepareStatement(getUserQuery);
				st.setString(1, email_address);
			}
		}
		JSONObject response = new JSONObject();
		ResultSet rs = st.executeQuery();
		if ( rs.next() ) {
			String newCode = utility.generateCode();
			String newValidationStatus = updateUserTable(db_user, db_password, jdbc_users, newCode,
					ClientApplicationID[CLIENT_ID], ClientApplicationID[APPLICATION_ID],
					mobile_number, email_address);
			//------- Send email/sms -------//
			if ( verification != null ) {
				if ( verification.equals("all") ) {
					String request = generate_notification_request(ClientApplicationID,
							mobile_number, email_address, "sms", newCode, rule_id, rule_name);
					invoke_notification_api(getNotification_api_url(), request);

					request = generate_notification_request(ClientApplicationID, mobile_number,
							email_address, "email", newCode, rule_id, rule_name);
					invoke_notification_api(getNotification_api_url(), request);
				} else {
					String request = generate_notification_request(ClientApplicationID,
							mobile_number, email_address, verification, newCode, rule_id, rule_name);
					invoke_notification_api(getNotification_api_url(), request);
				}
			}
			String verification_state = verification_state_map.get(newValidationStatus
					.toLowerCase());

			response.put("response-type", "success");
			response.put("user-id", rs.getInt("user_id"));
			response.put("user-status", rs.getString("user_status"));
			response.put("verification-state", verification_state);
		} else {
			db.close();
			return "not_found";
		}
		db.close();
		return response.toString();
	}

	public String json_error( String error_message , String type ) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("response-type", type);
		obj.put("response-description", error_message);
		return obj.toString();
	}

	//--------------------------------------------------- Notification API --------------------------------------------------//

	// To invoke notification api
	public void invoke_notification_api( String notification_api_url , String request ) throws IOException , org.json.simple.parser.ParseException , JSONException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(notification_api_url);
		StringEntity input = new StringEntity(request);
		input.setContentType("application/json;charset=UTF-8");
		postRequest.setEntity(input);
		// Getting Response From Notification API
		HttpResponse response = httpClient.execute(postRequest);
		// -------------------------- Responses --------------------------//
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		int status_code = response.getStatusLine().getStatusCode();
		JSONObject json = new JSONObject(responseString);
		if ( status_code == 401 ) {
			// unauthorized
			setAPIResult("client-id/application-id unauthorized. (notification API)");
		} else if ( status_code == 400 ) {
			// error
			String error = json.getString("response-description");
			setAPIResult(error + " (notification API)");
		} else if ( status_code == 500 ) {
			// error
			setAPIResult("Notification API Error.");
		} else if ( status_code == 200 ) {
			// 200 ok
			JSONArray arr = json.getJSONArray("details");
			JSONObject details = arr.getJSONObject(0);
			String error = details.getString("error-message");
			boolean result = details.getBoolean("successful");
			if ( result == true ) {
				setAPIResult("success");
			} else {
				setAPIResult(error + " (notification API)");
			}
		} else {
			setAPIResult(responseString + " (notification API)");
		}
	}

	public String generate_notification_request( String ClientApplicationID[] , String mobile_number , String email_address , String verification , String returnedCode , String rule_id , String rule_name ) throws JSONException {
		JSONObject obj = new JSONObject();
		JSONArray contacts_array = new JSONArray();
		JSONObject contacts = new JSONObject();

		obj.put("client-id", ClientApplicationID[CLIENT_ID]);
		obj.put("application-id", ClientApplicationID[APPLICATION_ID]);

		// ------------------ delivery-time-relative ---------------------//
		JSONObject time_relative = new JSONObject();
		String seconds = "5";
		time_relative.put("value", seconds);
		time_relative.put("unit", "second");
		obj.put("delivery-time-relative", time_relative);
		obj.put("message-type", "system-message");
		obj.put("message-subtype", "all");
		//Setting rule_id/rule_name if exist
		if ( rule_id != null ) {
			obj.put("rule-id", rule_id);
		} else if ( rule_name != null ) {
			obj.put("rule-name", rule_name);
		}
		//Setting channel-type and contact details
		if ( verification.equals("sms") ) {
			obj.put("channel-type", "sms");
			contacts.put("mobile-number", mobile_number);
			contacts_array.put(contacts);
			obj.put("contacts", contacts_array);
		} else if ( verification.equals("email") ) {
			obj.put("channel-type", "email");
			contacts.put("email-address", email_address);
			contacts_array.put(contacts);
			obj.put("contacts", contacts_array);
		}

		// --------------- message-info object ---------------------//
		JSONObject message_info = new JSONObject();
		message_info.put("content", returnedCode);
		obj.put("message-info", message_info);

		return obj.toString();
	}

	public String updateUserTable( String db_user , String db_password , String jdbc , String newCode , String client_id , String application_id , String mobile_number , String email_address ) throws SQLException , ClassNotFoundException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		String updateValidationCodeSQL = "UPDATE users SET validation_code=?,validation_status=? WHERE client_id = ? AND application_id = ?";
		PreparedStatement st = null;
		String validation_status = "";
		if ( email_address != null ) {
			validation_status = "EMAIL-SENT";
			updateValidationCodeSQL += "AND email_address=?";
			st = con.prepareStatement(updateValidationCodeSQL);
			st.setString(1, newCode);
			st.setString(2, validation_status);
			st.setString(3, client_id);
			st.setString(4, application_id);
			st.setString(5, email_address);
		} else if ( mobile_number != null ) {
			validation_status = "SMS-SENT";
			updateValidationCodeSQL += "AND mobile_number=?";
			st = con.prepareStatement(updateValidationCodeSQL);
			st.setString(1, newCode);
			st.setString(2, validation_status);
			st.setString(3, client_id);
			st.setString(4, application_id);
			st.setString(5, mobile_number);
		}
		st.executeUpdate();
		con.close();
		return validation_status;
	}

	public void setAPIResult( String result ) {
		this.APIResult = result;
	}

	public String getAPIResult() {
		return this.APIResult;
	}

	public String getNotification_api_url() {
		return notification_api_url;
	}

	public void setNotification_api_url( String notification_api_url ) {
		this.notification_api_url = notification_api_url;
	}

}
