package FSBA;

import static FSBA.Window.*;
import static FSBA.Macro.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.im.*;
import java.beans.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.imageio.*;

import javax.swing.*;
import static FSBA.Macro.*;

class LoginDialog extends JDialog {
	/** JDBC instance passed from FSBA.Main */
	private DBC dbc;
	/** Login User's display name: [First Name] [Last Name] */
	private String[] userDisplayName;
	
	/** Background image of LoginDialog */
	private final String backgroundImg = "/resources/LoginBackground.jpg";

	/** Content panel of JDialog */
	private JPanel panel;

	/** Username JTextfield */
	private JTextField usernameField = new JTextField();
	/** Password JPasswordField */
	private JPasswordField passwordField = new JPasswordField();
		
	/** Create instance of LoginDialog */ 
	public LoginDialog(DBC dbc, String[] userDisplayName) {

		setResizable(false);

		this.dbc = dbc;
		this.userDisplayName = userDisplayName;
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.setupComponents();
		
		this.setVisible(true);
		
	}
	
	/** Components' setup */
	private void setupComponents() {
		this.setTitle(APPNAME + " Login");
		//this.setSize(300, 180);
        setSize(new Dimension(win_w, win_h));
		//this.setLocationRelativeTo(null);
		Window.setLocationCenter(this);
		
		//Object[] message = { "Username:", usernameField, "Password:", passwordField };
		
		panel = new JPanel();
	
		panel.setLayout(null);
		usernameField.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		usernameField.setBounds(843, 362, 270, 38);
		usernameField.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));
		panel.add(usernameField);
		
		passwordField.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		passwordField.setBounds(843, 448, 270, 38);
		passwordField.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));
		panel.add(passwordField);
		JLabel background;
		try {
			background = new JLabel(new ImageIcon(ImageIO.read(getClass().getResourceAsStream(backgroundImg))));
		
			background.setBounds(0, 0, 1280, 720);
			background.setVisible(true);
			panel.add(background);

		} catch (IOException e1) {
			logException(e1);
		}
		
		setContentPane(panel);


		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
		passwordField.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	        	// store username and password
				String user = new String(usernameField.getText());
				String pw = new String(passwordField.getPassword());
				
				if(checkLogin(user, pw)) {
					close();
				} else {
					// Invalid username or password
					passwordField.selectAll();
					JOptionPane.showMessageDialog(null, "Invalid username or password!", "Try again", JOptionPane.ERROR_MESSAGE);
					passwordField.requestFocusInWindow();
				}
			
	        }
	    });

	}

	/** Login Check */
	private boolean checkLogin(String user, String pw) {
    	// debug("checkLogin Begin");
    	boolean retVal = false;
    	//debug("user: "+ user + ", pw: "+pw);
    	//ResultSet rs = dbc.query("SELECT COUNT(*),firstName,lastName FROM staff WHERE username=\"" + user + "\" and password=\"" + pw + "\"");
    	ResultSet rs = dbc.select("COUNT(*),firstName,lastName", "staff", "WHERE username=\"" + user + "\" and password=\"" + pw + "\"");
    	int count;
		try {
			count = rs==null ? -1 : rs.getInt(1);
			if(count == 1) {
				retVal = true;
				// assign first Name
	    		userDisplayName[0] = rs.getString(2) + " " + rs.getString(3);
	    		
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
	
	/** Wrap up for LoginDialog */
	public void close() {
		dispose();
	}
}
