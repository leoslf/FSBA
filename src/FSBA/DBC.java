package FSBA;
import java.io.*;
import java.sql.*;

//import org.sqlite.*;
import static FSBA.Macro.*;

/**
 * Database Connection with related methods provided
 * @author leosin
 *
 */
class DBC {
	/** Java Database Connection (JDBC) instance */
	private static Connection conn = null;
	/** Database type */
	private String dbType = "sqlite";
	/** Database file: for SQLite */
	private String dbFile = new File(".").getAbsolutePath() + "/all_proj.db";

	/**
	 * Create Java Database Connection to specific file of SQLite
	 * Add exit hook to close connection
	 * @param argv user's information
	 */
	public DBC(String...argv) {
		if(argv!=null) {
			if(argv.length > 1) {
				dbType = argv[0];
				dbFile = argv[1];
			}
		}
		if(conn == null) {
			// setup hook to close database connection when shutdown
			Runtime.getRuntime().addShutdownHook(new Thread() { public void run() { DBC.this.close(); } });
			
			try {
				File f = new File(dbFile);
				boolean create = !(f.exists() && !f.isDirectory()) || f.length() < (10 << 10);
				if(create) {
					@SuppressWarnings("unused")
					// clone template database to filesyste from resources, if database does not exist in filesystem
					ResourceUtil ru = new ResourceUtil("template.db","all_proj.db");
				}
				// start connection to database
				conn = DriverManager.getConnection("jdbc:" + dbType + ":" + dbFile);
				check(conn!=null);
				logInfo("DB Connected");
				
			} catch (Exception e) {
				logException(e);
			} 
		}
	}
	
	/**
	 * Query given SQL statement to database
	 * @param sql SQL statement 
	 * @return Queried results
	 */
	private ResultSet query(String sql) {
		ResultSet rs = null;
		Statement statement;
		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(sql);
		} catch (SQLException e) {
			logException(e);
		} 
		return rs;
	}
	
	/**
	 * Parameterized query: SELECT
	 * @param columns Columns to be queried
	 * @param table Table reference
	 * @param conditions String array of conditions
	 * @return Queried result
	 */
	public ResultSet select(String columns, String table, String ...conditions) {
		return query("SELECT " + columns + " FROM " + table + " " + (conditions != null && conditions.length > 0 ? conditions[0] : ""));
	}
	
	/**
	 * Update record(s) in database
	 */
	public void update(String table, String colNval, String conditions) {
		Statement statement = null;
		String sql = "UPDATE " + table
				+ " SET " + colNval + " WHERE " + conditions;
		try {
			if(conn.getAutoCommit()) {
				conn.setAutoCommit(false);
			}
			statement = conn.createStatement();
		    statement.executeUpdate(sql);
		} catch (SQLException e) {
			logException(e);
		}
	}
	
	/**
	 * Commit update
	 */
	public void commitUpdate() {
        try {
			conn.commit();
		} catch (SQLException e) {
			logException(e);
		}
	}
	
	/**
	 * Execute prepared insert statement
	 * @param table INSERT INTO [table] ...
	 * @param columns fields
	 * @param values values(...)
	 * @return key / id of new row
	 */
	public int insert(String table, String columns, String values) {
		PreparedStatement pstmt;
		ResultSet rs;
		int retVal = -1;
		String statementStr =  "INSERT INTO "+ table + " (" + columns + ") VALUES ("+ values + ")";
		logInfo(statementStr);
		try {
			pstmt = conn.prepareStatement(
					 statementStr, 
				     Statement.RETURN_GENERATED_KEYS
				     );

			pstmt.executeUpdate();

			rs = pstmt.getGeneratedKeys(); // will return the ID in ID_COLUMN
			
			if (rs.next()){
			    retVal = rs.getInt(1);
			}
		} catch (SQLException e) {
			logException(e);
		} 

		return retVal;
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
