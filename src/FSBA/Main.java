package FSBA;

import java.awt.im.*;
import java.sql.SQLException;

public class Main {
	/** Object to store user's display name, available to be passed by reference */ 
	private static String[] userDisplayName = new String[1];; // have to object in order to be passed by reference
	/** Database connection */
	private static DBC dbc;
	
	/**
	 * Entry point to FSBA
	 * @param argv
	 * @throws SQLException
	 */
	public static void main(String[] argv) throws SQLException {
		// init db
		dbc = new DBC();
		
		// create LoginDialog: modal dialog
		LoginDialog login = new LoginDialog(dbc, userDisplayName);
		
		Window win = new Window(dbc, userDisplayName[0]);
		
		
	}
}
