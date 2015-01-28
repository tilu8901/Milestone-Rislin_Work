package utilityClasses;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Model.log4j;

public class Database {
	private Connection con;
	private Statement st;
	private final String forName = "org.postgresql.Driver";
	private String JDBC;
	private String DATABASE_USER;
	private String DATABASE_PASS;

	private log4j logger = new log4j();

	public Database ( String user , String password , String jdbc ) throws ClassNotFoundException ,
			SQLException {
		JDBC = jdbc;
		DATABASE_USER = user;
		DATABASE_PASS = password;
		connect();
	}

	public void connect() throws ClassNotFoundException , SQLException {
		Class.forName(forName);
		con = DriverManager.getConnection(JDBC, DATABASE_USER, DATABASE_PASS);
		st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		logger.logger_info("Connection To Database Established.");
	}

	public Statement getStatement() {
		return st;
	}

	public Connection getConnection() {
		return con;
	}

	public void close() throws SQLException {
		if ( con != null ) {
			con.close();
		}
		if ( st != null ) {
			st.close();
		}
		logger.logger_info("Connection To Database Ended.");
	}
}
