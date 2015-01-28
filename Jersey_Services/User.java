package Jersey_Services;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
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
import User_Functions.Create;
import User_Functions.Verify;

@ Path ( "user" ) public class User {
	@ Context protected ServletContext context;
	private String db_user;
	private String db_password;
	private String jdbc;
	private String jdbc_users;
	private Map<String, String> SQLqueries = new HashMap<String, String>();
	private String[] notifier2Tables = new String[23];

	private static Logger logger = Logger.getLogger(User.class);
	private final static int USER_ID = 1;
	private final static int SEARCH_RESULT = 0;

	public User () {
		SQLqueries.put("users", "DELETE FROM users WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_appointment",
				"DELETE FROM table_appointment WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_account",
				"DELETE FROM table_account WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_calendar",
				"DELETE FROM table_calendar WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_calendat_schedule",
				"DELETE FROM table_calendar_schedule WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_configuration",
				"DELETE FROM table_configuration WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_route_rules_sms",
				"DELETE FROM table_calendar_schedule WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_participant",
				"DELETE FROM table_participant WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_route_rules_from",
				"DELETE FROM table_route_rules_from WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_route_rules_sms",
				"DELETE FROM table_route_rules_message WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_route_rules_to",
				"DELETE FROM table_route_rules_to WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_campaign",
				"DELETE FROM table_campaign WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_campaign_code",
				"DELETE FROM table_campaign_code WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_campaign_transactions",
				"DELETE FROM table_campaign_transactions WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_permission",
				"DELETE FROM table_permission WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_profile",
				"DELETE FROM table_profile WHERE client_id=? AND application_id = ?");
		SQLqueries
				.put("table_recipientlist_recipient",
						"delete from table_recipientlist_recipient USING table_recipient_list WHERE (select id from table_recipient_list WHERE"
								+ " table_recipient_list.client_id=? AND table_recipient_list.application_id=?) = table_recipientlist_recipient.recipient_list_id");
		SQLqueries.put("table_recipient_list",
				"DELETE FROM table_recipient_list WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_recurrence",
				"DELETE FROM table_recurrence WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_report",
				"DELETE FROM table_report WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_rule",
				"DELETE FROM table_rule WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_source_verification",
				"DELETE FROM table_source_verification WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_transaction",
				"DELETE FROM table_profile WHERE client_id=? AND application_id = ?");
		SQLqueries.put("table_unsubscription",
				"DELETE FROM table_unsubscription WHERE client_id=? AND application_id = ?");

		notifier2Tables[0] = "users";
		notifier2Tables[1] = "table_appointment";
		notifier2Tables[2] = "table_account";
		notifier2Tables[3] = "table_calendar";
		notifier2Tables[4] = "table_calendat_schedule";
		notifier2Tables[5] = "table_configuration";
		notifier2Tables[6] = "table_route_rules_sms";
		notifier2Tables[7] = "table_participant";
		notifier2Tables[8] = "table_route_rules_from";
		notifier2Tables[9] = "table_route_rules_sms";
		notifier2Tables[10] = "table_route_rules_to";
		notifier2Tables[11] = "table_campaign";
		notifier2Tables[12] = "table_campaign_code";
		notifier2Tables[13] = "table_campaign_transactions";
		notifier2Tables[14] = "table_permission";
		notifier2Tables[15] = "table_profile";
		notifier2Tables[16] = "table_recipientlist_recipient";
		notifier2Tables[17] = "table_recipient_list";
		notifier2Tables[18] = "table_report";
		notifier2Tables[19] = "table_rule";
		notifier2Tables[20] = "table_source_verification";
		notifier2Tables[21] = "table_transaction";
		notifier2Tables[22] = "table_unsubscription";
	}

	@ DELETE @ QueryParam ( "{client-id}{application-id}" ) @ Produces ( {
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response userStatus( @ QueryParam ( "client-id" ) String client_id , @ QueryParam ( "application-id" ) String application_id ) {
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc_users = context.getInitParameter("jdbc_users");
		jdbc = context.getInitParameter("jdbc");

		try {

			if ( utility.parameterCheckNull(client_id) ) {
				return Response.serverError()
						.entity(utility.json_error("Must Provide client-id.", "error")).build();
			}
			if ( utility.parameterCheckNull(application_id) ) {
				return Response.serverError()
						.entity(utility.json_error("Must Provide application-id.", "error"))
						.build();
			}
			if ( !utility.clientApplicationIDValidate(db_user, db_password, jdbc, client_id,
					application_id) ) {
				return Response.serverError()
						.entity(utility.json_error("Invalid client-id/application-id.", "error"))
						.build();
			}

			String userType = getUserType(db_user, db_password, jdbc_users, client_id,
					application_id);

			if ( userType == null ) {
				return Response.serverError()
						.entity(utility.json_error("user_type does not exist.", "error")).build();
			}
			if ( userType.equals("notfound") ) {
				return Response
						.serverError()
						.entity(utility.json_error("No user found with this combination.", "error"))
						.build();
			}
			if ( !userType.equalsIgnoreCase("registered") ) {
				return Response.serverError()
						.entity(utility.json_error("User is un-registered.", "error")).build();
			}

			String result = deleteUser(db_user, db_password, jdbc, jdbc_users, client_id,
					application_id);

			if ( !result.equals("success") ) {
				return Response.serverError().entity(utility.json_error(result, "error")).build();
			} else {
				JSONObject response = new JSONObject();
				response.put("response-type", "success");
				response.put("response-description", "user with client-id " + client_id
						+ " has been deleted.");
				return Response.ok(response.toString()).build();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}

	@ POST @ Produces ( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } ) public Response user_create( JSONObject json ) {
		String result = "";
		db_user = context.getInitParameter("db_user");
		db_password = context.getInitParameter("db_password");
		jdbc = context.getInitParameter("jdbc_users");
		String notification_api_url = context.getInitParameter("notification_api_url");

		try {
			Create cr = new Create(json.optString("user-name"), json.optString("password"),
					json.optString("firstname"), json.optString("lastname"),
					json.optString("email-address"), json.optString("mobile-number"),
					json.optString("country-code"), json.optString("company-name"),
					json.optString("timezone"), json.optString("verification-method"),
					json.optString("user-type"), json.optString("client-id"),
					json.optString("application-id"), notification_api_url);

			result = cr.create_user(db_user, db_password, jdbc);
			String resultArr[] = result.split("/");
			
			if ( resultArr[SEARCH_RESULT].equals("success") ) {
				JSONObject obj = new JSONObject();
				obj.put("response-type", "success");
				obj.put("response-description", "new user " + cr.getUser_name()
						+ " has been created");
				obj.put("response-state", "verification-required");
				obj.put("user-id", resultArr[USER_ID]);
				result = obj.toString();
				return Response.ok(result).build();
			}
			
			if ( resultArr[SEARCH_RESULT].equals("updated") ) {
				JSONObject obj = new JSONObject();
				obj.put("response-type", "success");
				obj.put("response-description",
						"Unregistered User Already Exists, New Verification Code Has Been Sent.");
				obj.put("response-state", "verification-required");
				obj.put("user-id", resultArr[USER_ID]);
				result = obj.toString();
				return Response.ok(result).build();
			}
			
			if ( resultArr[SEARCH_RESULT].equals("user_exists") ) {
				JSONObject obj = new JSONObject();
				obj.put("response-type", "success");
				obj.put("response-description",
						"User Is Registered And Already Exists In The Database..");
				obj.put("user-id", resultArr[USER_ID]);
				result = obj.toString();
				return Response.ok(result).build();
			}
			
			if ( !result.equals("success") ) {
				logger.error(result);
				JSONObject obj = new JSONObject();
				obj.put("response-type", "error");
				obj.put("response-description", result);
				result = obj.toString();
				return Response.serverError().entity(result).build();
			}
			
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
		logger.info(result);
		return Response.ok(result).build();
	}

	@ POST @ Path ( "/verify" ) @ Produces ( { MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_XML } ) public Response user_verify( JSONObject json ) {
		String result = "";
		try {
			db_user = context.getInitParameter("db_user");
			db_password = context.getInitParameter("db_password");
			jdbc = context.getInitParameter("jdbc_users");

			if ( utility.parameterCheckNull(json.optString("verification-type")) ) {
				return Response.serverError()
						.entity(json_error("verification-type is missing.", "error")).build();
			}
			if ( utility.parameterCheckNull(json.optString("verification-value")) ) {
				return Response.serverError()
						.entity(json_error("verification-value is missing.", "error")).build();
			}
			if ( utility.parameterCheckNull(json.optString("verification-code")) ) {
				return Response.serverError()
						.entity(json_error("verification-code is missing.", "error")).build();
			}

			else {
				Verify verify = new Verify(json.optString("verification-type"),
						json.optString("verification-value"), json.optString("verification-code"));
				result = verify.verify_user(db_user, db_password, jdbc);
				if ( result.equals("success") ) {
					JSONObject obj = new JSONObject();
					obj.put("response-type", "success");
					obj.put("response-description", "user: " + verify.getUserName()
							+ " has been verified");
					obj.put("response-state", "verification-completed");
					result = obj.toString();
				} else if ( result.equals("exist") ) {
					JSONObject obj = new JSONObject();
					obj.put("response-type", "success");
					obj.put("response-description", "Found Existing User");
					obj.put("response-state", "verification-completed");
					result = obj.toString();
				} else {
					return Response.serverError().entity(verify_json_error(result)).build();
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			result = verify_json_error(e.toString());
			logger.info(e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
		return Response.ok(result).build();
	}

	public String verify_json_error( String message ) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("response-type", "error");
			obj.put("response-description", message);
			obj.put("response-state", "verification-required");
		} catch ( JSONException e ) {
			logger.error(e.toString());
			return (e.toString());
		}
		return obj.toString();
	}

	public String json_error( String error_message , String type ) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("response-type", type);
		obj.put("response-description", error_message);
		return obj.toString();
	}

	public String deleteUser( String db_user , String db_password , String jdbc , String jdbc_users , String client_id , String application_id ) throws ClassNotFoundException , SQLException {
		//Connection to users database
		Database db1 = new Database(db_user, db_password, jdbc_users);
		Connection con1 = db1.getConnection();
		//Connection to notifier2 database
		Database db2 = new Database(db_user, db_password, jdbc);
		Connection con2 = db2.getConnection();

		con1.setAutoCommit(false);
		String result = "";

		try {
			PreparedStatement st = con1.prepareStatement(SQLqueries.get(notifier2Tables[0]));
			st.setString(1, client_id);
			st.setString(2, application_id);

			int rowsAffected = st.executeUpdate();
			if ( rowsAffected > 0 ) {
				con2 = db2.getConnection();
				con2.setAutoCommit(false);
				for ( int i = 1 ; i < notifier2Tables.length ; i++ ) {
					st = con2.prepareStatement(SQLqueries.get(notifier2Tables[i]));
					st.setString(1, client_id);
					st.setString(2, application_id);
					st.execute();
				}
				con1.commit();
				con2.commit();
				result = "success";
			} else {
				result = "User could not be found.";
			}

		} catch ( Exception e ) {
			con1.rollback();
			con2.rollback();
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			logger.error(exceptionAsString);
			result = "SQL Error Occured.";
		} finally {
			con1.close();
			con2.close();
		}

		return result;
	}

	public String getUserType( String db_user , String db_password , String jdbc_users , String client_id , String application_id ) throws ClassNotFoundException , SQLException {
		Database db = new Database(db_user, db_password, jdbc_users);
		Connection con = db.getConnection();
		String deleteUserSQL = "SELECT client_id,application_id,user_type FROM users WHERE client_id=? AND application_id = ?";
		PreparedStatement st = con.prepareStatement(deleteUserSQL);
		st.setString(1, client_id);
		st.setString(2, application_id);
		boolean found = false;
		ResultSet rs = st.executeQuery();
		String user_type = "notfound";
		if ( rs.next() ) {
			found = true;
			user_type = rs.getString("user_type");
		}
		con.close();
		if ( !found ) {
			return "notfound";
		} else {
			return user_type;
		}
	}

}
