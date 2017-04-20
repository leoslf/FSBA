package FSBA;

import java.*;
import java.sql.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import static FSBA.Macro.*;

class DropdownList extends JComboBox {
	/** ResultSet instance obtained from Constructor argument */
	private static ResultSet resultSet = null;
	/** 2-Dimension List of Rows */
	private static Vector<String> rows = new Vector<String>();
	
	DropdownList(ResultSet _resultSet) {
		resultSet = _resultSet;

		setModel(createListModel());
	}
	
	private DefaultComboBoxModel createListModel() {
		try {
			getRows();
		} catch (SQLException e) {
			logException(e);
		}
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		for(String val : rows) {
			//debug(val);
			comboBoxModel.addElement(val);
		}
		return comboBoxModel;
	}
	
	/**
	 * Obtain Rows from resultSet
	 * @throws SQLException
	 */
	static void getRows() throws SQLException {
		while(resultSet.next()) {
				getNextRow();
		}
		
	}
	
	/**
	 * Helper function to obtain nextRows from resultSet
	 */
	static void getNextRow() {
		//rows.add("");
		try {
			String row = resultSet.getString(1);
			//debug(row);
			rows.add(row);
		} catch (SQLException e) {
			logException(e);
		}
	}
}
