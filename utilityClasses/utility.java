package utilityClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class utility {
	
	//---------------------- Generate six digit code ----------------------//
	public static String generateCode() {
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		for ( int i = 0 ; i < 10 ; i++ ) {
			numbers.add(i);
		}
		Collections.shuffle(numbers);
		String result = "";
		for ( int i = 0 ; i < 6 ; i++ ) {
			result += numbers.get(i).toString();
		}
		return result;
	}
	
	
	//---------------------- Check if a string is null or empty ----------------------//
	public static boolean parameterCheckNull( String param ) {
		if ( param == null ) {
			return true;
		} else if ( param.equals("") ) {
			return true;
		}
		return false;
	}
	//---------------------- Validate client-id and application-id combination ----------------------//
	public static boolean clientApplicationIDValidate( String db_user , String db_password , String jdbc , String client_id , String application_id ) throws ClassNotFoundException , SQLException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st;
		String query = "SELECT client_id, application_id FROM table_account WHERE client_id = ? AND application_id = ? ";
		st = con.prepareStatement(query);
		st.setString(1, client_id);
		st.setString(2, application_id);
		ResultSet rs = st.executeQuery();

		boolean valid;
		if ( rs.next() ) {
			valid = true;
		} else {
			valid = false;
		}
		db.close();
		return valid;
	}

	/**
	 * Takes database user,password,jdbc and calendar_name,client_id,application_id
	 *
	 * Finds the calendar_id from table_calendar based on calendar_name
	 *
	 * @return calendar_id if found/return notfound 
	 */
	public static String getCalendarID( String db_user , String db_password , String jdbc , String calendar_name , String client_id , String application_id ) throws SQLException , ClassNotFoundException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("SELECT calendar_id,name,client_id,application_id from table_calendar where name = ? AND client_id = ? AND application_id = ?");
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

	public static String getParticipantType( String db_user , String db_password , String jdbc , String appointment_id ) throws SQLException , ClassNotFoundException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("SELECT appointment_id,participant_type FROM table_appointment WHERE appointment_id=?");
		st.setInt(1, Integer.parseInt(appointment_id));
		ResultSet rs = st.executeQuery();
		String participant_type = "";
		if ( rs.next() ) {
			participant_type = rs.getString("participant_type");
		} else {
			participant_type = "notfound";
		}
		db.close();
		return participant_type;
	}

	public static String getMaxParticipant( String db_user , String db_password , String jdbc , String appointment_id ) throws SQLException , ClassNotFoundException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("SELECT appointment_id,no_of_participants FROM table_appointment WHERE appointment_id=?");
		st.setInt(1, Integer.parseInt(appointment_id));
		ResultSet rs = st.executeQuery();
		String no_of_participants = "";
		if ( rs.next() ) {
			no_of_participants = rs.getString("no_of_participants");
		} else {
			no_of_participants = "notfound";
		}
		db.close();
		return no_of_participants;
	}

	public static int getNumberOfParticipants( String db_user , String db_password , String jdbc , String client_id , String application_id , String appointment_id ) throws SQLException , ClassNotFoundException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("SELECT appointment_id,confirmation_status,client_id,application_id FROM table_participant WHERE appointment_id=? AND client_id=? AND application_id=?");
		st.setInt(1, Integer.parseInt(appointment_id));
		st.setString(2, client_id);
		st.setString(3, application_id);
		ResultSet rs = st.executeQuery();
		int rowCount = getRowCount(rs);
		db.close();
		return rowCount;
	}

	public static int getNumberOfConfirmedParticipants( String db_user , String db_password , String jdbc , String client_id , String application_id , String appointment_id ) throws SQLException , ClassNotFoundException {
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("SELECT appointment_id,confirmation_status,client_id,application_id FROM table_participant WHERE appointment_id=? AND client_id=? AND application_id=?");
		st.setInt(1, Integer.parseInt(appointment_id));
		st.setString(2, client_id);
		st.setString(3, application_id);
		ResultSet rs = st.executeQuery();
		int no_of_confirmed_participant = 0;
		while ( rs.next() ) {
			String confirmation_status = rs.getString("confirmation_status");
			if ( confirmation_status != null ) {
				if ( rs.getString("confirmation_status").equals("confirmed") ) {
					no_of_confirmed_participant++;
				}
			}
		}
		db.close();
		return no_of_confirmed_participant;
	}

	private static int getRowCount( ResultSet resultSet ) throws SQLException {
		int count = 0;
		while ( resultSet.next() ) {
			count++;
		}
		return count;
	}

	//------------------ Get client-id and application-id by key -----------------//
	public static String[] getClientApplicationID( String db_user , String db_password , String jdbc , String key) throws SQLException , ClassNotFoundException {
		String ClientApplicationID[] = new String[2];
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("SELECT client_id,application_id,key FROM table_account WHERE key=?");
		st.setString(1, key);
		ResultSet rs = st.executeQuery();
		if(rs.next()){
			ClientApplicationID[0] = rs.getString("client_Id");
			ClientApplicationID[1] = rs.getString("application_Id");
		}
		else{
			db.close();
			return null;
		}
		db.close();
		return ClientApplicationID;
	}
	
	public static String getUserIdByEventId( String db_user , String db_password , String jdbc , String event_id,String client_id,String application_id,String calendar_id) throws SQLException , ClassNotFoundException {
		
		Database db = new Database(db_user, db_password, jdbc);
		Connection con = db.getConnection();
		PreparedStatement st = con
				.prepareStatement("SELECT appointment_id,user_id FROM table_participant WHERE appointment_id=? AND client_id=? AND application_id=? AND calendar_id=?");
		st.setInt(1, Integer.parseInt(event_id));
		st.setString(2, client_id);
		st.setString(3, application_id);
		st.setInt(4, Integer.parseInt(calendar_id));
		ResultSet rs = st.executeQuery();
		String user_id = "";
		if(rs.next()){
			user_id = rs.getString("user_id");
		}
		else{
			db.close();
			return "not_found";
		}
		db.close();
		return user_id;
	}
	
	/*
	 * Compose JSON error response
	 */
	public static String json_error( String error_message , String type ) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("response-type", type);
		obj.put("response-description", error_message);
		return obj.toString();
	}
	
}
