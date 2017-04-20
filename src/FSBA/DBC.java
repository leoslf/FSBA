package FSBA;
import java.sql.*;
import org.sqlite.*;
import static FSBA.Macro.*;

class DBC {
	/** Java Database Connection (JDBC) instance */
	private Connection conn;
	/** Statement to be prepared */
	private Statement statement;
	/** Database type */
	private String dbType = "sqlite";
	/** Database file: for SQLite */
	private String dbFile = "all_proj.db";

	/**
	 * Create Java Database Connection to specific file of sqlite
	 * Add exit hook to close connection
	 */
	public DBC() {
		Runtime.getRuntime().addShutdownHook(new Thread() { public void run() { DBC.this.close(); } });
		//debug("DB Begin");
		try {
			conn = DriverManager.getConnection("jdbc:" + dbType + ":" + dbFile);
			check(conn!=null);
			logInfo("DB Connected");
		} catch (Exception e) {
			logException(e);
		} 
		//debug("DB End");
	}
	
	/**
	 * Query given SQL statement to database
	 * @param sql SQL statement 
	 * @return
	 */
	private ResultSet query(String sql) {
		//debug("query Begin");
		ResultSet rs = null;
		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(sql);
		} catch (SQLException e) {
			logException(e);
		} 
		//debug("query End");
		return rs;
	}
	
	/** Parameterized query: SELECT */
	public ResultSet select(String columns, String table, String ...conditions) {
		return query("SELECT " + columns + " FROM " + table + " " + (conditions != null && conditions.length > 0 ? conditions[0] : ""));
	}
	
	/**
	 * Close Database connection
	 */
	public void close() {
		try {
            if (conn != null) {
                conn.close();
    			logInfo("DB closed successful");
            }
        } catch (Exception e) {
            logException(e);
        }
	}
}
