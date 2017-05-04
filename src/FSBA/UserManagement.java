package FSBA;

import static FSBA.Macro.*;
import FSBA.DBC;
import java.sql.*;

/**
 * Utilities for user management
 * @author leosin
 *
 */
class UserManagement {
	/** Database connection instance */
	private DBC dbc;
	
	/**
	 * Default constructor
	 */
	public UserManagement() {
		dbc = new DBC();
	}
	
	
	/**
	 * initialize with database connection setup
	 * @param dbc database connection 
	 */
	public UserManagement(DBC dbc) {
		this.dbc = dbc;
	}
	
	/** 
	 * Parameterized Query/Search
	 * @param conditions to be searched
	 * @return the result set of query
	 */
	public ResultSet query(String ... conditions) {
		
		return dbc.select(""
				+ "s.id AS \"Order No.\", "
				+ "o.status AS \"Status\", "
				+ "s.customer AS \"Customer\", " 
				+ "s.phone AS \"Phone\", "
				+ "d.location AS \"Location\", "
				+ "s.date AS \"Date\", "
				+ "t.timeslot AS \"Timeslot\"",
				"sales AS s",
				"LEFT JOIN orderStatuses AS o " //CONDITIONS
					+ "ON o.id = s.status_id "
				+ "LEFT JOIN deliveryLocations AS d "
					+ "ON d.id = s.deliveryLocation_id "
				+ "LEFT JOIN timeslots AS t "
					+ "ON t.id = s.timeslot_id " 
				+ (conditions != null && conditions.length > 0 ? conditions[0] : "")
				);
	}
	
	/**
	 * Update records in database
	 * @param id Row id in table
	 * @param column displayed column
	 * @param newValue new value of the element
	 * @return ...//TODO:
	 */
	public void update(int id, int column, Object newValue) { //TODO: return type		
		//return dbc.update();
	}
	
	/**
	 * Login check query to database
	 * @param user username input
	 * @param pw password input
	 * @param userArr user info
	 * @return whether both username and password matches a distinct account in database
	 */
	public boolean checkLogin(String user, String pw, String[] userArr) {
    	// debug("checkLogin Begin");
    	boolean retVal = false;
    	//debug("user: "+ user + ", pw: "+pw);
    	
    	ResultSet rs = dbc.select("COUNT(*),id,firstName,lastName", "staff", "WHERE username=\"" + user + "\" and password=\"" + pw + "\"");
    	int count;
		try {
			count = rs==null ? -1 : rs.getInt(1);
			if(count == 1) {
				retVal = true;
				// assign displayname
				userArr[0] = Integer.toString(rs.getInt(2));
				userArr[1] = rs.getString(3) + " " + rs.getString(4);
	    		
	    	}
		} catch (SQLException e) {
			logException(e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				logException(e);
			} catch (Exception e) {
				logException(e);
			}
		}
		// debug("checkLogin End: "+ retVal);
    	return retVal;
    }
}
	
