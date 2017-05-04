package FSBA;

/**
 * Entry point of the system
 * @author leosin
 *
 */
public class Main {
	/**
	 * Entry point to FSBA
	 * @param argv Array Vector from user input for command line
	 */
	@SuppressWarnings("unused")
	public static void main(String[] argv) {		
		// variable declarations
		/** Object to store user's display name, available to be passed by reference */ 
		String[] user = new String[2]; // have to object in order to be passed by reference
		
		// create LoginDialog: modal dialog
		// TEST LoginDialog login = new LoginDialog(dbc, userDisplayName);
		LoginDialog login = new LoginDialog(user);
		
		//Window win = new Window(dbc, userDisplayName[0]);
		SalesOrderUI win = new SalesOrderUI(user);
		
	}
}
